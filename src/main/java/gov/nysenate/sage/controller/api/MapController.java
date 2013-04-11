package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.ApiError;
import gov.nysenate.sage.client.response.MapResponse;
import gov.nysenate.sage.client.response.MultipleMapResponse;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.api.ApiRequest;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.service.map.MapService;
import gov.nysenate.sage.service.map.MapServiceProvider;
import gov.nysenate.sage.util.FormatUtil;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class MapController extends BaseApiController
{
    private static Logger logger = Logger.getLogger(AddressController.class);
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

        /** Get a new map service instance */
        MapService mapService = mapServiceProvider.newInstance();

        DistrictType districtType = DistrictType.resolveType(type);
        if (districtType != null) {
            if (districtCode != null) {
                logger.info("Retrieving " + districtType.name() + " district " + districtCode + " map.");
                mapResponse = new MapResponse(mapService.getDistrictMap(districtType, districtCode));
            }
            else {
                logger.info("Retrieving all " + districtType.name() + "district maps.");
                mapResponse = new MultipleMapResponse(mapService.getDistrictMaps(districtType));
            }
        }
        else {
            mapResponse = new ApiError(this.getClass(), ResultStatus.UNSUPPORTED_DISTRICT_MAP);
        }
        setApiResponse(mapResponse, request);
    }
}
