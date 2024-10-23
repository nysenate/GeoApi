package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.client.response.geo.BatchGeocodeResponse;
import gov.nysenate.sage.client.response.geo.GeocodeResponse;
import gov.nysenate.sage.client.response.geo.RevGeocodeResponse;
import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.dao.logger.geocode.SqlGeocodeRequestLogger;
import gov.nysenate.sage.dao.logger.geocode.SqlGeocodeResultLogger;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.api.ApiRequest;
import gov.nysenate.sage.model.api.BatchGeocodeRequest;
import gov.nysenate.sage.model.api.SingleGeocodeRequest;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.provider.geocode.Geocoder;
import gov.nysenate.sage.service.address.AddressServiceProvider;
import gov.nysenate.sage.service.geo.RevGeocodeServiceProvider;
import gov.nysenate.sage.service.geo.SageGeocodeServiceProvider;
import gov.nysenate.sage.util.TimeUtil;
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
import java.sql.Timestamp;
import java.util.List;

import static gov.nysenate.sage.controller.api.filter.ApiFilter.getApiRequest;
import static gov.nysenate.sage.model.result.ResultStatus.*;
import static gov.nysenate.sage.util.controller.ApiControllerUtil.*;

/**
 * Handles Geo Api requests
 */
@Controller
@RequestMapping(value = ConstantUtil.REST_PATH + "geo")
public class GeocodeController {
    private static final Logger logger = LoggerFactory.getLogger(GeocodeController.class);
    private final SageGeocodeServiceProvider geocodeServiceProvider;
    private final RevGeocodeServiceProvider revGeocodeServiceProvider;
    private final AddressServiceProvider addressProvider;
    private final SqlGeocodeRequestLogger sqlGeocodeRequestLogger;
    private final SqlGeocodeResultLogger sqlGeocodeResultLogger;
    private final boolean SINGLE_LOGGING_ENABLED;
    private final boolean BATCH_LOGGING_ENABLED;

    @Autowired
    public GeocodeController(Environment env, SqlGeocodeRequestLogger sqlGeocodeRequestLogger,
                             SqlGeocodeResultLogger sqlGeocodeResultLogger, SageGeocodeServiceProvider geocodeServiceProvider,
                             RevGeocodeServiceProvider revGeocodeServiceProvider, AddressServiceProvider addressProvider) {
        this.sqlGeocodeRequestLogger = sqlGeocodeRequestLogger;
        this.sqlGeocodeResultLogger = sqlGeocodeResultLogger;
        this.geocodeServiceProvider = geocodeServiceProvider;
        this.revGeocodeServiceProvider = revGeocodeServiceProvider;
        this.addressProvider = addressProvider;

        boolean API_LOGGING_ENABLED = env.isApiLoggingEnabled();
        this.SINGLE_LOGGING_ENABLED = API_LOGGING_ENABLED && env.isDetailedLoggingEnabled();
        this.BATCH_LOGGING_ENABLED = API_LOGGING_ENABLED && env.isBatchDetailedLoggingEnabled();
    }

    /**
     * Geocode Api
     * ---------------------------
     *
     * Geocode a single address
     *
     * Usage:
     * (GET)    /api/v2/geo/geocode
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param provider String
     * @param addr String
     * @param addr1 String
     * @param addr2 String
     * @param city String
     * @param state String
     * @param zip5 String
     * @param zip4 String
     * @param uspsValidate boolean
     * @param doNotCache boolean
     * @param useFallback boolean
     */
    @RequestMapping(value = "/geocode", method = RequestMethod.GET)
    public void geocode(HttpServletRequest request, HttpServletResponse response,
                        @RequestParam(required = false) String provider,
                        @RequestParam(required = false) String addr,
                        @RequestParam(required = false) String addr1,
                        @RequestParam(required = false) String addr2,
                        @RequestParam(required = false) String city,
                        @RequestParam(required = false) String state,
                        @RequestParam(required = false) String zip5,
                        @RequestParam(required = false) String zip4,
                        @RequestParam(required = false, defaultValue = "true") boolean useFallback,
                        @RequestParam(required = false, defaultValue = "false") boolean doNotCache,
                        @RequestParam(required = false,  defaultValue = "true") boolean uspsValidate) {

        Object geocodeResponse;
        Timestamp startTime = getCurrentTimeStamp();
        ApiRequest apiRequest = getApiRequest(request);
        boolean useCache = true;

        int requestId = -1;

        /** Construct a GeocodeRequest using the supplied params */
        SingleGeocodeRequest geocodeRequest = new SingleGeocodeRequest(apiRequest,
                getAddressFromParams(addr, addr1, addr2, city, state, zip5, zip4), provider,
                useFallback, useCache, doNotCache, uspsValidate);
        // TODO: normalize address
        geocodeRequest.setAddress(geocodeRequest.getAddress());
        logGeoRequest(apiRequest, geocodeRequest, requestId);

        if (!checkProvider(provider)) {
            geocodeResponse = new ApiError(this.getClass(), PROVIDER_NOT_SUPPORTED);
            setApiResponse(geocodeResponse, request);
            return;
        }

        Address uspsAddress = performAddressCorrection(geocodeRequest.getAddress());
        if (uspsAddress != null) {
            geocodeRequest.setAddress(uspsAddress);
        }

        if (geocodeRequest.getAddress() != null && !geocodeRequest.getAddress().isEmpty()) {
            /** Obtain geocode result */
            GeocodeResult geocodeResult = geocodeServiceProvider.geocode(geocodeRequest);

            /** Construct response from request */
            geocodeResponse = new GeocodeResponse(geocodeResult);

            /** Log geocode request/result to database */
            if (SINGLE_LOGGING_ENABLED && requestId != -1) {
                sqlGeocodeResultLogger.logGeocodeResult(requestId, geocodeResult);
            }
        } else {
            geocodeResponse = new ApiError(this.getClass(), MISSING_ADDRESS);
        }

        logElaspedTime(startTime);
        setApiResponse(geocodeResponse, request);
    }

    /**
     * Reverse Geocode Api
     * ---------------------------
     *
     * Reverse geocode a single pair of latlon coordinates
     *
     * Usage:
     * (GET)    /api/v2/geo/revgeocode
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param provider String
     * @param lat String
     * @param lon String
     * @param doNotCache boolean
     * @param useFallback boolean
     */
    @RequestMapping(value = "/revgeocode", method = RequestMethod.GET)
    public void revGeocode(HttpServletRequest request, HttpServletResponse response,
                           @RequestParam(required = false) String provider,
                           @RequestParam String lat,
                           @RequestParam String lon,
                           @RequestParam(required = false, defaultValue = "true") boolean useFallback,
                           @RequestParam(required = false, defaultValue = "false") boolean doNotCache,
                           @RequestParam(required = false,  defaultValue = "true") boolean uspsValidate) {
        Object geocodeResponse;
        Timestamp startTime = getCurrentTimeStamp();
        ApiRequest apiRequest = getApiRequest(request);
        boolean useCache = true;



        int requestId = -1;

        /** Construct a GeocodeRequest using the supplied params */
        SingleGeocodeRequest geocodeRequest = new SingleGeocodeRequest(apiRequest,
                new Address(), provider, useFallback, useCache, doNotCache, uspsValidate);

        logGeoRequest(apiRequest, geocodeRequest, requestId);

        if (!checkProvider(provider)) {
            geocodeResponse = new ApiError(this.getClass(), PROVIDER_NOT_SUPPORTED);
            setApiResponse(geocodeResponse, request);
            return;
        }

        Point point = getPointFromParams(lat, lon);
        if (point != null) {
            geocodeRequest.setReverse(true);
            geocodeRequest.setPoint(point);
            GeocodeResult revGeocodeResult = revGeocodeServiceProvider.reverseGeocode(geocodeRequest);
            geocodeResponse = new RevGeocodeResponse(revGeocodeResult);

            /** Log rev geocode request/result to database */
            if (SINGLE_LOGGING_ENABLED) {
                sqlGeocodeResultLogger.logGeocodeResult(requestId, revGeocodeResult);
                requestId = -1;
            }
        } else {
            geocodeResponse = new ApiError(this.getClass(), MISSING_POINT);
        }

        logElaspedTime(startTime);
        setApiResponse(geocodeResponse, request);
    }

    /**
     * Batch Geocode Api
     * ---------------------------
     *
     * Geocode a batch of addresses
     *
     * Usage:
     * (POST)    /api/v2/geo/geocode/batch
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param provider String
     * @param doNotCache boolean
     * @param useFallback boolean
     */
    @RequestMapping(value = "/geocode/batch", method = RequestMethod.POST)
    public void batchGeocode(HttpServletRequest request, HttpServletResponse response,
                             @RequestParam(required = false) String provider,
                             @RequestParam(required = false, defaultValue = "true") boolean useFallback,
                             @RequestParam(required = false, defaultValue = "false") boolean doNotCache,
                             @RequestParam(required = false,  defaultValue = "true") boolean uspsValidate) throws IOException {

        Object geocodeResponse = new ApiError(this.getClass(), SERVICE_NOT_SUPPORTED);
        Timestamp startTime = getCurrentTimeStamp();
        ApiRequest apiRequest = getApiRequest(request);
        boolean useCache = true;

        int requestId = -1;

        /** Construct a GeocodeRequest using the supplied params */
        SingleGeocodeRequest geocodeRequest = new SingleGeocodeRequest(apiRequest,
                new Address(), provider, useFallback, useCache, doNotCache, uspsValidate);
        logGeoRequest(apiRequest, geocodeRequest, requestId);

        if (!checkProvider(provider)) {
            geocodeResponse = new ApiError(this.getClass(), PROVIDER_NOT_SUPPORTED);
            setApiResponse(geocodeResponse, request);
            return;
        }

        String batchJsonPayload = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
        List<Address> addresses = getAddressesFromJsonBody(batchJsonPayload);
        if (!addresses.isEmpty()) {
            var batchGeocodeRequest = new BatchGeocodeRequest(geocodeRequest);
            batchGeocodeRequest.setAddresses(addresses);

            List<GeocodeResult> geocodeResults = geocodeServiceProvider.geocode(batchGeocodeRequest);
            geocodeResponse = new BatchGeocodeResponse(geocodeResults);

            if (BATCH_LOGGING_ENABLED) {
                sqlGeocodeResultLogger.logBatchGeocodeResults(batchGeocodeRequest, geocodeResults, true);
            }
        } else {
            geocodeResponse = new ApiError(this.getClass(), INVALID_BATCH_ADDRESSES);
        }

        logElaspedTime(startTime);
        setApiResponse(geocodeResponse, request);
    }

    /**
     * Batch Reverse Geocode Api
     * ---------------------------
     * Reverse geocode a batch of latlon coordinates
     * Usage:
     * (POST)    /api/v2/geo/revgeocode/batch
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param provider String
     * @param doNotCache boolean
     * @param useFallback boolean
     */
    @RequestMapping(value = "/revgeocode/batch", method = RequestMethod.POST)
    public void batchRevGeocode(HttpServletRequest request, HttpServletResponse response,
                                @RequestParam(required = false) String provider,
                                @RequestParam(required = false, defaultValue = "true") boolean useFallback,
                                @RequestParam(required = false, defaultValue = "false") boolean doNotCache,
                                @RequestParam(required = false,  defaultValue = "true") boolean uspsValidate) throws IOException {

        Object geocodeResponse = new ApiError(this.getClass(), SERVICE_NOT_SUPPORTED);
        Timestamp startTime = getCurrentTimeStamp();
        ApiRequest apiRequest = getApiRequest(request);
        boolean useCache = true;

        int requestId = -1;

        /** Construct a GeocodeRequest using the supplied params */
        SingleGeocodeRequest geocodeRequest = new SingleGeocodeRequest(apiRequest,
                new Address(), provider, useFallback, useCache, doNotCache, uspsValidate);
        logGeoRequest(apiRequest, geocodeRequest, requestId);

        if (!checkProvider(provider)) {
            geocodeResponse = new ApiError(this.getClass(), PROVIDER_NOT_SUPPORTED);
            setApiResponse(geocodeResponse, request);
            return;
        }

        String batchJsonPayload = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
        List<Point> points = getPointsFromJsonBody(batchJsonPayload);
        if (!points.isEmpty()) {
            BatchGeocodeRequest batchGeocodeRequest = new BatchGeocodeRequest(geocodeRequest);
            batchGeocodeRequest.setReverse(true);
            batchGeocodeRequest.setPoints(points);

            List<GeocodeResult> revGeocodeResults = revGeocodeServiceProvider.reverseGeocode(points, Geocoder.valueOf(provider.toUpperCase().trim()));
            geocodeResponse = new BatchGeocodeResponse(revGeocodeResults);

            if (BATCH_LOGGING_ENABLED) {
                sqlGeocodeResultLogger.logBatchGeocodeResults(batchGeocodeRequest, revGeocodeResults, true);
            }
        } else {
            geocodeResponse = new ApiError(this.getClass(), INVALID_BATCH_POINTS);
        }

        logElaspedTime(startTime);
        setApiResponse(geocodeResponse, request);

    }

    private void logGeoRequest(ApiRequest apiRequest, SingleGeocodeRequest geocodeRequest, int requestId) {
        logger.info("=======================================================");
        logger.info("|{}Geocode Request {} ", (apiRequest.isBatch() ? " Batch " : " "), apiRequest.getId());
        logger.info("| Mode: {} | IP: {} | Provider: {}", apiRequest.getRequest(), apiRequest.getIpAddress(), apiRequest.getProvider());
        if (!apiRequest.isBatch() && apiRequest.getRequest().equals("geocode") || apiRequest.getRequest().equals("geocache")) {
            logger.info("| Input Address: {}", geocodeRequest.getAddress().toLogString());
        }
        logger.info("=======================================================");

        if (SINGLE_LOGGING_ENABLED) {
            requestId = sqlGeocodeRequestLogger.logGeocodeRequest(geocodeRequest);
        }
    }

    private boolean checkProvider(String provider) {
        /**
         * If provider is specified then make sure it matches the available providers. Send an
         * api error and return if the provider is not supported.
         */
        if (provider != null && !provider.isEmpty()) {
            try {
                Geocoder.valueOf(provider.toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        return true;
    }

    private void logElaspedTime(Timestamp startTime) {
        logger.info("Geo Response in {} ms.", TimeUtil.getElapsedMs(startTime));
    }

    private Timestamp getCurrentTimeStamp() {
        return TimeUtil.currentTimestamp();
    }


    /**
     * Perform USPS address correction on either the geocoded address or the input address.
     * If the geocoded address is invalid, the original address will be corrected and set as the address
     * on the supplied geocodedAddress parameter.
     *
     * @return GeocodedAddress the address corrected geocodedAddress.
     */
    private Address performAddressCorrection(Address address) {
        AddressResult addressResult = addressProvider.validate(address, null, false);
        if (addressResult != null && addressResult.isValidated()) {
            if (logger.isTraceEnabled()) {
                logger.trace("USPS Validated Address: " + addressResult.getAddress().toLogString());
            }
            return addressResult.getAddress();
        }
        return null;
    }
}
