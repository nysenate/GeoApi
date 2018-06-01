package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.client.response.geo.BatchGeocodeResponse;
import gov.nysenate.sage.client.response.geo.GeocodeResponse;
import gov.nysenate.sage.client.response.geo.RevGeocodeResponse;
import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.dao.logger.GeocodeRequestLogger;
import gov.nysenate.sage.dao.logger.GeocodeResultLogger;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.api.ApiRequest;
import gov.nysenate.sage.model.api.BatchGeocodeRequest;
import gov.nysenate.sage.model.api.GeocodeRequest;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.service.geo.GeocodeServiceProvider;
import gov.nysenate.sage.service.geo.RevGeocodeServiceProvider;
import gov.nysenate.sage.util.TimeUtil;
import gov.nysenate.sage.util.controller.ConstantUtil;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

import static gov.nysenate.sage.filter.ApiFilter.getApiRequest;
import static gov.nysenate.sage.model.result.ResultStatus.*;
import static gov.nysenate.sage.util.controller.ApiControllerUtil.*;

/** Handles Geo Api requests */
@Controller
@RequestMapping(value = ConstantUtil.REST_PATH + "geo")
public class GeocodeController
{
    private static Logger logger = LogManager.getLogger(GeocodeController.class);
    private static GeocodeServiceProvider geocodeServiceProvider;
    private static RevGeocodeServiceProvider revGeocodeServiceProvider;

    /** Usage loggers */
    private static Boolean SINGLE_LOGGING_ENABLED = false;
    private static Boolean BATCH_LOGGING_ENABLED = false;
    private static GeocodeRequestLogger geocodeRequestLogger;
    private static GeocodeResultLogger geocodeResultLogger;
    private final Environment env;

    @Autowired
    public GeocodeController(Environment env, GeocodeRequestLogger geocodeRequestLogger,
                             GeocodeResultLogger geocodeResultLogger, GeocodeServiceProvider geocodeServiceProvider,
                             RevGeocodeServiceProvider revGeocodeServiceProvider) {
        this.env = env;
        this.geocodeRequestLogger = geocodeRequestLogger;
        this.geocodeResultLogger = geocodeResultLogger;
        this.geocodeServiceProvider = geocodeServiceProvider;
        this.revGeocodeServiceProvider = revGeocodeServiceProvider;

        logger.debug("Initialized " + this.getClass().getSimpleName());
        boolean API_LOGGING_ENABLED = env.isApiLoggingEnabled();
        SINGLE_LOGGING_ENABLED = API_LOGGING_ENABLED && env.isDetailedLoggingEnabled();
        BATCH_LOGGING_ENABLED = API_LOGGING_ENABLED && env.isBatchDetailedLoggingEnabled();
    }

    @RequestMapping(value = "/geocode", method = RequestMethod.GET)
    public void geoGeocode(HttpServletRequest request, HttpServletResponse response,
                                @RequestParam String provider,
                                @RequestParam String addr, @RequestParam String addr1, @RequestParam String addr2,
                                @RequestParam String city, @RequestParam String state, @RequestParam String zip5,
                                @RequestParam String zip4, @RequestParam boolean useFallback,
                                @RequestParam boolean doNotCache, @RequestParam boolean bypassCache) {

        Object geocodeResponse = new ApiError(this.getClass(), SERVICE_NOT_SUPPORTED);
        Timestamp startTime = getCurrentTimeStamp();
        ApiRequest apiRequest = getApiRequest(request);
        Boolean useCache = true;

        determineCacheProviderProps(useCache,bypassCache,doNotCache,provider);

        int requestId = -1;

        /** Construct a GeocodeRequest using the supplied params */
        GeocodeRequest geocodeRequest = new GeocodeRequest(apiRequest,
                getAddressFromParams(addr,addr1,addr2,city,state,zip5,zip4),
                provider, useFallback, useCache, bypassCache, doNotCache);

        logGeoRequest(apiRequest,geocodeRequest,requestId);

        if (!checkProvider(provider, geocodeResponse, request)) {
            return;
        }

        if (geocodeRequest.getAddress() != null && !geocodeRequest.getAddress().isEmpty()) {
            /** Obtain geocode result */
            GeocodeResult geocodeResult = geocodeServiceProvider.geocode(geocodeRequest);

            /** Construct response from request */
            geocodeResponse = new GeocodeResponse(geocodeResult);

            /** Log geocode request/result to database */
            if (SINGLE_LOGGING_ENABLED && requestId != -1) {
                geocodeResultLogger.logGeocodeResult(requestId, geocodeResult);
            }
        }
        else {
            geocodeResponse = new ApiError(this.getClass(), MISSING_ADDRESS);
        }

        logElaspedTime(startTime);
        setApiResponse(geocodeResponse,request);
    }

    @RequestMapping(value = "/revgeocode", method = RequestMethod.GET)
    public void geoRevGeocode(HttpServletRequest request, HttpServletResponse response,
                           @RequestParam String provider, @RequestParam String lat, @RequestParam String lon,
                              @RequestParam boolean useFallback, @RequestParam boolean doNotCache,
                              @RequestParam boolean bypassCache) {
        Object geocodeResponse = new ApiError(this.getClass(), SERVICE_NOT_SUPPORTED);
        Timestamp startTime = getCurrentTimeStamp();
        ApiRequest apiRequest = getApiRequest(request);
        Boolean useCache = true;

        determineCacheProviderProps(useCache,bypassCache,doNotCache,provider);

        int requestId = -1;

        /** Construct a GeocodeRequest using the supplied params */
        GeocodeRequest geocodeRequest = new GeocodeRequest(apiRequest,
                new Address(), provider, useFallback, useCache, bypassCache, doNotCache);

        logGeoRequest(apiRequest,geocodeRequest,requestId);

        if (!checkProvider(provider, geocodeResponse, request)) {
            return;
        }

        Point point = getPointFromParams(lat,lon);
        if (point != null ) {
            geocodeRequest.setReverse(true);
            geocodeRequest.setPoint(point);
            GeocodeResult revGeocodeResult = revGeocodeServiceProvider.reverseGeocode(geocodeRequest);
            geocodeResponse = new RevGeocodeResponse(revGeocodeResult);

            /** Log rev geocode request/result to database */
            if (SINGLE_LOGGING_ENABLED) {
                geocodeResultLogger.logGeocodeResult(requestId, revGeocodeResult);
                requestId = -1;
            }
        }
        else {
            geocodeResponse = new ApiError(this.getClass(), MISSING_POINT);
        }

        logElaspedTime(startTime);
        setApiResponse(geocodeResponse,request);
    }

    @RequestMapping(value = "/batch/geocode", method = RequestMethod.GET)
    public void geoBatchGeocode(HttpServletRequest request, HttpServletResponse response,
                           @RequestParam String provider, @RequestParam boolean useFallback,
                           @RequestParam boolean doNotCache, @RequestParam boolean bypassCache) throws IOException {

        Object geocodeResponse = new ApiError(this.getClass(), SERVICE_NOT_SUPPORTED);
        Timestamp startTime = getCurrentTimeStamp();
        ApiRequest apiRequest = getApiRequest(request);
        Boolean useCache = true;

        determineCacheProviderProps(useCache,bypassCache,doNotCache,provider);

        int requestId = -1;

        /** Construct a GeocodeRequest using the supplied params */
        GeocodeRequest geocodeRequest = new GeocodeRequest(apiRequest,
                new Address(), provider, useFallback, useCache, bypassCache, doNotCache);

        logGeoRequest(apiRequest,geocodeRequest,requestId);

        if (!checkProvider(provider, geocodeResponse, request)) {
            return;
        }

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

        logElaspedTime(startTime);
        setApiResponse(geocodeResponse,request);
    }

    @RequestMapping(value = "/batch/revgeocode", method = RequestMethod.GET)
    public void geoBatchRevGeocode(HttpServletRequest request, HttpServletResponse response,
                           @RequestParam String provider,
                           @RequestParam String addr, @RequestParam String addr1, @RequestParam String addr2,
                           @RequestParam String city, @RequestParam String state, @RequestParam String zip5,
                           @RequestParam String zip4, @RequestParam boolean useFallback,
                           @RequestParam boolean doNotCache, @RequestParam boolean bypassCache) throws IOException {

        Object geocodeResponse = new ApiError(this.getClass(), SERVICE_NOT_SUPPORTED);
        Timestamp startTime = getCurrentTimeStamp();
        ApiRequest apiRequest = getApiRequest(request);
        Boolean useCache = true;

        determineCacheProviderProps(useCache,bypassCache,doNotCache,provider);

        int requestId = -1;

        /** Construct a GeocodeRequest using the supplied params */
        GeocodeRequest geocodeRequest = new GeocodeRequest(apiRequest,
                new Address(), provider, useFallback, useCache, bypassCache, doNotCache);

        logGeoRequest(apiRequest,geocodeRequest,requestId);

        if (!checkProvider(provider, geocodeResponse, request)) {
            return;
        }

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

        logElaspedTime(startTime);
        setApiResponse(geocodeResponse,request);

    }

    private void logGeoRequest(ApiRequest apiRequest, GeocodeRequest geocodeRequest, int requestId) {
        logger.info("=======================================================");
        logger.info(String.format("|%sGeocode Request %d ", (apiRequest.isBatch() ? " Batch " : " "), apiRequest.getId()));
        logger.info(String.format("| Mode: %s | IP: %s | Provider: %s", apiRequest.getRequest(), apiRequest.getIpAddress(), apiRequest.getProvider()));
        if (!apiRequest.isBatch() && apiRequest.getRequest().equals("geocode") || apiRequest.getRequest().equals("geocache")) {
            logger.info("| Input Address: " + geocodeRequest.getAddress().toLogString());
        }
        logger.info("=======================================================");

        if (SINGLE_LOGGING_ENABLED) {
            requestId = geocodeRequestLogger.logGeocodeRequest(geocodeRequest);
        }
    }

    private boolean checkProvider(String provider, Object geocodeResponse, HttpServletRequest request) {
        /**
         * If provider is specified then make sure it matches the available providers. Send an
         * api error and return if the provider is not supported.
         */
        if (provider != null && !provider.isEmpty() && !geocodeServiceProvider.isRegistered(provider) && !provider.equals("geocache")) {
            geocodeResponse = new ApiError(this.getClass(), PROVIDER_NOT_SUPPORTED);
            setApiResponse(geocodeResponse, request);
            return false;
        }
        return true;
    }

    private void determineCacheProviderProps(boolean useCache, boolean bypassCache, boolean doNotCache, String provider) {
        /** Only want to use cache when the provider is not specified */
        useCache = (provider == null);
        if (bypassCache || doNotCache) {
            useCache = false;
        }
        if (provider != null && provider.equals("geocache")) {
            useCache = true;
        }
    }

    private void logElaspedTime(Timestamp startTime) {
        logger.info(String.format("Geo Response in %s ms.", TimeUtil.getElapsedMs(startTime)));
    }

    private Timestamp getCurrentTimeStamp() {
        return TimeUtil.currentTimestamp();
    }

}
