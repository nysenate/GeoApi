package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.street.StreetResponse;
import gov.nysenate.sage.model.address.DistrictedStreetRange;
import gov.nysenate.sage.model.api.ApiRequest;
import gov.nysenate.sage.model.result.StreetResult;
import gov.nysenate.sage.provider.district.StreetLookupService;
import gov.nysenate.sage.provider.district.Streetfile;
import gov.nysenate.sage.util.controller.ConstantUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static gov.nysenate.sage.controller.api.filter.ApiFilter.getApiRequest;
import static gov.nysenate.sage.util.controller.ApiControllerUtil.setApiResponse;

@Controller
@RequestMapping(value = ConstantUtil.REST_PATH + "street")
public class StreetController {
    private static final Logger logger = LoggerFactory.getLogger(StreetController.class);
    private final StreetLookupService streetfile;

    @Autowired
    public StreetController(Streetfile streetfile) {
        this.streetfile = streetfile;
    }

    /**
     * Street Lookup Api
     * ---------------------------
     * Look up street data for a zip5 code
     * Usage:
     * (GET)    /api/v2/street/lookup
     *
     * @param request HttpServletRequest
     * @param zip5 String
     */
    @GetMapping(value = "/lookup")
    public void addressBatchCityState(HttpServletRequest request, @RequestParam int zip5) {
        Object streetLookupResponse;
        ApiRequest apiRequest = getApiRequest(request);
        logStreetRequest(apiRequest, zip5);

        logger.info("Getting street data for zip5 {}", zip5);
        List<DistrictedStreetRange> streets = streetfile.streetLookup(zip5);
        StreetResult streetResult = new StreetResult(streetfile.source(), streets);
        if (streets != null) {
            logger.info("Street file look up for zip 5: {} was successful", zip5);
        }
        else {
            logger.warn("No street lookup result was found from the request zip 5:{}", zip5);
        }
        streetLookupResponse = new StreetResponse(streetResult);

        setApiResponse(streetLookupResponse, request);
    }


    private static void logStreetRequest(ApiRequest apiRequest, int zip5) {
        logger.info("--------------------------------------");
        logger.info("|{} Street Request {} ", (apiRequest.isBatch() ? " Batch " : " "), apiRequest.getId());
        logger.info("| Mode: {} | zip5: {}", apiRequest.getRequest(), zip5);
        logger.info("--------------------------------------");
    }
}
