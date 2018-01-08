package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.address.*;
import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.api.ApiRequest;
import gov.nysenate.sage.service.address.AddressService;
import gov.nysenate.sage.service.address.AddressServiceProvider;
import gov.nysenate.sage.util.Config;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

import static gov.nysenate.sage.model.result.ResultStatus.*;

/**
 * Address API controller handles the various AddressService requests including
 *  - Address Validation
 *  - City State Lookup
 *  - ZipCode Lookup
 */
@Controller
public final class AddressController extends BaseApiController
{
    private Logger logger = Logger.getLogger(AddressController.class);
    private static AddressServiceProvider addressProvider;

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        addressProvider = ApplicationFactory.getAddressServiceProvider();
        logger.debug("Initialized " + this.getClass().getSimpleName());
    }

    /** Proxies to doGet() */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        this.doGet(request, response);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        Object addressResponse;

        /** Get the ApiRequest */
        ApiRequest apiRequest = getApiRequest(request);
        String provider = apiRequest.getProvider();

        Boolean usePunctuation = Boolean.parseBoolean(request.getParameter("punct"));

        logger.info("--------------------------------------");
        logger.info(String.format("|%sAddress Request %d ", (apiRequest.isBatch() ? " Batch " : " "), apiRequest.getId()));
        logger.info(String.format("| Mode: %s | Punct: %s", apiRequest.getRequest(), usePunctuation));
        if (!apiRequest.isBatch()) {
            logger.info("| Input Address: " + getAddressFromParams(request).toLogString());
        }
        logger.info("--------------------------------------");

        /**
         * If provider is specified then make sure it matches the available providers. Send an
         * api error and return if the provider is not supported.
         */
        if (provider != null && !provider.isEmpty()) {
            if (!addressProvider.isRegistered(provider)) {
                addressResponse = new ApiError(this.getClass(), PROVIDER_NOT_SUPPORTED);
                setApiResponse(addressResponse, request);
                return;
            }
        }

        /** Handle single request */
        if (!apiRequest.isBatch()) {
            Address address = getAddressFromParams(request);
            if (address != null && !address.isEmpty()) {
                switch (apiRequest.getRequest()) {
                    case "validate": {
                        addressResponse = new ValidateResponse(addressProvider.validate(address, provider, usePunctuation));
                        break;
                    }
                    case "citystate" : {
                        addressResponse = new CityStateResponse(addressProvider.lookupCityState(address, provider));
                        break;
                    }
                    case "zipcode" : {
                        addressResponse = new ZipcodeResponse(addressProvider.lookupZipcode(address, provider));
                        break;
                    }
                    default: {
                        addressResponse = new ApiError(this.getClass(), SERVICE_NOT_SUPPORTED);
                    }
                }
            }
            else {
                addressResponse = new ApiError(this.getClass(), MISSING_ADDRESS);
            }
        }
        /** Handle batch request */
        else {
            String batchJsonPayload = IOUtils.toString(request.getInputStream(), "UTF-8");
            ArrayList<Address> addresses = getAddressesFromJsonBody(batchJsonPayload);
            if (addresses != null && !addresses.isEmpty()) {
                switch (apiRequest.getRequest()) {
                    case "validate": {
                        AddressService addressService = addressProvider.newInstance(provider, true);
                        addressResponse = new BatchValidateResponse(addressService.validate(addresses));
                        break;
                    }
                    case "citystate": {
                        AddressService addressService = addressProvider.newInstance(provider, true);
                        addressResponse = new BatchCityStateResponse(addressService.lookupCityState(addresses));
                        break;
                    }
                    default : {
                        addressResponse = new ApiError(this.getClass(), SERVICE_NOT_SUPPORTED);
                    }
                }
            }
            else {
                addressResponse = new ApiError(this.getClass(), INVALID_BATCH_ADDRESSES);
            }
        }

        /** Set response */
        setApiResponse(addressResponse, request);
    }
}
