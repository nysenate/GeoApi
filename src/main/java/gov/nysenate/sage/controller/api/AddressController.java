package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.address.*;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.api.ApiRequest;
import gov.nysenate.sage.service.address.AddressServiceProvider;
import gov.nysenate.sage.util.controller.ConstantUtil;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static gov.nysenate.sage.controller.api.filter.ApiFilter.getApiRequest;
import static gov.nysenate.sage.util.controller.ApiControllerUtil.*;

/**
 * Address API controller handles the various AddressService requests including
 *  - Address Validation
 *  - City State Lookup
 *  - ZipCode Lookup
 */
@Controller
@RequestMapping(value = ConstantUtil.REST_PATH + "address")
public final class AddressController {
    private final Logger logger = LoggerFactory.getLogger(AddressController.class);
    private final AddressServiceProvider addressProvider;

    @Autowired
    public AddressController(AddressServiceProvider addressProvider) {
        this.addressProvider = addressProvider;
    }

    /**
     * Address Validation Api
     * ---------------------------
     * Validates an address with USPS
     * Usage:
     * (GET)    /api/v2/address/validate
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param provider String
     * @param punct boolean
     * @param addr String
     * @param addr1 String
     * @param addr2 String
     * @param city String
     * @param state String
     * @param zip5 String
     * @param zip4 String
     */
    @RequestMapping(value = "/validate", method = RequestMethod.GET)
    public void addressValidate(HttpServletRequest request, HttpServletResponse response,
                                @RequestParam(required = false) String provider,
                                @RequestParam(required = false) boolean punct,
                                @RequestParam(required = false) String addr,
                                @RequestParam(required = false) String addr1,
                                @RequestParam(required = false) String addr2,
                                @RequestParam(required = false) String city,
                                @RequestParam(required = false) String state,
                                @RequestParam(required = false) String zip5,
                                @RequestParam(required = false) String zip4) {
        ApiRequest apiRequest = getApiRequest(request);
        logAddressInput(apiRequest, request, punct);
        Address address = getAddressFromParams(addr, addr1, addr2, city, state, zip5, zip4);
        setApiResponse(new ValidateResponse(addressProvider.validate(address, provider, punct)), request);

    }

    /**
     * City State Validation Api
     * ---------------------------
     * Looks up a city state with USPS
     * Usage:
     * (GET)    /api/v2/address/citystate
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param provider String
     * @param punct boolean
     * @param addr String
     * @param addr1 String
     * @param addr2 String
     * @param city String
     * @param state String
     * @param zip5 String
     * @param zip4 String
     */
    @RequestMapping(value = "/citystate", method = RequestMethod.GET)
    public void addressCityState(HttpServletRequest request, HttpServletResponse response,
                                 @RequestParam(required = false) String provider,
                                 @RequestParam(required = false) boolean punct,
                                 @RequestParam(required = false) String addr,
                                 @RequestParam(required = false) String addr1,
                                 @RequestParam(required = false) String addr2,
                                 @RequestParam(required = false) String city,
                                 @RequestParam(required = false) String state,
                                 @RequestParam String zip5,
                                 @RequestParam(required = false) String zip4) {
        ApiRequest apiRequest = getApiRequest(request);
        logAddressInput(apiRequest, request, punct);
        Address address = getAddressFromParams(addr, addr1, addr2, city, state, zip5, zip4);
        Object addressResponse = new CityStateResponse(addressProvider.lookupCityState(address, provider));
        setApiResponse(addressResponse, request);
    }

    /**
     * Zipcode validation Api
     * ---------------------------
     * Looks up a zipcode from an address input with USPS
     * Usage:
     * (GET)    /api/v2/address/zipcode
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param provider String
     * @param punct boolean
     * @param addr String
     * @param addr1 String
     * @param addr2 String
     * @param city String
     * @param state String
     * @param zip5 String
     * @param zip4 String
     */
    @RequestMapping(value = "/zipcode", method = RequestMethod.GET)
    public void addressZipcode(HttpServletRequest request, HttpServletResponse response,
                               @RequestParam(required = false) String provider,
                               @RequestParam(required = false) boolean punct,
                               @RequestParam(required = false) String addr,
                               @RequestParam(required = false) String addr1,
                               @RequestParam(required = false) String addr2,
                               @RequestParam(required = false) String city,
                               @RequestParam(required = false) String state,
                               @RequestParam(required = false) String zip5,
                               @RequestParam(required = false) String zip4) {
        ApiRequest apiRequest = getApiRequest(request);
        logAddressInput(apiRequest, request, punct);
        Address address = getAddressFromParams(addr, addr1, addr2, city, state, zip5, zip4);
        Object addressResponse = new ZipcodeResponse(addressProvider.lookupZipcode(address, provider));
        setApiResponse(addressResponse, request);
    }

    /**
     * Batch Address Validation Api
     * ---------------------------
     * Batch address validation with USPS
     * Usage:
     * (GET)    /api/v2/address/validate/batch
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param provider String
     * @param punct boolean
     */
    @RequestMapping(value = "/validate/batch", method = RequestMethod.POST)
    public void addressBatchValidate(HttpServletRequest request, HttpServletResponse response,
                                     @RequestParam(required = false) String provider,
                                     @RequestParam(required = false) boolean punct) throws IOException {
        ApiRequest apiRequest = getApiRequest(request);
        logAddressInput(apiRequest, request, punct);
        String batchJsonPayload = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
        List<Address> addresses = getAddressesFromJsonBody(batchJsonPayload);
        Object addressResponse = new BatchValidateResponse(addressProvider.validate(addresses, provider, punct));
        setApiResponse(addressResponse, request);
    }

    /**
     * Batch City State Validation Api
     * ---------------------------
     * Batch city state validation with USPS
     * Usage:
     * (GET)    /api/v2/address/citystate/batch
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param provider String
     * @param punct boolean
     */
    @RequestMapping(value = "/citystate/batch", method = RequestMethod.POST)
    public void addressBatchCityState(HttpServletRequest request, HttpServletResponse response,
                                      @RequestParam(required = false) String provider,
                                      @RequestParam(required = false) boolean punct) throws IOException {
        ApiRequest apiRequest = getApiRequest(request);
        logAddressInput(apiRequest, request, punct);
        String batchJsonPayload = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
        List<Address> addresses = getAddressesFromJsonBody(batchJsonPayload);
        Object addressResponse = new BatchCityStateResponse(addressProvider.lookupCityState(addresses, provider));
        setApiResponse(addressResponse, request);
    }


    private void logAddressInput(ApiRequest apiRequest, HttpServletRequest request,boolean usePunctuation ) {
        logger.info("--------------------------------------");
        logger.info("|{}Address Request {} ", (apiRequest.isBatch() ? " Batch " : " "), apiRequest.getId());
        logger.info("| Mode: {} | Punct: {}", apiRequest.getRequest(), usePunctuation);
        if (!apiRequest.isBatch()) {
            logger.info("| Input Address: {}", getAddressFromParams(request).toLogString());
        }
        logger.info("--------------------------------------");
    }
}
