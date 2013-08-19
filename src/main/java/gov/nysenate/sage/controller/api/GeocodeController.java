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
import gov.nysenate.sage.model.api.BatchGeocodeRequest;
import gov.nysenate.sage.model.api.GeocodeRequest;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.service.geo.GeocodeServiceProvider;
import gov.nysenate.sage.service.geo.RevGeocodeServiceProvider;
import gov.nysenate.sage.util.Config;
import org.apache.commons.io.IOUtils;
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
    private static Boolean SINGLE_LOGGING_ENABLED = false;
    private static Boolean BATCH_LOGGING_ENABLED = false;
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
        Boolean API_LOGGING_ENABLED = Boolean.parseBoolean(config.getValue("api.logging.enabled", "false"));
        SINGLE_LOGGING_ENABLED = API_LOGGING_ENABLED && Boolean.parseBoolean(config.getValue("detailed.logging.enabled", "true"));
        BATCH_LOGGING_ENABLED = API_LOGGING_ENABLED && Boolean.parseBoolean(config.getValue("batch.detailed.logging.enabled", "false"));
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

        /** Construct a GeocodeRequest using the supplied params */
        GeocodeRequest geocodeRequest = new GeocodeRequest(apiRequest, getAddressFromParams(request), provider, useFallback, useCache);

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
                    if (geocodeRequest.getAddress() != null && !geocodeRequest.getAddress().isEmpty()) {
                        /** Obtain geocode result */
                        GeocodeResult geocodeResult = geocodeServiceProvider.geocode(geocodeRequest);

                        /** Construct response from request */
                        geocodeResponse = new GeocodeResponse(geocodeResult);

                        /** Log geocode request/result to database */
                        if (SINGLE_LOGGING_ENABLED) {
                            int requestId = geocodeRequestLogger.logGeocodeRequest(geocodeRequest);
                            geocodeResultLogger.logGeocodeResult(requestId, geocodeResult);
                        }
                    }
                    else {
                        geocodeResponse = new ApiError(this.getClass(), MISSING_ADDRESS);
                    }
                }
                /** Handle batch geocoding requests */
                else {
                    String batchJsonPayload = IOUtils.toString(request.getInputStream(), "UTF-8");
                    List<Address> addresses = getAddressesFromJsonBody(batchJsonPayload);
                    if (addresses.size() > 0) {
                        BatchGeocodeRequest batchGeocodeRequest = new BatchGeocodeRequest(geocodeRequest);
                        batchGeocodeRequest.setAddresses(addresses);

                        List<GeocodeResult> geocodeResults = geocodeServiceProvider.geocode(batchGeocodeRequest);
                        geocodeResponse = new BatchGeocodeResponse(geocodeResults);

                        if (BATCH_LOGGING_ENABLED) {
                            geocodeResultLogger.logBatchGeocodeResults(batchGeocodeRequest, geocodeResults, true);
                        }
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
                        geocodeRequest.setReverse(true);
                        geocodeRequest.setPoint(point);
                        GeocodeResult revGeocodeResult = revGeocodeServiceProvider.reverseGeocode(geocodeRequest);
                        geocodeResponse = new RevGeocodeResponse(revGeocodeResult);

                        /** Log rev geocode request/result to database */
                        if (SINGLE_LOGGING_ENABLED) {
                            geocodeResultLogger.logGeocodeRequestAndResult(geocodeRequest, revGeocodeResult);
                        }
                    }
                    else {
                        geocodeResponse = new ApiError(this.getClass(), MISSING_POINT);
                    }
                }
                /** Handle batch rev geocoding requests */
                else {
                    String batchJsonPayload = IOUtils.toString(request.getInputStream(), "UTF-8");
                    List<Point> points = getPointsFromJsonBody(batchJsonPayload);
                    if (points.size() > 0) {
                        BatchGeocodeRequest batchGeocodeRequest = new BatchGeocodeRequest(geocodeRequest);
                        batchGeocodeRequest.setReverse(true);
                        batchGeocodeRequest.setPoints(points);

                        List<GeocodeResult> revGeocodeResults = revGeocodeServiceProvider.reverseGeocode(batchGeocodeRequest);
                        geocodeResponse = new BatchGeocodeResponse(revGeocodeResults);

                        if (BATCH_LOGGING_ENABLED) {
                            geocodeResultLogger.logBatchGeocodeResults(batchGeocodeRequest, revGeocodeResults, true);
                        }
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
