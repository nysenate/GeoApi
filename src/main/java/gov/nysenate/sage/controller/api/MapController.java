package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.base.BaseResponse;
import gov.nysenate.sage.client.response.map.MapResponse;
import gov.nysenate.sage.client.response.map.MetadataResponse;
import gov.nysenate.sage.client.response.map.MultipleMapResponse;
import gov.nysenate.sage.client.response.map.MultipleMetadataResponse;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.MapResult;
import gov.nysenate.sage.provider.district.DistrictShapefile;
import gov.nysenate.sage.service.district.DistrictMemberProvider;
import gov.nysenate.sage.util.FormatUtil;
import gov.nysenate.sage.util.controller.ConstantUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(value = ConstantUtil.REST_PATH + "map")
public class MapController {
    private static final Logger logger = LoggerFactory.getLogger(MapController.class);
    private final DistrictShapefile districtShapefile;
    private final DistrictMemberProvider districtMemberProvider;

    @Autowired
    public MapController(DistrictShapefile districtShapefile, DistrictMemberProvider districtMemberProvider) {
        this.districtShapefile = districtShapefile;
        this.districtMemberProvider = districtMemberProvider;
    }

    /**
     * District Map Api
     * ---------------------------
     * Get a requested District type district map if it exists
     * Senate, Assembly, Congressional, Zip, County, Town, School boundaries are retrieved with this api
     * @see DistrictType
     * Usage:
     * (GET)    /api/v2/map/{distType}
     */
    @GetMapping(value = "/{distType}")
    public BaseResponse mapSchool(@PathVariable String distType,
                                  @RequestParam(required = false) String district,
                                  @RequestParam(required = false) boolean showMembers,
                                  @RequestParam(required = false) boolean meta) {
        MapResult mapResult;
        DistrictType districtType = DistrictType.resolveType(distType);
        if (district != null) {
            district = FormatUtil.cleanString(district);
            logger.info("Retrieving {} district {} map.", districtType.name(), district);
            mapResult = districtShapefile.getDistrictMap(districtType, district);
            if (showMembers || meta) {
                districtMemberProvider.assignDistrictMembers(mapResult);
            }
            return (meta) ? new MetadataResponse(mapResult) : new MapResponse(mapResult);
        } else {
            logger.info("Retrieving all {} district maps.", districtType.name());
            mapResult = districtShapefile.getDistrictMaps(districtType);
            if (showMembers || meta) {
                districtMemberProvider.assignDistrictMembers(mapResult);
            }
            return (meta) ? new MultipleMetadataResponse(mapResult) : new MultipleMapResponse(mapResult);
        }
    }
}
