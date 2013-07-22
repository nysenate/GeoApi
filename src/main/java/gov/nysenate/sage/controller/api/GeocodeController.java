package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.client.response.geo.BatchGeocodeResponse;
import gov.nysenate.sage.client.response.geo.GeocodeResponse;
import gov.nysenate.sage.client.response.geo.RevGeocodeResponse;
import gov.nysenate.sage.dao.logger.GeocodeRequestLogger;
import gov.nysenate.sage.dao.logger.GeocodeResultLogger;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.api.ApiRequest;
import gov.nysenate.sage.model.api.GeocodeRequest;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.service.geo.GeocodeServiceProvider;
import gov.nysenate.sage.service.geo.RevGeocodeServiceProvider;
import gov.nysenate.sage.util.Config;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import static gov.nysenate.sage.model.result.ResultStatus.*;

/** Handles Geo Api requests */
public class GeocodeController extends BaseApiController implements Observer
{
    private static Logger logger = Logger.getLogger(GeocodeController.class);
    private static Config config = ApplicationFactory.getConfig();
    private static GeocodeServiceProvider geocodeServiceProvider = ApplicationFactory.getGeocodeServiceProvider();
    private static RevGeocodeServiceProvider revGeocodeServiceProvider = ApplicationFactory.getRevGeocodeServiceProvider();


    /** Usage loggers */
    private static Boolean LOGGING_ENABLED = false;
    private static GeocodeRequestLogger geocodeRequestLogger;
    private static GeocodeResultLogger geocodeResultLogger;

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        logger.debug("Initialized " + this.getClass().getSimpleName());
        geocodeRequestLogger = new GeocodeRequestLogger();
        geocodeResultLogger = new GeocodeResultLogger();
        update(null, null);
    }

    @Override
    public void update(Observable o, Object arg) {
        LOGGING_ENABLED = Boolean.parseBoolean(config.getValue("api.logging.enabled"));
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

        /** Only want to use cache when the provider is not specified */
        boolean useCache = (provider == null);

        /**
         * If provider is specified then make sure it matches the available providers. Send an
         * api error and return if the provider is not supported.
         */
        if (provider != null && !provider.isEmpty() && !geocodeServiceProvider.isRegistered(provider)) {
            geocodeResponse = new ApiError(this.getClass(), PROVIDER_NOT_SUPPORTED);
            setApiResponse(geocodeResponse, request);
            return;
        }

        logger.info("--------------------------------------");
        logger.info(String.format("Geocode Request | Mode: %s", apiRequest.getRequest()));
        logger.info("--------------------------------------");

        switch (apiRequest.getRequest()) {
            case "geocode":
            {
                /** Handle single geocoding requests */
                if (!apiRequest.isBatch()) {
                    Address address = getAddressFromParams(request);
                    if (address != null && !address.isEmpty()) {
                        /** Obtain geocode response */
                        GeocodeResult geocodeResult = geocodeServiceProvider.geocode(address, provider, useFallback, useCache);

                        /** Log geocode request/result to database */
                        int requestId = geocodeRequestLogger.logGeocodeRequest(new GeocodeRequest(apiRequest, address, provider, useFallback, useCache));
                        geocodeResultLogger.logGeocodeResult(requestId, geocodeResult);

                        /** Construct response from request */
                        geocodeResponse = new GeocodeResponse(geocodeResult);
                    }
                    else {
                        geocodeResponse = new ApiError(this.getClass(), MISSING_ADDRESS);
                    }
                }
                /** Handle batch geocoding requests */
                else {
                    List<Address> addresses = getAddressesFromJsonBody(request);
                    if (addresses.size() > 0) {
                        List<GeocodeResult> geocodeResults = geocodeServiceProvider.geocode(addresses);
                        geocodeResponse = new BatchGeocodeResponse(geocodeResults);
                    }
                    else {
                        geocodeResponse = new ApiError(this.getClass(), INVALID_BATCH_ADDRESSES);
                    }
                }
                break;
            }
            case "revgeocode" :
            {
                /** Handle single rev geocoding requests */
                if (!apiRequest.isBatch()) {
                    Point point = getPointFromParams(request);
                    if (point != null ) {
                        logger.debug("sending to rev geo service provider");
                        GeocodeResult revGeocodeResult = revGeocodeServiceProvider.reverseGeocode(point, provider, true);
                        geocodeResponse = new RevGeocodeResponse(revGeocodeResult);
                    }
                    else {
                        geocodeResponse = new ApiError(this.getClass(), MISSING_POINT);
                    }
                }
                /** Handle batch rev geocoding requests */
                else {
                    List<Point> points = getPointsFromJsonBody(request);
                    if (points.size() > 0) {
                        List<GeocodeResult> revGeocodeResults = revGeocodeServiceProvider.reverseGeocode(points);
                        geocodeResponse = new BatchGeocodeResponse(revGeocodeResults);
                    }
                    else {
                        geocodeResponse = new ApiError(this.getClass(), INVALID_BATCH_POINTS);
                    }
                }
                break;
            }
            default: {
                geocodeResponse = new ApiError(this.getClass(), SERVICE_NOT_SUPPORTED);
            }
        }
        /** Set response */
        setApiResponse(geocodeResponse, request);
    }
}
