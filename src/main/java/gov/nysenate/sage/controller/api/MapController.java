package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.client.response.map.MapResponse;
import gov.nysenate.sage.client.response.map.MetadataResponse;
import gov.nysenate.sage.client.response.map.MultipleMapResponse;
import gov.nysenate.sage.client.response.map.MultipleMetadataResponse;
import gov.nysenate.sage.model.api.ApiRequest;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.MapResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.provider.DistrictShapefile;
import gov.nysenate.sage.service.district.DistrictMemberProvider;
import gov.nysenate.sage.service.map.MapService;
import gov.nysenate.sage.service.map.MapServiceProvider;
import gov.nysenate.sage.util.controller.ConstantUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static gov.nysenate.sage.filter.ApiFilter.getApiRequest;
import static gov.nysenate.sage.util.controller.ApiControllerUtil.setApiResponse;

@Controller
@RequestMapping(value = ConstantUtil.REST_PATH + "map")
public class MapController
{
    private static Logger logger = LoggerFactory.getLogger(MapController.class);
    private DistrictShapefile districtShapefile;

    @Autowired
    public MapController(DistrictShapefile districtShapefile) {
        this.districtShapefile = districtShapefile;
    }

    @RequestMapping(value = "/senate", method = RequestMethod.GET)
    public void mapSenate(HttpServletRequest request, HttpServletResponse response,
                                @RequestParam String district, @RequestParam boolean showMembers,
                                @RequestParam boolean meta) {

        /** Get the ApiRequest */
        ApiRequest apiRequest = getApiRequest(request);
        Object mapResponse = getDistrictMap(district,"senate",showMembers,meta);
        setApiResponse(mapResponse, request);
    }

    @RequestMapping(value = "/assembly", method = RequestMethod.GET)
    public void mapAssembly(HttpServletRequest request, HttpServletResponse response,
                          @RequestParam String district, @RequestParam boolean showMembers,
                          @RequestParam boolean meta) {

        /** Get the ApiRequest */
        ApiRequest apiRequest = getApiRequest(request);

        Object mapResponse = getDistrictMap(district,"assembly",showMembers,meta);
        setApiResponse(mapResponse, request);
    }

    @RequestMapping(value = "/congressional", method = RequestMethod.GET)
    public void mapCongressional(HttpServletRequest request, HttpServletResponse response,
                          @RequestParam String district, @RequestParam boolean showMembers,
                          @RequestParam boolean meta) {

        /** Get the ApiRequest*/
        ApiRequest apiRequest = getApiRequest(request);

        Object mapResponse = getDistrictMap(district,"congressional",showMembers,meta);
        setApiResponse(mapResponse, request);
    }

    @RequestMapping(value = "/county", method = RequestMethod.GET)
    public void mapCounty(HttpServletRequest request, HttpServletResponse response,
                          @RequestParam String district, @RequestParam boolean showMembers,
                          @RequestParam boolean meta) {

        /** Get the ApiRequest */
        ApiRequest apiRequest = getApiRequest(request);

        Object mapResponse = getDistrictMap(district,"county",showMembers,meta);
        setApiResponse(mapResponse, request);
    }

    @RequestMapping(value = "/town", method = RequestMethod.GET)
    public void mapTown(HttpServletRequest request, HttpServletResponse response,
                          @RequestParam String district, @RequestParam boolean showMembers,
                          @RequestParam boolean meta) {
        /** Get the ApiRequest */
        ApiRequest apiRequest = getApiRequest(request);

        Object mapResponse = getDistrictMap(district,"town",showMembers,meta);
        setApiResponse(mapResponse, request);
    }

    @RequestMapping(value = "/school", method = RequestMethod.GET)
    public void mapSchool(HttpServletRequest request, HttpServletResponse response,
                          @RequestParam String district, @RequestParam boolean showMembers,
                          @RequestParam boolean meta) {
        /** Get the ApiRequest */
        ApiRequest apiRequest = getApiRequest(request);

        Object mapResponse = getDistrictMap(district,"school",showMembers,meta);
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
                DistrictMemberProvider.assignDistrictMembers(mapResult);
            }
            mapResponse = (meta) ? new MetadataResponse(mapResult) : new MapResponse(mapResult);
        }
        else {
            logger.info("Retrieving all " + districtType.name() + " district maps.");
            mapResult = districtShapefile.getDistrictMaps(districtType);
            if (showMembers || meta) {
                DistrictMemberProvider.assignDistrictMembers(mapResult);
            }
            mapResponse = (meta) ? new MultipleMetadataResponse(mapResult) : new MultipleMapResponse(mapResult);
        }
        return mapResponse;
    }
}
