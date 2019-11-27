package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.client.response.street.StreetResponse;
import gov.nysenate.sage.model.address.DistrictedStreetRange;
import gov.nysenate.sage.model.api.ApiRequest;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.model.result.StreetResult;
import gov.nysenate.sage.service.street.StreetLookupServiceProvider;
import gov.nysenate.sage.util.FormatUtil;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import gov.nysenate.sage.util.controller.ConstantUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static gov.nysenate.sage.filter.ApiFilter.getApiRequest;
import static gov.nysenate.sage.util.controller.ApiControllerUtil.setApiResponse;

@Controller
@RequestMapping(value = ConstantUtil.REST_PATH + "street")
public class StreetController
{
    private static Logger logger = LoggerFactory.getLogger(StreetController.class);
    private static StreetLookupServiceProvider streetProvider;

    @Autowired
    public StreetController(StreetLookupServiceProvider streetProvider) {
        this.streetProvider = streetProvider;
    }

    /**
     * Street Lookup Api
     * ---------------------------
     *
     * Look up street data for a zip5 code
     *
     * Usage:
     * (GET)    /api/v2/street/lookup
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param zip5 String
     */
    @RequestMapping(value = "/lookup", method = RequestMethod.GET)
    public void addressBatchCityState(HttpServletRequest request, HttpServletResponse response,
                                      @RequestParam String zip5) {
        Object streetLookupResponse;
        /** Get the ApiRequest */
        zip5 = FormatUtil.cleanString(zip5);
        ApiRequest apiRequest = getApiRequest(request);
        logStreetRequest(apiRequest,zip5);

        if (zip5 != null && !zip5.isEmpty()) {
            logger.info("Getting street data for zip5 " + zip5);
            StreetResult streetResult = new StreetResult(this.getClass());
            List<DistrictedStreetRange> streets = streetProvider.getDefaultProvider().streetLookup(zip5);
            if (streets != null) {
                streetResult.setDistrictedStreetRanges(streets);
                streetResult.setStatusCode(ResultStatus.SUCCESS);
                logger.info("Street file look up for zip 5: "+ zip5 + " was successful");
            }
            else {
                streetResult.setStatusCode(ResultStatus.NO_STREET_LOOKUP_RESULT);
                logger.warn("No street lookup result was found from the request zip 5:" + zip5);
            }
            streetLookupResponse = new StreetResponse(streetResult);
        }
        else {
            streetLookupResponse = new ApiError(this.getClass(), ResultStatus.MISSING_ZIPCODE);
            logger.warn("The api request is missing a zip 5 code");
        }

        setApiResponse(streetLookupResponse, request);
    }


    private void logStreetRequest(ApiRequest apiRequest, String zip5) {
        logger.info("--------------------------------------");
        logger.info(String.format("|%s Street Request %d ", (apiRequest.isBatch() ? " Batch " : " "), apiRequest.getId()));
        logger.info(String.format("| Mode: %s | zip5: %s", apiRequest.getRequest(), zip5));
        logger.info("--------------------------------------");
    }
}
