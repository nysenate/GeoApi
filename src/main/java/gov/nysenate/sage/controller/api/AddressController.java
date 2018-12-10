package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.address.*;
import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.api.ApiRequest;
import gov.nysenate.sage.provider.address.AddressService;
import gov.nysenate.sage.service.address.AddressServiceProvider;
import gov.nysenate.sage.util.controller.ConstantUtil;
import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

import static gov.nysenate.sage.filter.ApiFilter.getApiRequest;
import static gov.nysenate.sage.model.result.ResultStatus.*;
import static gov.nysenate.sage.util.controller.ApiControllerUtil.getAddressFromParams;
import static gov.nysenate.sage.util.controller.ApiControllerUtil.getAddressesFromJsonBody;
import static gov.nysenate.sage.util.controller.ApiControllerUtil.setApiResponse;

/**
 * Address API controller handles the various AddressService requests including
 *  - Address Validation
 *  - City State Lookup
 *  - ZipCode Lookup
 */
@Controller
@RequestMapping(value = ConstantUtil.REST_PATH + "address")
public final class AddressController
{
    private Logger logger = LoggerFactory.getLogger(AddressController.class);
    private static AddressServiceProvider addressProvider;

    @Autowired
    public AddressController(AddressServiceProvider addressProvider) {
        this.addressProvider = addressProvider;
    }

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
        Object addressResponse = new ApiError(this.getClass(), PROVIDER_NOT_SUPPORTED);

        /** Get the ApiRequest */
        ApiRequest apiRequest = getApiRequest(request);

        logAddressInput(apiRequest, request, punct);

        if (checkProvider(provider, addressResponse)) {
            Address address = getAddressFromParams(addr,addr1,addr2,city,state,zip5,zip4);
            addressResponse = new ValidateResponse(addressProvider.validate(address, provider, punct));
        }
        setApiResponse(addressResponse, request);

    }

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
        Object addressResponse = new ApiError(this.getClass(), PROVIDER_NOT_SUPPORTED);

        /** Get the ApiRequest */
        ApiRequest apiRequest = getApiRequest(request);
        logAddressInput(apiRequest, request, punct);

        if (checkProvider(provider, addressResponse)) {
            Address address = getAddressFromParams(addr,addr1,addr2,city,state,zip5,zip4);
            addressResponse = new CityStateResponse(addressProvider.lookupCityState(address, provider));
        }
        setApiResponse(addressResponse, request);
    }

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
        Object addressResponse = new ApiError(this.getClass(), PROVIDER_NOT_SUPPORTED);

        /** Get the ApiRequest */
        ApiRequest apiRequest = getApiRequest(request);
        logAddressInput(apiRequest, request, punct);

        if (checkProvider(provider, addressResponse)) {
            Address address = getAddressFromParams(addr,addr1,addr2,city,state,zip5,zip4);
            addressResponse = new ZipcodeResponse(addressProvider.lookupZipcode(address, provider));
        }
        setApiResponse(addressResponse, request);
    }


    @RequestMapping(value = "/batch/validate", method = RequestMethod.GET)
    public void addressBatchValidate(HttpServletRequest request, HttpServletResponse response,
                                     @RequestParam(required = false) String provider,
                                     @RequestParam(required = false) boolean punct) throws IOException {
        /** Get the ApiRequest */
        ApiRequest apiRequest = getApiRequest(request);
        logAddressInput(apiRequest, request, punct);

        Object addressResponse = new ApiError(this.getClass(), INVALID_BATCH_ADDRESSES);

        String batchJsonPayload = IOUtils.toString(request.getInputStream(), "UTF-8");
        ArrayList<Address> addresses = getAddressesFromJsonBody(batchJsonPayload);

        if (checkProvider(provider, addressResponse)) {
            if (addresses != null && !addresses.isEmpty()) {
                AddressService addressService = addressProvider.getProviders().get(provider);
                addressResponse = new BatchValidateResponse(addressService.validate(addresses));
            }
        }
    }

    @RequestMapping(value = "/batch/citystate", method = RequestMethod.GET)
    public void addressBatchCityState(HttpServletRequest request, HttpServletResponse response,
                                      @RequestParam(required = false) String provider,
                                      @RequestParam(required = false) boolean punct) throws IOException {
        /** Get the ApiRequest */
        ApiRequest apiRequest = getApiRequest(request);
        logAddressInput(apiRequest, request, punct);

        Object addressResponse = new ApiError(this.getClass(), INVALID_BATCH_ADDRESSES);

        String batchJsonPayload = IOUtils.toString(request.getInputStream(), "UTF-8");
        ArrayList<Address> addresses = getAddressesFromJsonBody(batchJsonPayload);

        if (checkProvider(provider, addressResponse)) {
            if (addresses != null && !addresses.isEmpty()) {
                AddressService addressService = addressProvider.getProviders().get(provider);
                addressResponse = new BatchCityStateResponse(addressService.lookupCityState(addresses));
            }
        }

        setApiResponse(addressResponse, request);


    }


    private void logAddressInput(ApiRequest apiRequest, HttpServletRequest request,boolean usePunctuation ) {
        logger.info("--------------------------------------");
        logger.info(String.format("|%sAddress Request %d ", (apiRequest.isBatch() ? " Batch " : " "), apiRequest.getId()));
        logger.info(String.format("| Mode: %s | Punct: %s", apiRequest.getRequest(), usePunctuation));
        if (!apiRequest.isBatch()) {
            logger.info("| Input Address: " + getAddressFromParams(request).toLogString());
        }
        logger.info("--------------------------------------");
    }

    private boolean checkProvider(String provider, Object addressResponse) {
        boolean providerIsGood = true;
        if (provider != null && !provider.isEmpty()) {
            if (!addressProvider.getProviders().containsKey(provider)) {
                providerIsGood = false;
            }
        }
        return providerIsGood;
    }
}
