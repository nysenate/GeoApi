package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.base.BaseResponse;
import gov.nysenate.sage.client.response.base.BatchResponse;
import gov.nysenate.sage.client.response.district.DistrictTypeResponse;
import gov.nysenate.sage.client.response.map.MapResponse;
import gov.nysenate.sage.client.response.map.MultipleMapResponse;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.MapListResult;
import gov.nysenate.sage.model.result.MapResult;
import gov.nysenate.sage.provider.district.ShapefileService;
import gov.nysenate.sage.service.district.DistrictMemberProvider;
import gov.nysenate.sage.util.FormatUtil;
import gov.nysenate.sage.util.controller.ConstantUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

@RestController
@RequestMapping(value = ConstantUtil.REST_PATH + "map")
public class MapController extends BaseController {
    private static final Logger logger = LoggerFactory.getLogger(MapController.class);
    private final ShapefileService shapefileService;
    private final DistrictMemberProvider districtMemberProvider;

    @Autowired
    public MapController(ShapefileService shapefileService, DistrictMemberProvider districtMemberProvider) {
        this.shapefileService = shapefileService;
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
    public BaseResponse map(@PathVariable String distType,
                            @RequestParam(required = false) String district,
                            @RequestParam(required = false) boolean showMembers,
                            @RequestParam(required = false) boolean meta) {
        DistrictType districtType = getValue(distType, DistrictType.class);
        if (district != null) {
            district = FormatUtil.cleanString(district);
            logger.debug("Retrieving {} district {} map.", districtType.name(), district);
            MapResult mapResult = shapefileService.getDistrictMap(districtType, district);
            if (showMembers || meta) {
                districtMemberProvider.assignMember(mapResult.getDistrictMap());
            }
            return new MapResponse(mapResult, !meta);
        } else {
            logger.debug("Retrieving all {} district maps.", districtType.name());
            MapListResult mapListResult = shapefileService.getDistrictMaps(districtType);
            if ((showMembers || meta) && mapListResult.getDistrictMaps() != null) {
                for (DistrictMap districtMap : mapListResult.getDistrictMaps()) {
                    districtMemberProvider.assignMember(districtMap);
                }
            }
            return new MultipleMapResponse(mapListResult, !meta);
        }
    }

    @GetMapping("/types")
    public BatchResponse<DistrictTypeResponse> districtTypes() {
        List<DistrictTypeResponse> responses = Arrays.stream(DistrictType.values())
                .filter(type -> !type.lacksShapefile()).map(DistrictTypeResponse::new).toList();
        return new BatchResponse<>(responses, Function.identity());
    }
}
