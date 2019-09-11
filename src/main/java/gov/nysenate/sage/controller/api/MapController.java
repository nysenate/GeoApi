package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.map.MapResponse;
import gov.nysenate.sage.client.response.map.MetadataResponse;
import gov.nysenate.sage.client.response.map.MultipleMapResponse;
import gov.nysenate.sage.client.response.map.MultipleMetadataResponse;
import gov.nysenate.sage.model.api.ApiRequest;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.MapResult;
import gov.nysenate.sage.provider.district.DistrictShapefile;
import gov.nysenate.sage.service.district.DistrictMemberProvider;
import gov.nysenate.sage.util.controller.ConstantUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static gov.nysenate.sage.filter.ApiFilter.getApiRequest;
import static gov.nysenate.sage.util.controller.ApiControllerUtil.setApiResponse;

@Controller
@RequestMapping(value = ConstantUtil.REST_PATH + "map")
public class MapController {
    private static Logger logger = LoggerFactory.getLogger(MapController.class);
    private DistrictShapefile districtShapefile;
    private DistrictMemberProvider districtMemberProvider;

    @Autowired
    public MapController(DistrictShapefile districtShapefile, DistrictMemberProvider districtMemberProvider) {
        this.districtShapefile = districtShapefile;
        this.districtMemberProvider = districtMemberProvider;
    }

    /**
     * District Map Api
     * ---------------------------
     *
     * Get a requested District type district map if it exists
     *
     * Senate, Assembly, Congressional, Zip, County, Town, School boundaries are retrieved with this api
     *
     * @see DistrictType
     *
     * Usage:
     * (GET)    /api/v2/map/school
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param district String
     * @param showMembers boolean
     * @param meta boolean
     */
    @RequestMapping(value = "/{distType}", method = RequestMethod.GET)
    public void mapSchool(HttpServletRequest request, HttpServletResponse response,
                          @PathVariable String distType,
                          @RequestParam(required = false) String district,
                          @RequestParam(required = false) boolean showMembers,
                          @RequestParam(required = false) boolean meta) {
        /** Get the ApiRequest */
        ApiRequest apiRequest = getApiRequest(request);

        Object mapResponse = getDistrictMap(district, distType, showMembers, meta);
        setApiResponse(mapResponse, request);
    }

    private Object getDistrictMap(String districtCode, String distType,
                                  boolean showMembers, boolean meta) {
        Object mapResponse;
        MapResult mapResult;
        DistrictType districtType = DistrictType.resolveType(distType);
        if (districtCode != null) {
            logger.info("Retrieving " + districtType.name() + " district " + districtCode + " map.");
            mapResult = districtShapefile.getDistrictMap(districtType, districtCode);
            if (showMembers || meta) {
                districtMemberProvider.assignDistrictMembers(mapResult);
            }
            mapResponse = (meta) ? new MetadataResponse(mapResult) : new MapResponse(mapResult);
        } else {
            logger.info("Retrieving all " + districtType.name() + " district maps.");
            mapResult = districtShapefile.getDistrictMaps(districtType);
            if (showMembers || meta) {
                districtMemberProvider.assignDistrictMembers(mapResult);
            }
            mapResponse = (meta) ? new MultipleMetadataResponse(mapResult) : new MultipleMapResponse(mapResult);
        }
        return mapResponse;
    }
}
