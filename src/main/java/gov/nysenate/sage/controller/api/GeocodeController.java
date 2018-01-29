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
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

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

/** Handles Geo Api requests */
@Controller
public class GeocodeController extends BaseApiController implements Observer
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
    }

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        logger.debug("Initialized " + this.getClass().getSimpleName());
        update(null, null);
    }

    @Override
    public void update(Observable o, Object arg) {
        boolean API_LOGGING_ENABLED = env.isApiLoggingEnabled();
        SINGLE_LOGGING_ENABLED = API_LOGGING_ENABLED && env.isDetailedLoggingEnabled();
        BATCH_LOGGING_ENABLED = API_LOGGING_ENABLED && env.isBatchDetailedLoggingEnabled();
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
        Timestamp startTime = TimeUtil.currentTimestamp();

        /** Get the ApiRequest */
        ApiRequest apiRequest = getApiRequest(request);
        String provider = apiRequest.getProvider();

        /** Check whether or not to fallback */
        boolean useFallback = requestParameterEquals(request, "useFallback", "false") ? false : true;

        /** Only want to use cache when the provider is not specified */
        boolean useCache = (provider == null);

        int requestId = -1;

        /** Construct a GeocodeRequest using the supplied params */
        GeocodeRequest geocodeRequest = new GeocodeRequest(apiRequest, getAddressFromParams(request), provider, useFallback, useCache);


        logger.info("=======================================================");
        logger.info(String.format("|%sGeocode Request %d ", (apiRequest.isBatch() ? " Batch " : " "), apiRequest.getId()));
        logger.info(String.format("| Mode: %s | IP: %s | Provider: %s", apiRequest.getRequest(), apiRequest.getIpAddress(), apiRequest.getProvider()));
        if (!apiRequest.isBatch() && apiRequest.getRequest().equals("geocode")) {
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
        if (provider != null && !provider.isEmpty() && !geocodeServiceProvider.isRegistered(provider)) {
            geocodeResponse = new ApiError(this.getClass(), PROVIDER_NOT_SUPPORTED);
            setApiResponse(geocodeResponse, request);
            return;
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
            default: {
                geocodeResponse = new ApiError(this.getClass(), SERVICE_NOT_SUPPORTED);
            }
        }

        logger.info(String.format("Geo Response in %s ms.", TimeUtil.getElapsedMs(startTime)));

        /** Set response */
        setApiResponse(geocodeResponse, request);
    }
}
