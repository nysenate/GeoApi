package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.client.response.geo.BatchGeocodeResponse;
import gov.nysenate.sage.client.response.geo.GeocodeResponse;
import gov.nysenate.sage.client.response.geo.RevGeocodeResponse;
import gov.nysenate.sage.dao.logger.GeocodeRequestLogger;
import gov.nysenate.sage.dao.logger.GeocodeResultLogger;
import gov.nysenate.sage.factory.ApplicationFactory;
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
import gov.nysenate.sage.util.Config;
import gov.nysenate.sage.util.StreetAddressParser;
import gov.nysenate.sage.util.TimeUtil;
import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import static gov.nysenate.sage.model.result.ResultStatus.*;
import static gov.nysenate.sage.scripts.ProcessBatchJobs.addressProvider;

/** Handles Geo Api requests */
public class GeocodeController extends BaseApiController implements Observer
{
    private static Logger logger = LoggerFactory.getLogger(GeocodeController.class);
    private static Config config = ApplicationFactory.getConfig();
    private static GeocodeServiceProvider geocodeServiceProvider = ApplicationFactory.getGeocodeServiceProvider();
    private static RevGeocodeServiceProvider revGeocodeServiceProvider = ApplicationFactory.getRevGeocodeServiceProvider();
    public static AddressServiceProvider addressProvider = ApplicationFactory.getAddressServiceProvider();

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
        Object geocodeResponse = new ApiError(this.getClass(), SERVICE_NOT_SUPPORTED);
        Timestamp startTime = TimeUtil.currentTimestamp();

        /** Get the ApiRequest */
        ApiRequest apiRequest = getApiRequest(request);
        String provider = apiRequest.getProvider();

        /** Check whether or not to fallback */
        boolean useFallback = requestParameterEquals(request, "useFallback", "false") ? false : true;

        boolean bypassCache = requestParameterEquals(request, "bypassCache", "false") ? false : true;

        boolean doNotCache = requestParameterEquals(request, "doNotCache", "false") ? true : false;

        boolean isUspsValidate = requestParameterEquals(request, "uspsValidate", "false") ? false : true;


        /** Only want to use cache when the provider is not specified */
        boolean useCache = (provider == null);
        if (bypassCache || doNotCache) {
            useCache = false;
        }
        if (provider != null && provider.equals("geocache")) {
            useCache = true;
        }


        int requestId = -1;

        /** Construct a GeocodeRequest using the supplied params */
        GeocodeRequest geocodeRequest = new GeocodeRequest(apiRequest, getAddressFromParams(request), provider, useFallback, useCache, bypassCache, doNotCache, isUspsValidate);

        StreetAddress inputStreetAddress = StreetAddressParser.parseAddress(geocodeRequest.getAddress());
        Address reorderdAddress = inputStreetAddress.toAddress();
        geocodeRequest.setAddress(reorderdAddress);


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

        /**
         * If provider is specified then make sure it matches the available providers. Send an
         * api error and return if the provider is not supported.
         */
        if (provider != null && !provider.isEmpty() && !geocodeServiceProvider.isRegistered(provider) && !provider.equals("geocache")) {
            geocodeResponse = new ApiError(this.getClass(), PROVIDER_NOT_SUPPORTED);
            setApiResponse(geocodeResponse, request);
            return;
        }

        //usps validation
        Address validatedAddress = null;
        /** Parse the input address */
        StreetAddress streetAddress = null;
        try {
            streetAddress = StreetAddressParser.parseAddress(geocodeRequest.getAddress());
        }
        catch (Exception ex) {
            logger.debug("Failed to parse input address" + geocodeRequest.getAddress().toLogString());
        }

        /** This info about the address helps to decide how to process it */
        Address inputAddress = geocodeRequest.getAddress();
        if (inputAddress != null && !inputAddress.isEmpty()) {
            /** Perform usps address correction if requested */
            if (geocodeRequest.isUspsValidate()) {
                Address addressToValidate = null;
                if (inputAddress.isEligibleForUSPS()) {
                    addressToValidate = inputAddress;
                } else if (streetAddress != null) {
                    addressToValidate = streetAddress.toAddress();
                }
                if (addressToValidate != null) {
                    validatedAddress = performAddressCorrection(addressToValidate, geocodeRequest);
                    geocodeRequest.setAddress(validatedAddress);
                }
            }
        }

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
                        if (SINGLE_LOGGING_ENABLED && requestId != -1) {
                            geocodeResultLogger.logGeocodeResult(requestId, geocodeResult);
                            requestId = -1;
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
                            geocodeResultLogger.logGeocodeResult(requestId, revGeocodeResult);
                            requestId = -1;
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
        }

        logger.info(String.format("Geo Response in %s ms.", TimeUtil.getElapsedMs(startTime)));

        /** Set response */
        setApiResponse(geocodeResponse, request);
    }

    /**
     * Perform USPS address correction on either the geocoded address or the input address.
     * If the geocoded address is invalid, the original address will be corrected and set as the address
     * on the supplied geocodedAddress parameter.
     * @return GeocodedAddress the address corrected geocodedAddress.
     */
    private Address performAddressCorrection(Address address, GeocodeRequest geocodeRequest)
    {
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
