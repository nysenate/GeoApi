package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.client.response.street.StreetResponse;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.address.DistrictedStreetRange;
import gov.nysenate.sage.model.api.ApiRequest;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.model.result.StreetResult;
import gov.nysenate.sage.service.street.StreetLookupServiceProvider;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class StreetController extends BaseApiController
{
    private static Logger logger = Logger.getLogger(StreetController.class);
    private static StreetLookupServiceProvider streetProvider = ApplicationFactory.getStreetLookupServiceProvider();

    @Override
    public void init(ServletConfig config) throws ServletException {}

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        doGet(request, response);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        Object streetLookupResponse;

        /** Get the ApiRequest */
        ApiRequest apiRequest = getApiRequest(request);

        /** Zip5 is required to perform street lookup */
        String zip5 = request.getParameter("zip5");

        switch (apiRequest.getRequest()) {
            case "lookup" :
                if (zip5 != null && !zip5.isEmpty()) {
                    logger.info("Getting street data for zip5 " + zip5);
                    StreetResult streetResult = new StreetResult(this.getClass());
                    List<DistrictedStreetRange> streets = streetProvider.newInstance ().streetLookup(zip5);
                    if (streets != null) {
                        streetResult.setDistrictedStreetRanges(streets);
                        streetResult.setStatusCode(ResultStatus.SUCCESS);
                    }
                    else {
                        streetResult.setStatusCode(ResultStatus.NO_STREET_LOOKUP_RESULT);
                    }
                    streetLookupResponse = new StreetResponse(streetResult);
                }
                else {
                    streetLookupResponse = new ApiError(this.getClass(), ResultStatus.MISSING_ZIPCODE);
                }
                break;

            default :
                streetLookupResponse = new ApiError(this.getClass(), ResultStatus.SERVICE_NOT_SUPPORTED);
        }

        setApiResponse(streetLookupResponse, request);
    }
}
