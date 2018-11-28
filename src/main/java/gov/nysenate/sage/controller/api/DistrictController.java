package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.client.response.district.*;
import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.dao.logger.SqlDistrictRequestLogger;
import gov.nysenate.sage.dao.logger.SqlDistrictResultLogger;
import gov.nysenate.sage.dao.logger.SqlGeocodeRequestLogger;
import gov.nysenate.sage.dao.logger.SqlGeocodeResultLogger;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.model.api.*;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.service.address.AddressServiceProvider;
import gov.nysenate.sage.service.district.DistrictMemberProvider;
import gov.nysenate.sage.service.district.DistrictServiceProvider;
import gov.nysenate.sage.service.geo.GeocodeServiceProvider;
import gov.nysenate.sage.service.geo.RevGeocodeServiceProvider;
import gov.nysenate.sage.service.map.MapServiceProvider;
import gov.nysenate.sage.util.FormatUtil;
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
import java.util.*;

import static gov.nysenate.sage.filter.ApiFilter.getApiRequest;
import static gov.nysenate.sage.model.result.ResultStatus.*;
import static gov.nysenate.sage.service.district.DistrictServiceProvider.DistrictStrategy;
import static gov.nysenate.sage.util.controller.ApiControllerUtil.*;

/** Handles District Api requests */
@Controller
@RequestMapping(value = ConstantUtil.REST_PATH + "district")
public class DistrictController
{
    private static Logger logger = LoggerFactory.getLogger(DistrictController.class);

    /** Service Providers */
    private AddressServiceProvider addressProvider;
    private DistrictServiceProvider districtProvider;
    private GeocodeServiceProvider geocodeProvider;
    private RevGeocodeServiceProvider revGeocodeProvider;
    private MapServiceProvider mapProvider;
    private DistrictMemberProvider districtMemberProvider;


    /** Loggers */
    private SqlGeocodeRequestLogger sqlGeocodeRequestLogger;
    private SqlGeocodeResultLogger sqlGeocodeResultLogger;
    private SqlDistrictRequestLogger sqlDistrictRequestLogger;
    private SqlDistrictResultLogger sqlDistrictResultLogger;

    private static String BLUEBIRD_DISTRICT_STRATEGY;

    private final Environment env;

    private static Boolean SINGLE_LOGGING_ENABLED = false;
    private static Boolean BATCH_LOGGING_ENABLED = false;

    @Autowired
    public DistrictController(Environment env, AddressServiceProvider addressProvider, DistrictServiceProvider districtProvider,
                              RevGeocodeServiceProvider revGeocodeProvider, MapServiceProvider mapProvider,
                              SqlGeocodeRequestLogger sqlGeocodeRequestLogger, SqlGeocodeResultLogger sqlGeocodeResultLogger,
                              SqlDistrictRequestLogger sqlDistrictRequestLogger, SqlDistrictResultLogger sqlDistrictResultLogger,
                              DistrictMemberProvider districtMemberProvider, GeocodeServiceProvider geocodeProvider) {

        this.addressProvider = addressProvider;
        this.districtProvider = districtProvider;
        this.geocodeProvider = geocodeProvider;
        this.revGeocodeProvider = revGeocodeProvider;
        this.mapProvider = mapProvider;
        this.districtMemberProvider = districtMemberProvider;
        this.env = env;
        this.sqlGeocodeRequestLogger = sqlGeocodeRequestLogger;
        this.sqlGeocodeResultLogger = sqlGeocodeResultLogger;
        this.sqlDistrictRequestLogger = sqlDistrictRequestLogger;
        this.sqlDistrictResultLogger = sqlDistrictResultLogger;

        BLUEBIRD_DISTRICT_STRATEGY = env.getDistrictStrategyBluebird();
        boolean API_LOGGING_ENABLED = env.isApiLoggingEnabled();
        SINGLE_LOGGING_ENABLED = API_LOGGING_ENABLED && env.isDetailedLoggingEnabled();
        BATCH_LOGGING_ENABLED = API_LOGGING_ENABLED && env.isBatchDetailedLoggingEnabled();
    }

    @RequestMapping(value = "/assign", method = RequestMethod.GET)
    public void districtAssign(HttpServletRequest request, HttpServletResponse response,
                                @RequestParam String provider, @RequestParam String geoProvider,
                                @RequestParam boolean showMembers, @RequestParam boolean showMaps,
                                @RequestParam boolean uspsValidate, @RequestParam boolean skipGeocode,
                                @RequestParam boolean showMultiMatch, @RequestParam String districtStrategy,
                                @RequestParam boolean usePunct, @RequestParam String lat, @RequestParam String lon,
                                @RequestParam String addr, @RequestParam String addr1, @RequestParam String addr2,
                                @RequestParam String city, @RequestParam String state, @RequestParam String zip5,
                                @RequestParam String zip4) {
        Object districtResponse = null;
        Timestamp startTime = getCurrentTimeStamp();
        int requestId = -1;

        /** Get the ApiRequest */
        ApiRequest apiRequest = getApiRequest(request);

        DistrictRequest districtRequest = new DistrictRequest();
        districtRequest.setApiRequest(apiRequest);
        districtRequest.setAddress(getAddressFromParams(addr,addr1,addr2,city,state,zip5,zip4));
        districtRequest.setPoint(getPointFromParams(lat,lon));
        districtRequest.setProvider(provider);
        districtRequest.setGeoProvider(geoProvider);
        districtRequest.setShowMembers(showMembers);
        districtRequest.setShowMaps(showMaps);
        districtRequest.setUspsValidate(uspsValidate);
        districtRequest.setUsePunct(usePunct);
        districtRequest.setSkipGeocode(skipGeocode);
        districtRequest.setRequestTime(new Timestamp(new Date().getTime()));
        districtRequest.setDistrictStrategy(districtStrategy);

        Address reorderdAddress = StreetAddressParser.parseAddress(districtRequest.getAddress()).toAddress();
        if (reorderdAddress.getState().isEmpty()) {
            reorderdAddress.setState("NY");
        }
        districtRequest.setAddress(reorderdAddress);

        logDistrictRequest(apiRequest, districtRequest, requestId);

        if(!checkProviders(provider, geoProvider, request)) {
            return;
        }

        DistrictResult districtResult = handleDistrictRequest(districtRequest,requestId);
        if (districtResult.isMultiMatch() && showMultiMatch) {
            districtResponse = (showMaps) ? new MappedMultiDistrictResponse(districtResult) : new MultiDistrictResponse(districtResult);
        }
        else {
            districtResponse = (showMaps) ? new MappedDistrictResponse(districtResult) : new DistrictResponse(districtResult);
        }


        setApiResponse(districtResponse, request);

        logElapsedTime(startTime, apiRequest);
    }

    @RequestMapping(value = "/batch/assign", method = RequestMethod.GET)
    public void districtBatchAssign(HttpServletRequest request,
                                @RequestParam String provider, @RequestParam String geoProvider,
                                @RequestParam boolean showMembers, @RequestParam boolean showMaps,
                                @RequestParam boolean uspsValidate, @RequestParam boolean skipGeocode,
                                    @RequestParam String districtStrategy, @RequestParam boolean usePunct)
            throws IOException {
        Object districtResponse = null;
        Timestamp startTime = getCurrentTimeStamp();
        int requestId = -1;

        /** Get the ApiRequest */
        ApiRequest apiRequest = getApiRequest(request);


        DistrictRequest districtRequest = new DistrictRequest();
        districtRequest.setApiRequest(apiRequest);
        districtRequest.setProvider(provider);
        districtRequest.setGeoProvider(geoProvider);
        districtRequest.setShowMembers(showMembers);
        districtRequest.setShowMaps(showMaps);
        districtRequest.setUspsValidate(uspsValidate);
        districtRequest.setUsePunct(usePunct);
        districtRequest.setSkipGeocode(skipGeocode);
        districtRequest.setRequestTime(startTime);
        districtRequest.setDistrictStrategy(districtStrategy);

        logDistrictRequest(apiRequest, districtRequest, requestId);

        if(!checkProviders(provider, geoProvider, request)) {
            return;
        }

        String batchJsonPayload = IOUtils.toString(request.getInputStream(), "UTF-8");
        List<Address> addresses;
        List<Point> points = new ArrayList<>();
        addresses = getAddressesFromJsonBody(batchJsonPayload);
        if (addresses.isEmpty()) {
            points = getPointsFromJsonBody(batchJsonPayload);
        }
        if (!addresses.isEmpty() || !points.isEmpty()) {
            BatchDistrictRequest batchDistrictRequest = new BatchDistrictRequest(districtRequest);
            batchDistrictRequest.setAddresses(addresses);
            batchDistrictRequest.setPoints(points);

            List<DistrictResult> districtResults = handleBatchDistrictRequest(batchDistrictRequest);
            districtResponse = new BatchDistrictResponse(districtResults);
        }
        else {
            districtResponse = new ApiError(this.getClass(), INVALID_BATCH_ADDRESSES);
        }

        setApiResponse(districtResponse, request);

        logElapsedTime(startTime, apiRequest);
    }

    @RequestMapping(value = "/bluebird", method = RequestMethod.GET)
    public void bluebirdAssign(HttpServletRequest request, HttpServletResponse response,
                               @RequestParam String provider, @RequestParam String geoProvider,
                               @RequestParam boolean usePunct, @RequestParam String lat, @RequestParam String lon,
                               @RequestParam String addr, @RequestParam String addr1, @RequestParam String addr2,
                               @RequestParam String city, @RequestParam String state, @RequestParam String zip5,
                               @RequestParam String zip4) {
        Object districtResponse = null;
        Timestamp startTime = getCurrentTimeStamp();
        int requestId = -1;

        /** Get the ApiRequest */
        ApiRequest apiRequest = getApiRequest(request);

        DistrictRequest districtRequest = new DistrictRequest();
        districtRequest.setApiRequest(apiRequest);
        districtRequest.setAddress(getAddressFromParams(addr,addr1,addr2,city,state,zip5,zip4));
        districtRequest.setPoint(getPointFromParams(lat,lon));
        districtRequest.setProvider(provider);
        districtRequest.setGeoProvider(geoProvider);
        districtRequest.setUsePunct(usePunct);
        districtRequest.setRequestTime(startTime);

        Address reorderdAddress = StreetAddressParser.parseAddress(districtRequest.getAddress()).toAddress();
        if (reorderdAddress.getState().isEmpty()) {
            reorderdAddress.setState("NY");
        }
        districtRequest.setAddress(reorderdAddress);

        logDistrictRequest(apiRequest, districtRequest, requestId);

        if(!checkProviders(provider, geoProvider, request)) {
            return;
        }

        DistrictRequest bluebirdRequest = DistrictRequest.buildBluebirdRequest(districtRequest, BLUEBIRD_DISTRICT_STRATEGY);
        DistrictResult districtResult = handleDistrictRequest(bluebirdRequest, requestId);
        districtResponse = new DistrictResponse(districtResult);

        setApiResponse(districtResponse, request);

        logElapsedTime(startTime, apiRequest);
    }

    @RequestMapping(value = "/batch/bluebird", method = RequestMethod.GET)
    public void bluebirdBatchAssign(HttpServletRequest request, HttpServletResponse response,
                                    @RequestParam String provider, @RequestParam String geoProvider,
                                    @RequestParam boolean usePunct) throws IOException {
        Object districtResponse = null;
        Timestamp startTime = getCurrentTimeStamp();
        int requestId = -1;

        /** Get the ApiRequest */
        ApiRequest apiRequest = getApiRequest(request);

        DistrictRequest districtRequest = new DistrictRequest();
        districtRequest.setApiRequest(apiRequest);
        districtRequest.setProvider(provider);
        districtRequest.setGeoProvider(geoProvider);
        districtRequest.setUsePunct(usePunct);
        districtRequest.setRequestTime(startTime);

        logDistrictRequest(apiRequest, districtRequest, requestId);

        if(!checkProviders(provider, geoProvider, request)) {
            return;
        }

        String batchJsonPayload = IOUtils.toString(request.getInputStream(), "UTF-8");
        List<Address> addresses = getAddressesFromJsonBody(batchJsonPayload);
        List<Point> points = new ArrayList<>();
        if (addresses.isEmpty()) {
            points = getPointsFromJsonBody(batchJsonPayload);
        }
        if (!addresses.isEmpty() || !points.isEmpty()) {
            DistrictRequest bluebirdRequest = DistrictRequest.buildBluebirdRequest(districtRequest, BLUEBIRD_DISTRICT_STRATEGY);
            BatchDistrictRequest batchBluebirdRequest = new BatchDistrictRequest(bluebirdRequest);
            batchBluebirdRequest.setAddresses(addresses);
            batchBluebirdRequest.setPoints(points);

            List<DistrictResult> districtResults = handleBatchDistrictRequest(batchBluebirdRequest);
            districtResponse = new BatchDistrictResponse(districtResults);
        }
        else {
            districtResponse = new ApiError(this.getClass(), INVALID_BATCH_ADDRESSES);
        }

        setApiResponse(districtResponse, request);

        logElapsedTime(startTime, apiRequest);
    }

    private Timestamp getCurrentTimeStamp() {
        return TimeUtil.currentTimestamp();
    }

    private void logDistrictRequest(ApiRequest apiRequest, DistrictRequest districtRequest , int requestId) {
        logger.info("=======================================================");
        logger.info(String.format("|%sDistrict '%s' Request %d ", (apiRequest.isBatch() ? " Batch " : " "), apiRequest.getRequest(), apiRequest.getId()));
        logger.info(String.format("| IP: %s | Maps: %s | Members: %s", apiRequest.getIpAddress(), districtRequest.isShowMaps(), districtRequest.isShowMembers()));
        if (!apiRequest.isBatch()) {
            logger.info("| Input Address: " + districtRequest.getAdressLogString());
        }
        logger.info("=======================================================");

        if (SINGLE_LOGGING_ENABLED) {
            requestId = sqlDistrictRequestLogger.logDistrictRequest(districtRequest);
        }
    }

    private boolean checkProviders(String provider, String geoProvider, HttpServletRequest request) {
        /**
         * If providers are specified then make sure they match the available providers. Send an
         * api error and return if the provider is not supported.
         */
        boolean validProviders = true;
        if (provider != null && !provider.isEmpty() && !districtProvider.isRegistered(provider)) {
            setApiResponse(new ApiError(this.getClass(), DISTRICT_PROVIDER_NOT_SUPPORTED), request);
            validProviders = false;
        }
        if (geoProvider != null && !geoProvider.isEmpty() && !geocodeProvider.isRegistered(geoProvider)) {
            setApiResponse(new ApiError(this.getClass(), GEOCODE_PROVIDER_NOT_SUPPORTED), request);
            validProviders = false;
        }
        return validProviders;
    }

    private void logElapsedTime(Timestamp startTime, ApiRequest apiRequest) {
        long elapsedTimeMs = TimeUtil.getElapsedMs(startTime);
        logger.info(String.format("%sDistrict Response %d sent in %d ms.",
                (apiRequest.isBatch() ? " Batch " : " "), apiRequest.getId(), elapsedTimeMs));
    }

    /**
     * Handle district assignment requests and performs functions based on settings in the supplied DistrictRequest.
     * @param districtRequest   Contains the various parameters for the District Assign/Bluebird API
     * @return  DistrictResult
     */
    private DistrictResult handleDistrictRequest(DistrictRequest districtRequest, int requestId)
    {
        Address address = districtRequest.getAddress();
        Point point = districtRequest.getPoint();
        DistrictResult districtResult;
        GeocodedAddress geocodedAddress;
        Address validatedAddress = null;
        Boolean zipProvided = false;
        Boolean isPoBox = false;

        /** Parse the input address */
        StreetAddress streetAddress = null;
        try {
            streetAddress = StreetAddressParser.parseAddress(address);
        }
        catch (Exception ex) {
            logger.debug("Failed to parse input address" + address.toLogString());
        }

        /** This info about the address helps to decide how to process it */
        zipProvided = isZipProvided(streetAddress);
        isPoBox = (streetAddress != null) ? streetAddress.isPoBoxAddress() : false;

        if (address != null && !address.isEmpty()) {
            /** Perform usps address correction if requested */
            if (districtRequest.isUspsValidate()) {
                Address addressToValidate = null;
                if (address.isEligibleForUSPS()) {
                    addressToValidate = address;
                }
                else if (streetAddress != null) {
                    addressToValidate = streetAddress.toAddress();
                }
                if (addressToValidate != null) {
                    validatedAddress = performAddressCorrection(addressToValidate, districtRequest);
                }
            }

            /** Create GeocodedAddress using either the validated address or original. */
            Address addressToGeocode = (validatedAddress != null && !validatedAddress.isEmpty())
                                       ? validatedAddress : address;
            geocodedAddress = new GeocodedAddress(addressToGeocode);

            /** Geocode address unless opted out */
            if (!districtRequest.isSkipGeocode()) {
                GeocodeRequest geocodeRequest = new GeocodeRequest(districtRequest.getApiRequest(), addressToGeocode, districtRequest.getGeoProvider(), true, true);
                /** Disable cache if provider is specified. */
                if (districtRequest.getGeoProvider() != null && !districtRequest.getGeoProvider().isEmpty()) {
                    geocodeRequest.setUseCache(false);
                }
                geocodedAddress = performGeocode(geocodeRequest, isPoBox);
            }
        }
        /** Perform reverse geocoding for point input */
        else if (point != null) {
            geocodedAddress = new GeocodedAddress(null, new Geocode(point, GeocodeQuality.POINT, "User Supplied"));
        }
        else {
            districtResult = new DistrictResult(this.getClass());
            districtResult.setStatusCode(MISSING_INPUT_PARAMS);
            return districtResult;
        }

        /** Set geocoded address to district request for processing, keeping in mind the validated address */
        if (geocodedAddress != null) {
            if (validatedAddress != null && !validatedAddress.isEmpty() && geocodedAddress.isValidGeocode()
                && (geocodedAddress.getGeocode().getQuality().compareTo(GeocodeQuality.HOUSE) >= 0)) {
                districtRequest.setGeocodedAddress(new GeocodedAddress(validatedAddress, geocodedAddress.getGeocode()));
            }
            else {
                districtRequest.setGeocodedAddress(geocodedAddress);
            }
        }

        /** Obtain district result */
        districtResult = performDistrictAssign(districtRequest, zipProvided, isPoBox);
        if (logger.isDebugEnabled()) {
            logger.debug("Obtained district result with assigned districts: " + FormatUtil.toJsonString(districtResult.getAssignedDistricts()));
        }

        /** Adjust address if it's a PO BOX and was not USPS validated */
        if (isPoBox && !districtResult.isUspsValidated() && districtResult.getAddress() != null) {
            districtResult.getAddress().setAddr1("PO Box " + streetAddress.getPoBox());
        }

        /** Add map and boundary information to the district result */
        if (districtRequest.isShowMaps() && districtResult.isSuccess()) {
            mapProvider.assignMapsToDistrictInfo(districtResult.getDistrictInfo(), districtResult.getDistrictMatchLevel(), false);
        }

        /** Ensure all members (senators,assemblyman, etc) are presented if requested */
        if (districtRequest.isShowMembers() && districtResult.isSuccess()) {
            districtMemberProvider.assignDistrictMembers(districtResult);
        }

        if (SINGLE_LOGGING_ENABLED && requestId != -1) {
            sqlDistrictResultLogger.logDistrictResult(requestId, districtResult);
            requestId = -1;
        }

        return districtResult;
    }

    /**
     * Determines if a zip5 was specified in the input address.
     * @param streetAddress  Parsed input Address
     * @return  True if zip5 was provided, false otherwise
     */
    private Boolean isZipProvided(StreetAddress streetAddress) {
        Boolean zipProvided = false;
        if (streetAddress != null) {
            String zip5 = streetAddress.getZip5();
            zipProvided = (!zip5.isEmpty() && zip5.length() == 5);
        }
        return zipProvided;
    }

    /**
     * Performs geocoding using the default geocode service provider.
     * @param geoRequest The GeocodeRequest to handle.
     * @return GeocodedAddress
     */
    private GeocodedAddress performGeocode(GeocodeRequest geoRequest, boolean isPoBox)
    {
        Address address = (geoRequest.getAddress() != null) ? geoRequest.getAddress().clone() : null;
        String geoProvider = geoRequest.getProvider();
        GeocodeResult geocodeResult = null;

        /** Address-to-point geocoding */
        if (!geoRequest.isReverse()) {
            /** Geocoding for Po Box works better when the address line is empty */
            if (isPoBox && address != null && address.isParsed()) {
                address.setAddr1("");
            }
            /** Do not fallback to other geocoders if provider is specified */
            if (geoProvider != null && !geoProvider.isEmpty()) {
                geoRequest.setUseFallback(false);
            }
            geocodeResult = geocodeProvider.geocode(geoRequest);
        }
        /** Point-to-address geocoding */
        else {
            geocodeResult = revGeocodeProvider.reverseGeocode(geoRequest);
        }

        /** Log geocode request/result to database */
        if (SINGLE_LOGGING_ENABLED) {
            int requestId = sqlGeocodeRequestLogger.logGeocodeRequest(geoRequest);
            sqlGeocodeResultLogger.logGeocodeResult(requestId, geocodeResult);
        }

        return (geocodeResult != null) ? geocodeResult.getGeocodedAddress() : null;
    }

    /**
     * Perform USPS address correction on either the geocoded address or the input address.
     * If the geocoded address is invalid, the original address will be corrected and set as the address
     * on the supplied geocodedAddress parameter.
     * @return GeocodedAddress the address corrected geocodedAddress.
     */
    private Address performAddressCorrection(Address address, DistrictRequest districtRequest)
    {
        boolean usePunct = (districtRequest != null) ? districtRequest.isUsePunct() : false;
        AddressResult addressResult = addressProvider.validate(address, null, usePunct);
        if (addressResult != null && addressResult.isValidated()) {
            if (logger.isTraceEnabled()) {
                logger.trace("USPS Validated Address: " + addressResult.getAdressLogString());
            }
            return addressResult.getAddress();
        }
        return null;
    }

    /**
     * Performs either single or multi district assignment based on the quality of the geocode and the input address.
     * If either an address or geocode is missing, the method will set the appropriate error statuses to the DistrictResult.
     * @param districtRequest               DistrictRequest containing the various parameters
     * @param zipProvided      Set true if user input address included a zip5
     * @param isPoBox          Set true if user input address had a Po Box
     * @return                 DistrictResult
     */
    private DistrictResult performDistrictAssign(DistrictRequest districtRequest, Boolean zipProvided, Boolean isPoBox)
    {
        DistrictResult districtResult = new DistrictResult(this.getClass());
        districtResult.setStatusCode(NO_DISTRICT_RESULT);

        GeocodedAddress geocodedAddress = districtRequest.getGeocodedAddress();
        if (geocodedAddress != null) {
            if (geocodedAddress.isValidAddress()) {
                if (geocodedAddress.isValidGeocode()) {
                    GeocodeQuality level = geocodedAddress.getGeocode().getQuality();
                    if (logger.isTraceEnabled()) {
                        logger.trace(FormatUtil.toJsonString(geocodedAddress));
                    }
                    /** House level matches and above can utilize default district assignment behaviour */
                     if (level.compareTo(GeocodeQuality.HOUSE) >= 0) {
                        districtResult = districtProvider.assignDistricts(geocodedAddress, districtRequest);
                    }
                    /** All other level matches are routed to the overlap assignment method */
                    else {
                        districtResult = districtProvider.assignMultiMatchDistricts(geocodedAddress, zipProvided);
                    }
                }
                else {
                    districtResult.setStatusCode(ResultStatus.INVALID_GEOCODE);
                }
            }
            else if (geocodedAddress.isValidGeocode()) {
                districtRequest.setDistrictStrategy(DistrictStrategy.shapeOnly);
                districtResult = districtProvider.assignDistricts(geocodedAddress, districtRequest);
            }
            else {
                districtResult.setStatusCode(ResultStatus.INSUFFICIENT_ADDRESS);
            }
        }
        else {
            districtResult.setStatusCode(ResultStatus.MISSING_GEOCODED_ADDRESS);
        }

        return districtResult;
    }

    /**
     * Utilizes the service providers to perform batch address validation, geo-coding, and district assignment for an address.
     * @return List<DistrictResult>
     */
    private List<DistrictResult> handleBatchDistrictRequest(BatchDistrictRequest batchDistrictRequest)
    {
        List<Address> addresses = batchDistrictRequest.getAddresses();
        List<Point> points = batchDistrictRequest.getPoints();
        List<GeocodedAddress> geocodedAddresses = new ArrayList<>();

        Boolean usingAddresses = false, usingPoints = false;
        if (addresses != null && !addresses.isEmpty()) {
            usingAddresses = true;
            for (Address address : addresses) {
                geocodedAddresses.add(new GeocodedAddress(address));
            }
        }
        else if (points != null && !points.isEmpty()) {
            usingPoints = true;
            for (Point point : points) {
                geocodedAddresses.add(new GeocodedAddress(null, new Geocode(point, GeocodeQuality.POINT, "User Supplied")));
            }
        }
        else {
            /* No input, return empty result list */
            logger.warn("No input for batch api request! Returning empty list.");
            return new ArrayList<>();
        }

        /** Batch USPS validation */
        if (usingAddresses && batchDistrictRequest.isUspsValidate()) {
            List<AddressResult> addressResults = addressProvider.validate(addresses, null, batchDistrictRequest.isUsePunct());
            if (addressResults != null && addressResults.size() == addresses.size()) {
                for (int i = 0; i < addressResults.size(); i++) {
                    if (addressResults.get(i).isValidated()) {
                        geocodedAddresses.get(i).setAddress(addressResults.get(i).getAddress());
                    }
                }
            }
        }

        /** Batch Geocoding */
        if (usingAddresses && !batchDistrictRequest.isSkipGeocode()) {
            BatchGeocodeRequest batchGeocodeRequest = new BatchGeocodeRequest(batchDistrictRequest);
            List<GeocodeResult> geocodeResults = geocodeProvider.geocode(batchGeocodeRequest);
            for (int i = 0; i < geocodeResults.size(); i++) {
                GeocodeResult geocodeResult = geocodeResults.get(i);
                if (geocodeResult != null) {
                    if (geocodedAddresses.get(i).isValidAddress() && geocodedAddresses.get(i).getAddress().isUspsValidated()) {
                        geocodedAddresses.get(i).setGeocode(geocodeResult.getGeocode());
                    }
                    else {
                        geocodedAddresses.set(i, geocodeResult.getGeocodedAddress());
                    }
                }
            }
            if (BATCH_LOGGING_ENABLED) {
                sqlGeocodeResultLogger.logBatchGeocodeResults(batchGeocodeRequest, geocodeResults, true);
            }
        }

        /** Set geocoded addresses to batch district request for processing */
        batchDistrictRequest.setGeocodedAddresses(geocodedAddresses);

        /** If using points only, set the district strategy to shapefile lookup only */
        if (usingPoints) {
            batchDistrictRequest.setDistrictStrategy(DistrictStrategy.shapeOnly);
        }

        /** Batch District Assign */
        List<DistrictResult> districtResults = districtProvider.assignDistricts(batchDistrictRequest);

        /** Perform batch logging */
        if (BATCH_LOGGING_ENABLED) {
            sqlDistrictResultLogger.logBatchDistrictResults(batchDistrictRequest, districtResults, true);
        }
        return districtResults;
    }
}