package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.client.response.geo.BatchGeocodeResponse;
import gov.nysenate.sage.client.response.geo.GeocodeResponse;
import gov.nysenate.sage.client.response.geo.RevGeocodeResponse;
import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.dao.logger.geocode.SqlGeocodeRequestLogger;
import gov.nysenate.sage.dao.logger.geocode.SqlGeocodeResultLogger;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.model.api.ApiRequest;
import gov.nysenate.sage.model.api.BatchGeocodeRequest;
import gov.nysenate.sage.model.api.GeocodeRequest;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.service.address.AddressServiceProvider;
import gov.nysenate.sage.service.geo.GeocodeServiceProvider;
import gov.nysenate.sage.service.geo.RevGeocodeServiceProvider;
import gov.nysenate.sage.util.StreetAddressParser;
import gov.nysenate.sage.util.TimeUtil;
import gov.nysenate.sage.util.controller.ConstantUtil;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

import static gov.nysenate.sage.filter.ApiFilter.getApiRequest;
import static gov.nysenate.sage.model.result.ResultStatus.*;
import static gov.nysenate.sage.util.controller.ApiControllerUtil.*;

/**
 * Handles Geo Api requests
 */
@Controller
@RequestMapping(value = ConstantUtil.REST_PATH + "geo")
public class GeocodeController {
    private static Logger logger = LoggerFactory.getLogger(GeocodeController.class);
    private GeocodeServiceProvider geocodeServiceProvider;
    private RevGeocodeServiceProvider revGeocodeServiceProvider;
    private AddressServiceProvider addressProvider;

    /**
     * Usage loggers
     */
    private static Boolean SINGLE_LOGGING_ENABLED = false;
    private static Boolean BATCH_LOGGING_ENABLED = false;
    private static SqlGeocodeRequestLogger sqlGeocodeRequestLogger;
    private static SqlGeocodeResultLogger sqlGeocodeResultLogger;
    private final Environment env;

    @Autowired
    public GeocodeController(Environment env, SqlGeocodeRequestLogger sqlGeocodeRequestLogger,
                             SqlGeocodeResultLogger sqlGeocodeResultLogger, GeocodeServiceProvider geocodeServiceProvider,
                             RevGeocodeServiceProvider revGeocodeServiceProvider, AddressServiceProvider addressProvider) {
        this.env = env;
        this.sqlGeocodeRequestLogger = sqlGeocodeRequestLogger;
        this.sqlGeocodeResultLogger = sqlGeocodeResultLogger;
        this.geocodeServiceProvider = geocodeServiceProvider;
        this.revGeocodeServiceProvider = revGeocodeServiceProvider;
        this.addressProvider = addressProvider;

        logger.debug("Initialized " + this.getClass().getSimpleName());
        boolean API_LOGGING_ENABLED = env.isApiLoggingEnabled();
        SINGLE_LOGGING_ENABLED = API_LOGGING_ENABLED && env.isDetailedLoggingEnabled();
        BATCH_LOGGING_ENABLED = API_LOGGING_ENABLED && env.isBatchDetailedLoggingEnabled();
    }

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
                        @RequestParam(required = false) boolean useFallback,
                        @RequestParam(required = false) boolean doNotCache,
                        @RequestParam(required = false) boolean bypassCache,
                        @RequestParam(required = false) boolean uspsValidate) {

        Object geocodeResponse = new ApiError(this.getClass(), SERVICE_NOT_SUPPORTED);
        Timestamp startTime = getCurrentTimeStamp();
        ApiRequest apiRequest = getApiRequest(request);
        Boolean useCache = true;

        determineCacheProviderProps(useCache, bypassCache, doNotCache, provider);

        int requestId = -1;

        /** Construct a GeocodeRequest using the supplied params */
        GeocodeRequest geocodeRequest = new GeocodeRequest(apiRequest,
                getAddressFromParams(addr, addr1, addr2, city, state, zip5, zip4), provider,
                useFallback, useCache, bypassCache, doNotCache, uspsValidate);

        StreetAddress inputStreetAddress = StreetAddressParser.parseAddress(geocodeRequest.getAddress());
        Address reorderdAddress = inputStreetAddress.toAddress();
        geocodeRequest.setAddress(reorderdAddress);


        logGeoRequest(apiRequest, geocodeRequest, requestId);

        if (!checkProvider(provider, geocodeResponse, request)) {
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

    @RequestMapping(value = "/revgeocode", method = RequestMethod.GET)
    public void revGeocode(HttpServletRequest request, HttpServletResponse response,
                           @RequestParam(required = false) String provider,
                           @RequestParam String lat,
                           @RequestParam String lon,
                           @RequestParam(required = false) boolean useFallback,
                           @RequestParam(required = false) boolean doNotCache,
                           @RequestParam(required = false) boolean bypassCache) {
        Object geocodeResponse = new ApiError(this.getClass(), SERVICE_NOT_SUPPORTED);
        Timestamp startTime = getCurrentTimeStamp();
        ApiRequest apiRequest = getApiRequest(request);
        Boolean useCache = true;

        determineCacheProviderProps(useCache, bypassCache, doNotCache, provider);

        int requestId = -1;

        /** Construct a GeocodeRequest using the supplied params */
        GeocodeRequest geocodeRequest = new GeocodeRequest(apiRequest,
                new Address(), provider, useFallback, useCache, bypassCache, doNotCache);

        logGeoRequest(apiRequest, geocodeRequest, requestId);

        if (!checkProvider(provider, geocodeResponse, request)) {
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

    @RequestMapping(value = "/geocode/batch", method = RequestMethod.POST)
    public void batchGeocode(HttpServletRequest request, HttpServletResponse response,
                             @RequestParam(required = false) String provider,
                             @RequestParam(required = false) boolean useFallback,
                             @RequestParam(required = false) boolean doNotCache,
                             @RequestParam(required = false) boolean bypassCache) throws IOException {

        Object geocodeResponse = new ApiError(this.getClass(), SERVICE_NOT_SUPPORTED);
        Timestamp startTime = getCurrentTimeStamp();
        ApiRequest apiRequest = getApiRequest(request);
        Boolean useCache = true;

        determineCacheProviderProps(useCache, bypassCache, doNotCache, provider);

        int requestId = -1;

        /** Construct a GeocodeRequest using the supplied params */
        GeocodeRequest geocodeRequest = new GeocodeRequest(apiRequest,
                new Address(), provider, useFallback, useCache, bypassCache, doNotCache);

        logGeoRequest(apiRequest, geocodeRequest, requestId);

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
                sqlGeocodeResultLogger.logBatchGeocodeResults(batchGeocodeRequest, geocodeResults, true);
            }
        } else {
            geocodeResponse = new ApiError(this.getClass(), INVALID_BATCH_ADDRESSES);
        }

        logElaspedTime(startTime);
        setApiResponse(geocodeResponse, request);
    }

    @RequestMapping(value = "/revgeocode/batch", method = RequestMethod.POST)
    public void batchRevGeocode(HttpServletRequest request, HttpServletResponse response,
                                @RequestParam(required = false) String provider,
                                @RequestParam(required = false) boolean useFallback,
                                @RequestParam(required = false) boolean doNotCache,
                                @RequestParam(required = false) boolean bypassCache) throws IOException {

        Object geocodeResponse = new ApiError(this.getClass(), SERVICE_NOT_SUPPORTED);
        Timestamp startTime = getCurrentTimeStamp();
        ApiRequest apiRequest = getApiRequest(request);
        Boolean useCache = true;

        determineCacheProviderProps(useCache, bypassCache, doNotCache, provider);

        int requestId = -1;

        /** Construct a GeocodeRequest using the supplied params */
        GeocodeRequest geocodeRequest = new GeocodeRequest(apiRequest,
                new Address(), provider, useFallback, useCache, bypassCache, doNotCache);

        logGeoRequest(apiRequest, geocodeRequest, requestId);

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
                sqlGeocodeResultLogger.logBatchGeocodeResults(batchGeocodeRequest, revGeocodeResults, true);
            }
        } else {
            geocodeResponse = new ApiError(this.getClass(), INVALID_BATCH_POINTS);
        }

        logElaspedTime(startTime);
        setApiResponse(geocodeResponse, request);

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
            requestId = sqlGeocodeRequestLogger.logGeocodeRequest(geocodeRequest);
        }
    }

    private boolean checkProvider(String provider, Object geocodeResponse, HttpServletRequest request) {
        /**
         * If provider is specified then make sure it matches the available providers. Send an
         * api error and return if the provider is not supported.
         */
        if (provider != null && !provider.isEmpty() &&
                !geocodeServiceProvider.getActiveGeoProviders().containsKey(provider) && !provider.equals("geocache")) {
            geocodeResponse = new ApiError(this.getClass(), PROVIDER_NOT_SUPPORTED);
            setApiResponse(geocodeResponse, request);
            return false;
        }
        return true;
    }

    private void determineCacheProviderProps(boolean useCache, boolean bypassCache, boolean doNotCache,
                                             String provider) {
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
                logger.trace("USPS Validated Address: " + addressResult.getAdressLogString());
            }
            return addressResult.getAddress();
        }
        return null;
    }
}
