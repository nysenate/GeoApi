package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.ApiError;
import gov.nysenate.sage.client.response.CityStateResponse;
import gov.nysenate.sage.client.response.ValidateResponse;
import gov.nysenate.sage.client.response.ZipcodeResponse;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.address.Address;

import gov.nysenate.sage.model.api.ApiRequest;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.service.base.ServiceProviders;
import gov.nysenate.sage.service.address.AddressService;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static gov.nysenate.sage.model.result.ResultStatus.*;

/**
 * Address API controller handles the various AddressService requests including
 *  - Address Validation
 *  - City State Lookup
 *  - ZipCode Lookup
 */
public final class AddressController extends BaseApiController
{
    private Logger logger = Logger.getLogger(AddressController.class);
    private static ServiceProviders<AddressService> addressProviders;

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        addressProviders = ApplicationFactory.getAddressServiceProvider();
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
        Object addressResponse = new ApiError(this.getClass(), RESPONSE_ERROR);

        /** Get the ApiRequest */
        ApiRequest apiRequest = getApiRequest(request);
        String provider = apiRequest.getProvider();

        /**
         * If provider is specified then make sure it matches the available providers. Send an
         * api error and return if the provider is not supported.
         */
        if (provider != null && !provider.isEmpty()) {
            if (!addressProviders.isRegistered(provider)) {
                addressResponse = new ApiError(this.getClass(), PROVIDER_NOT_SUPPORTED);
                setApiResponse(addressResponse, request);
                return;
            }
        }

        /** Handle single request */
        if (!apiRequest.isBatch()) {

            Address address = getAddressFromParams(request);
            if (address != null && !address.isEmpty()) {
                switch (apiRequest.getRequest())
                {
                    case "validate": {
                        addressResponse = new ValidateResponse(validate(address, provider));
                        break;
                    }
                    case "citystate" : {
                        addressResponse = new CityStateResponse(lookupCityState(address, provider));
                        break;
                    }
                    case "zipcode" : {
                        addressResponse = new ZipcodeResponse(lookupZipcode(address, provider));
                        break;
                    }
                    default: {
                        addressResponse = new ApiError(this.getClass(), SERVICE_NOT_SUPPORTED);
                    }
                }
            }
            else {
                addressResponse = new ApiError(AddressController.class, MISSING_ADDRESS);
            }
        }

        /** Set response */
        setApiResponse(addressResponse, request);
    }

    /**
     * USPS requires city so if we get an un-parsed address use MapQuest as the default option.
     */
    public static AddressResult validate(Address address, String provider)
    {
        if (address.isParsed()) {
            return addressProviders.newInstance(provider, "usps").validate(address);
        }
        return addressProviders.newInstance(provider, "mapquest").validate(address);
    }

    /**
     * Use USPS for a city state lookup by default.
     */
    public static AddressResult lookupCityState(Address address, String provider)
    {
        return addressProviders.newInstance(provider, "usps").lookupCityState(address);
    }

    /**
     * Use USPS for zip code lookup by default.
     */
    public static AddressResult lookupZipcode(Address address, String provider)
    {
        return addressProviders.newInstance(provider, "usps").lookupZipCode(address);
    }
}
