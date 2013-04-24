package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.ApiError;
import gov.nysenate.sage.client.response.GeocodeResponse;
import gov.nysenate.sage.client.response.RevGeocodeResponse;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.api.ApiRequest;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.service.geo.GeocodeServiceProvider;
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
    private static GeocodeServiceProvider geocodeServiceProvider = ApplicationFactory.getGeocodeServiceProvider();

    @Override
    public void init(ServletConfig config) throws ServletException
    {
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

        /** Check whether or not to fallback */
        boolean useFallback = requestParameterEquals(request, "useFallback", "false") ? false : true;

        /**
         * If provider is specified then make sure it matches the available providers. Send an
         * api error and return if the provider is not supported.
         */
        if (provider != null && !provider.isEmpty() && !geocodeServiceProvider.isRegistered(provider)) {
            geocodeResponse = new ApiError(this.getClass(), PROVIDER_NOT_SUPPORTED);
            setApiResponse(geocodeResponse, request);
            return;
        }

        /** Handle single geocoding requests */
        if (!apiRequest.isBatch()) {
            switch (apiRequest.getRequest()) {
                case "geocode": {
                    Address address = getAddressFromParams(request);
                    if (address != null && !address.isEmpty()) {
                        geocodeResponse = new GeocodeResponse(
                                geocodeServiceProvider.geocode(address, provider, useFallback));
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
     * Reverse geocoding uses the same strategy as <code>geocode</code>
     */
    public static GeocodeResult reverseGeocode(Point point, String provider)
    {
        if (provider != null && !provider.isEmpty()) {
            return geocodeServiceProvider.newInstance(provider).reverseGeocode(point);
        }
        else {
            GeocodeResult result = geocodeServiceProvider.newInstance("tiger").reverseGeocode(point);
            if (!result.isSuccess()) {
                result = geocodeServiceProvider.newInstance("yahoo").reverseGeocode(point);
                if (!result.isSuccess()) {
                    result = geocodeServiceProvider.newInstance("mapquest").reverseGeocode(point);
                }
            }
            return result;
        }
    }
}
