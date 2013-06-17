package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.client.response.map.MapResponse;
import gov.nysenate.sage.client.response.map.MetadataResponse;
import gov.nysenate.sage.client.response.map.MultipleMapResponse;
import gov.nysenate.sage.client.response.map.MultipleMetadataResponse;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.api.ApiRequest;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.MapResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.service.district.DistrictMemberProvider;
import gov.nysenate.sage.service.map.MapService;
import gov.nysenate.sage.service.map.MapServiceProvider;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class MapController extends BaseApiController
{
    private static Logger logger = Logger.getLogger(MapController.class);
    private static MapServiceProvider mapServiceProvider = ApplicationFactory.getMapServiceProvider();

    @Override
    public void init(ServletConfig config) throws ServletException {}

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        Object mapResponse = null;

        /** Get the ApiRequest */
        ApiRequest apiRequest = getApiRequest(request);

        /** The request represents the district type */
        String type = apiRequest.getRequest();

        /** Get the district code if exists */
        String districtCode = request.getParameter("district");

        /** Parameter to specify if member data should be retrieved if applicable */
        String showMembersStr = request.getParameter("showMembers");
        Boolean showMembers = (showMembersStr != null && showMembersStr.equals("true")) ? true : false;

        /** Meta data requests simply return listing info, no maps or members */
        boolean meta = requestParameterEquals(request, "meta", "true");

        /** Get a new map service instance */
        MapService mapService = mapServiceProvider.newInstance();

        DistrictType districtType = DistrictType.resolveType(type);
        if (districtType != null) {
            MapResult mapResult;
            if (districtCode != null) {
                logger.info("Retrieving " + districtType.name() + " district " + districtCode + " map.");
                mapResult = mapService.getDistrictMap(districtType, districtCode);
                if (showMembers || meta) {
                    DistrictMemberProvider.assignDistrictMembers(mapResult);
                }
                mapResponse = (meta) ? new MetadataResponse(mapResult) : new MapResponse(mapResult);
            }
            else {
                logger.info("Retrieving all " + districtType.name() + " district maps.");
                mapResult = mapService.getDistrictMaps(districtType);
                if (showMembers || meta) {
                    DistrictMemberProvider.assignDistrictMembers(mapResult);
                }
                mapResponse = (meta) ? new MultipleMetadataResponse(mapResult) : new MultipleMapResponse(mapResult);
            }
        }
        else {
            mapResponse = new ApiError(this.getClass(), ResultStatus.UNSUPPORTED_DISTRICT_MAP);
        }
        setApiResponse(mapResponse, request);
    }
}
