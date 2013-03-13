package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.api.ApiError;
import gov.nysenate.sage.client.api.geo.GeocodeResponse;
import gov.nysenate.sage.client.api.geo.RevGeocodeResponse;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.api.ApiRequest;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.service.ServiceProviders;
import gov.nysenate.sage.service.geo.GeocodeService;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static gov.nysenate.sage.model.result.ResultStatus.*;

/**
 *
 */
public class GeocodeController extends BaseApiController
{
    private Logger logger = Logger.getLogger(GeocodeController.class);
    private static ServiceProviders<GeocodeService> geocodeProviders;

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        geocodeProviders = ApplicationFactory.getGeoCodeServiceProviders();
        logger.debug("Initialized " + this.getClass().getSimpleName());
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        doGet(request, response);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        Object geocodeResponse;

        /** Get the ApiRequest */
        ApiRequest apiRequest = getApiRequest(request);
        String provider = apiRequest.getProvider();

        /**
         * If provider is specified then make sure it matches the available providers. Send an
         * api error and return if the provider is not supported.
         */
        if (provider != null && !provider.isEmpty()) {
            if (!geocodeProviders.isRegistered(provider)) {
                geocodeResponse = new ApiError(this.getClass(), PROVIDER_NOT_SUPPORTED);
                setApiResponse(geocodeResponse, request);
                return;
            }
        }

        /** Handle single request */
        if (!apiRequest.isBatch()) {
            switch (apiRequest.getRequest()) {
                case "geocode": {
                    Address address = getAddressFromParams(request);
                    if (address != null && !address.isEmpty()) {
                        geocodeResponse = new GeocodeResponse(geocode(address, provider));
                    }
                    else {
                        geocodeResponse = new ApiError(this.getClass(), MISSING_ADDRESS);
                    }
                    break;
                }
                case "revgeocode" : {
                    Point point = getPointFromParams(request);
                    if (point != null ) {
                        geocodeResponse = new RevGeocodeResponse(reverseGeocode(point, provider));
                    }
                    else {
                        geocodeResponse = new ApiError(this.getClass(), MISSING_POINT);
                    }
                    break;
                }
                default: {
                    geocodeResponse = new ApiError(this.getClass(), SERVICE_NOT_SUPPORTED);
                }
            }
        }
        else {
            geocodeResponse = new ApiError(this.getClass(), FEATURE_NOT_SUPPORTED);
        }

        /** Set response */
        setApiResponse(geocodeResponse, request);
    }

    /**
     * The default strategy for geocoding is as follows:
     *
     * If provider is specified - use that, no questions asked.
     * Otherwise start with TigerGeocoder. If that doesn't return a house level match
     * then try Yahoo. If Yahoo gets rate limited or does not return a match then
     * try with MapQuest.
     */
    public static GeocodeResult geocode(Address address, String provider)
    {
        if (provider != null && !provider.isEmpty()) {
            return geocodeProviders.newInstance(provider).geocode(address);
        }
        else {
            GeocodeResult result = geocodeProviders.newInstance("tiger").geocode(address);
            if (!result.isSuccess()) {
                result = geocodeProviders.newInstance("yahoo").geocode(address);
                if (!result.isSuccess()) {
                    result = geocodeProviders.newInstance("mapquest").geocode(address);
                }
            }
            return result;
        }
    }

    /**
     * Reverse geocoding uses the same strategy as <code>geocode</code>
     */
    public static GeocodeResult reverseGeocode(Point point, String provider)
    {
        if (provider != null && !provider.isEmpty()) {
            return geocodeProviders.newInstance(provider).reverseGeocode(point);
        }
        else {
            GeocodeResult result = geocodeProviders.newInstance("tiger").reverseGeocode(point);
            if (!result.isSuccess()) {
                result = geocodeProviders.newInstance("yahoo").reverseGeocode(point);
                if (!result.isSuccess()) {
                    result = geocodeProviders.newInstance("mapquest").reverseGeocode(point);
                }
            }
            return result;
        }
    }
}
