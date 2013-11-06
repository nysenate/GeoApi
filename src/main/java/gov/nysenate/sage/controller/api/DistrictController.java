package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.client.response.district.*;
import gov.nysenate.sage.dao.logger.DistrictRequestLogger;
import gov.nysenate.sage.dao.logger.DistrictResultLogger;
import gov.nysenate.sage.dao.logger.GeocodeRequestLogger;
import gov.nysenate.sage.dao.logger.GeocodeResultLogger;
import gov.nysenate.sage.factory.ApplicationFactory;
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
import gov.nysenate.sage.service.address.CityZipServiceProvider;
import gov.nysenate.sage.service.district.DistrictMemberProvider;
import gov.nysenate.sage.service.district.DistrictServiceProvider;
import gov.nysenate.sage.service.geo.GeocodeServiceProvider;
import gov.nysenate.sage.service.geo.RevGeocodeServiceProvider;
import gov.nysenate.sage.service.map.MapServiceProvider;
import gov.nysenate.sage.util.Config;
import gov.nysenate.sage.util.FormatUtil;
import gov.nysenate.sage.util.StreetAddressParser;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

import static gov.nysenate.sage.model.result.ResultStatus.*;
import static gov.nysenate.sage.service.district.DistrictServiceProvider.DistrictStrategy;

/** Handles District Api requests */
public class DistrictController extends BaseApiController implements Observer
{
    private static Logger logger = Logger.getLogger(DistrictController.class);
    private static Config config = ApplicationFactory.getConfig();

    /** Service Providers */
    private static AddressServiceProvider addressProvider = ApplicationFactory.getAddressServiceProvider();
    private static DistrictServiceProvider districtProvider = ApplicationFactory.getDistrictServiceProvider();
    private static GeocodeServiceProvider geocodeProvider = ApplicationFactory.getGeocodeServiceProvider();
    private static RevGeocodeServiceProvider revGeocodeProvider = ApplicationFactory.getRevGeocodeServiceProvider();
    private static MapServiceProvider mapProvider = ApplicationFactory.getMapServiceProvider();
    private static CityZipServiceProvider cityZipProvider = ApplicationFactory.getCityZipServiceProvider();

    /** Loggers */
    private static GeocodeRequestLogger geocodeRequestLogger;
    private static GeocodeResultLogger geocodeResultLogger;
    private static DistrictRequestLogger districtRequestLogger;
    private static DistrictResultLogger districtResultLogger;

    private static String BLUEBIRD_DISTRICT_STRATEGY;

    private static Boolean SINGLE_LOGGING_ENABLED = false;
    private static Boolean BATCH_LOGGING_ENABLED = false;

    @Override
    public void update(Observable o, Object arg) {
        BLUEBIRD_DISTRICT_STRATEGY = config.getValue("district.strategy.bluebird");

        Boolean API_LOGGING_ENABLED = Boolean.parseBoolean(config.getValue("api.logging.enabled", "false"));
        SINGLE_LOGGING_ENABLED = API_LOGGING_ENABLED && Boolean.parseBoolean(config.getValue("detailed.logging.enabled", "false"));
        BATCH_LOGGING_ENABLED = API_LOGGING_ENABLED && Boolean.parseBoolean(config.getValue("batch.detailed.logging.enabled", "false"));
    }

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        DistrictController.config.notifyOnChange(this);
        update(null, null);
        geocodeRequestLogger = new GeocodeRequestLogger();
        geocodeResultLogger = new GeocodeResultLogger();
        districtRequestLogger = new DistrictRequestLogger();
        districtResultLogger = new DistrictResultLogger();
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        this.doGet(request, response);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        Object districtResponse = null;

        /** Get the ApiRequest */
        ApiRequest apiRequest = getApiRequest(request);
        String provider = apiRequest.getProvider();

        /** Allow for specifying which geocoder to use */
        String geoProvider = request.getParameter("geoProvider");

        /** Fetch senator and other member info if true */
        Boolean showMembers = Boolean.parseBoolean(request.getParameter("showMembers"));

        /** Specify whether or not to return map data */
        Boolean showMaps = Boolean.parseBoolean(request.getParameter("showMaps"));

        /** Indicates whether address validation is required */
        Boolean uspsValidate = Boolean.parseBoolean(request.getParameter("uspsValidate"));

        /** Specify whether or not to geocode (Warning: If false, district assignment will be impaired) */
        Boolean skipGeocode = Boolean.parseBoolean(request.getParameter("skipGeocode"));

        /** Indicates whether info for multiple possible districts should be shown. */
        Boolean showMultiMatch = Boolean.parseBoolean(request.getParameter("showMultiMatch"));

        /** Specify district strategy */
        String districtStrategy = request.getParameter("districtStrategy");

        DistrictRequest districtRequest = new DistrictRequest();
        districtRequest.setApiRequest(apiRequest);
        districtRequest.setAddress(getAddressFromParams(request));
        districtRequest.setPoint(getPointFromParams(request));
        districtRequest.setProvider(provider);
        districtRequest.setGeoProvider(geoProvider);
        districtRequest.setShowMembers(showMembers);
        districtRequest.setShowMaps(showMaps);
        districtRequest.setUspsValidate(uspsValidate);
        districtRequest.setSkipGeocode(skipGeocode);
        districtRequest.setRequestTime(new Timestamp(new Date().getTime()));
        districtRequest.setDistrictStrategy(districtStrategy);

        logger.info("=======================================================");
        logger.info(String.format("| District Request %d | Mode: %s | IP: %s",
                   apiRequest.getId(), apiRequest.getRequest(), apiRequest.getIpAddress()));
        if (!apiRequest.isBatch()) {
            logger.info("-------------------------------------------------------");
            logger.info("| Input Address: " + districtRequest.getAddress());
        }
        logger.info("=======================================================");

        /**
         * If providers are specified then make sure they match the available providers. Send an
         * api error and return if the provider is not supported.
         */
        if (provider != null && !provider.isEmpty() && !districtProvider.isRegistered(provider)) {
            setApiResponse(new ApiError(this.getClass(), DISTRICT_PROVIDER_NOT_SUPPORTED), request);
            return;
        }
        if (geoProvider != null && !geoProvider.isEmpty() && !geocodeProvider.isRegistered(geoProvider)) {
            setApiResponse(new ApiError(this.getClass(), GEOCODE_PROVIDER_NOT_SUPPORTED), request);
            return;
        }

        switch (apiRequest.getRequest())
        {
            case "assign": {
                /** Handle single district assign request using the supplied query parameters. */
                if (!apiRequest.isBatch()) {
                    DistrictResult districtResult = handleDistrictRequest(districtRequest);
                    if (districtResult.isMultiMatch() && showMultiMatch) {
                        districtResponse = (showMaps) ? new MappedMultiDistrictResponse(districtResult) : new MultiDistrictResponse(districtResult);
                    }
                    else {
                        districtResponse = (showMaps) ? new MappedDistrictResponse(districtResult) : new DistrictResponse(districtResult);
                    }
                }
                /** Handle batch district assign request using the supplied query parameters. */
                else {
                    String batchJsonPayload = IOUtils.toString(request.getInputStream(), "UTF-8");
                    List<Address> addresses = getAddressesFromJsonBody(batchJsonPayload);
                    List<Point> points = getPointsFromJsonBody(batchJsonPayload);
                    if (addresses.size() > 0 || points.size() > 0) {
                        BatchDistrictRequest batchDistrictRequest = new BatchDistrictRequest(districtRequest);
                        batchDistrictRequest.setAddresses(addresses);
                        batchDistrictRequest.setPoints(points);

                        List<DistrictResult> districtResults = handleBatchDistrictRequest(batchDistrictRequest);
                        districtResponse = new BatchDistrictResponse(districtResults);
                    }
                    else {
                        districtResponse = new ApiError(this.getClass(), INVALID_BATCH_ADDRESSES);
                    }
                }
                break;
            }
            case "bluebird":
            {
                /** Handle single bluebird assign */
                if (!apiRequest.isBatch()) {
                    DistrictRequest bluebirdRequest = DistrictRequest.buildBluebirdRequest(districtRequest, BLUEBIRD_DISTRICT_STRATEGY);
                    DistrictResult districtResult = handleDistrictRequest(bluebirdRequest);
                    districtResponse = new DistrictResponse(districtResult);
                }
                /** Handle batch bluebird assign */
                else {
                    String batchJsonPayload = IOUtils.toString(request.getInputStream(), "UTF-8");
                    List<Address> addresses = getAddressesFromJsonBody(batchJsonPayload);
                    List<Point> points = getPointsFromJsonBody(batchJsonPayload);
                    if (addresses.size() > 0 || points.size() > 0) {
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
                }
                break;
            }
            default : {
                districtResponse = new ApiError(this.getClass(), SERVICE_NOT_SUPPORTED);
            }
        }
        setApiResponse(districtResponse, request);
    }

    /**
     * Handle district assignment requests and performs functions based on settings in the supplied DistrictRequest.
     * @param districtRequest   Contains the various parameters for the District Assign/Bluebird API
     * @return  DistrictResult
     */
    private DistrictResult handleDistrictRequest(DistrictRequest districtRequest)
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
            logger.debug("Failed to parse input address");
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
                    validatedAddress = performAddressCorrection(addressToValidate);
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
            if (validatedAddress != null && !validatedAddress.isEmpty()) {
                districtRequest.setGeocodedAddress(new GeocodedAddress(validatedAddress, geocodedAddress.getGeocode()));
            }
            else {
                districtRequest.setGeocodedAddress(geocodedAddress);
            }
        }

        /** Obtain district result */
        districtResult = performDistrictAssign(districtRequest, zipProvided, isPoBox);
        logger.debug("Obtained district result with assigned districts: " + FormatUtil.toJsonString(districtResult.getAssignedDistricts()));

        /** Adjust address if it's a PO BOX and was not USPS validated */
        if (isPoBox && !districtResult.isUspsValidated() && districtResult.getAddress() != null) {
            districtResult.getAddress().setAddr1("PO Box " + streetAddress.getPoBox());
        }

        /** Add map and boundary information to the district result */
        if (districtResult.isSuccess()) {
            mapProvider.assignMapsToDistrictInfo(districtResult.getDistrictInfo(), districtResult.getDistrictMatchLevel(), false);
        }

        /** Ensure all members (senators,assemblyman, etc) are presented if requested */
        if (districtRequest.isShowMembers() && districtResult.isSuccess()) {
            DistrictMemberProvider.assignDistrictMembers(districtResult);
        }

        if (SINGLE_LOGGING_ENABLED) {
            int requestId = districtRequestLogger.logDistrictRequest(districtRequest);
            districtResultLogger.logDistrictResult(requestId, districtResult);
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
            int requestId = geocodeRequestLogger.logGeocodeRequest(geoRequest);
            geocodeResultLogger.logGeocodeResult(requestId, geocodeResult);
        }

        return (geocodeResult != null) ? geocodeResult.getGeocodedAddress() : null;
    }

    /**
     * Perform USPS address correction on either the geocoded address or the input address.
     * If the geocoded address is invalid, the original address will be corrected and set as the address
     * on the supplied geocodedAddress parameter.
     * @return GeocodedAddress the address corrected geocodedAddress.
     */
    private Address performAddressCorrection(Address address)
    {
        logger.debug("USPS Validating: " + address.toString());
        AddressResult addressResult = addressProvider.newInstance().validate(address);
        if (addressResult.isValidated()) {
            logger.trace("USPS Validated Address: " + addressResult.getAddress());
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
                    Address address = geocodedAddress.getAddress();
                    logger.trace(FormatUtil.toJsonString(geocodedAddress));
                    /** House level matches and above can utilize default district assignment behaviour */
                    if (level.compareTo(GeocodeQuality.HOUSE) >= 0 || isPoBox) {
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
            logger.debug("No input for batch api request! Returning empty list.");
            return new ArrayList<>();
        }

        /** Batch USPS validation */
        if (usingAddresses && batchDistrictRequest.isUspsValidate()) {
            List<AddressResult> addressResults = addressProvider.newInstance().validate(addresses);
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
                geocodeResultLogger.logBatchGeocodeResults(batchGeocodeRequest, geocodeResults, true);
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
            districtResultLogger.logBatchDistrictResults(batchDistrictRequest, districtResults, true);
        }
        return districtResults;
    }
}