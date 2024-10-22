package gov.nysenate.sage.service.district;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.dao.logger.district.SqlDistrictRequestLogger;
import gov.nysenate.sage.dao.logger.district.SqlDistrictResultLogger;
import gov.nysenate.sage.dao.logger.geocode.SqlGeocodeRequestLogger;
import gov.nysenate.sage.dao.logger.geocode.SqlGeocodeResultLogger;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.model.api.*;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.provider.geocode.Geocoder;
import gov.nysenate.sage.service.address.AddressServiceProvider;
import gov.nysenate.sage.service.geo.RevGeocodeServiceProvider;
import gov.nysenate.sage.service.geo.SageGeocodeServiceProvider;
import gov.nysenate.sage.service.map.MapServiceProvider;
import gov.nysenate.sage.util.FormatUtil;
import gov.nysenate.sage.util.StreetAddressParser;
import gov.nysenate.sage.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static gov.nysenate.sage.model.result.ResultStatus.*;
import static gov.nysenate.sage.util.controller.ApiControllerUtil.setApiResponse;

@Service
public class TopLevelDistrictService {
    private static final Logger logger = LoggerFactory.getLogger(TopLevelDistrictService.class);

    private final AddressServiceProvider addressProvider;
    private final DistrictServiceProvider districtProvider;
    private final SageGeocodeServiceProvider geocodeProvider;
    private final RevGeocodeServiceProvider revGeocodeProvider;
    private final MapServiceProvider mapProvider;
    private final DistrictMemberProvider districtMemberProvider;

    private final boolean SINGLE_LOGGING_ENABLED;
    private final boolean BATCH_LOGGING_ENABLED;
    private final SqlGeocodeRequestLogger sqlGeocodeRequestLogger;
    private final SqlGeocodeResultLogger sqlGeocodeResultLogger;
    private final SqlDistrictRequestLogger sqlDistrictRequestLogger;
    private final SqlDistrictResultLogger sqlDistrictResultLogger;

    public TopLevelDistrictService(Environment env, AddressServiceProvider addressProvider, DistrictServiceProvider districtProvider,
                                   SageGeocodeServiceProvider geocodeProvider, RevGeocodeServiceProvider revGeocodeProvider,
                                   MapServiceProvider mapProvider, DistrictMemberProvider districtMemberProvider,
                                   SqlGeocodeRequestLogger sqlGeocodeRequestLogger, SqlGeocodeResultLogger sqlGeocodeResultLogger,
                                   SqlDistrictRequestLogger sqlDistrictRequestLogger, SqlDistrictResultLogger sqlDistrictResultLogger) {
        this.addressProvider = addressProvider;
        this.districtProvider = districtProvider;
        this.geocodeProvider = geocodeProvider;
        this.revGeocodeProvider = revGeocodeProvider;
        this.mapProvider = mapProvider;
        this.districtMemberProvider = districtMemberProvider;
        this.sqlGeocodeRequestLogger = sqlGeocodeRequestLogger;
        this.sqlGeocodeResultLogger = sqlGeocodeResultLogger;
        this.sqlDistrictRequestLogger = sqlDistrictRequestLogger;
        this.sqlDistrictResultLogger = sqlDistrictResultLogger;

        boolean API_LOGGING_ENABLED = env.isApiLoggingEnabled();
        SINGLE_LOGGING_ENABLED = API_LOGGING_ENABLED && env.isDetailedLoggingEnabled();
        BATCH_LOGGING_ENABLED = API_LOGGING_ENABLED && env.isBatchDetailedLoggingEnabled();
    }

    /**
     * If providers are specified then make sure they match the available providers. Send an
     * api error and return if the provider is not supported.
     */
    public boolean providersUnsupported(String provider, String geoProvider, HttpServletRequest request) {
        ResultStatus errorStatus = null;
        if (provider != null && !provider.isEmpty() &&
                !districtProvider.getProviders().containsKey(provider.toLowerCase())) {
            errorStatus = DISTRICT_PROVIDER_NOT_SUPPORTED;
        }
        Geocoder geocoder;
        try {
            geocoder = Geocoder.valueOf(geoProvider.toLowerCase().trim());

        } catch (IllegalArgumentException ex) {
            geocoder = null;
        }
        if (geocoder == null || !geocodeProvider.geocoders().contains(geocoder)) {
            errorStatus = GEOCODE_PROVIDER_NOT_SUPPORTED;
        }
        if (errorStatus != null) {
            setApiResponse(new ApiError(this.getClass(), errorStatus), request);
        }
        return errorStatus != null;
    }

    public void logDistrictRequest(ApiRequest apiRequest, DistrictRequest districtRequest) {
        logger.info("=======================================================");
        logger.info(String.format("|%sDistrict '%s' Request %d ", (apiRequest.isBatch() ? " Batch " : " "), apiRequest.getRequest(), apiRequest.getId()));
        logger.info(String.format("| IP: %s | Maps: %s | Members: %s", apiRequest.getIpAddress(), districtRequest.isShowMaps(), districtRequest.isShowMembers()));
        if (!apiRequest.isBatch()) {
            logger.info("| Input Address: " + districtRequest.getAdressLogString());
        }
        logger.info("=======================================================");

        if (SINGLE_LOGGING_ENABLED) {
            sqlDistrictRequestLogger.logDistrictRequest(districtRequest);
        }
    }

    public void logIntersectRequest(ApiRequest apiRequest, DistrictRequest districtRequest) {
        logger.info("=======================================================");
        logger.info(String.format("| '%s' Request %d |", apiRequest.getRequest(), apiRequest.getId()));
        logger.info(String.format("| IP: %s | Source %s %s | Intersect %s",
                apiRequest.getIpAddress(), districtRequest.getDistrictType(), districtRequest.getDistrictId(),
                districtRequest.getIntersectType()));
        logger.info("=======================================================");

        if (SINGLE_LOGGING_ENABLED) {
            sqlDistrictRequestLogger.logDistrictRequest(districtRequest);
        }
    }

    public void logElapsedTime(Timestamp startTime, ApiRequest apiRequest) {
        long elapsedTimeMs = TimeUtil.getElapsedMs(startTime);
        logger.info(String.format("%sDistrict Response %d sent in %d ms.",
                (apiRequest.isBatch() ? " Batch " : " "), apiRequest.getId(), elapsedTimeMs));
    }

    /**
     * Handle district assignment requests and performs functions based on settings in the supplied DistrictRequest.
     *
     * @param districtRequest Contains the various parameters for the District Assign/Bluebird API
     * @return DistrictResult
     */
    public DistrictResult handleDistrictRequest(DistrictRequest districtRequest, int requestId) {
        Address address = Optional.ofNullable(districtRequest.getAddress()).orElse(new Address());
        GeocodedAddress geocodedAddress = null;

        /* Parse the input address */
        StreetAddress streetAddress = StreetAddressParser.parseAddress(address);

        if (!address.isEmpty()) {
            /* Perform usps address correction if requested */
            if (districtRequest.isUspsValidate()) {
                if (address.isEligibleForUSPS()) {
                    address = streetAddress.toAddress();
                }
                address = performAddressCorrection(address, districtRequest);
            }
            geocodedAddress = getGeocodedAddress(districtRequest, address);
        }
        /* Perform reverse geocoding for point input */
        else if (districtRequest.getPoint() != null) {
            geocodedAddress = new GeocodedAddress(new Geocode(districtRequest.getPoint(), GeocodeQuality.POINT, "User Supplied"));
        } else {
            return new DistrictResult(null, geocodedAddress, MISSING_INPUT_PARAMS);
        }

        DistrictResult districtResult = performDistrictAssign(geocodedAddress, districtRequest.getProvider(),
                districtRequest.getDistrictTypes(), districtRequest.getDistrictStrategy(), isZipProvided(streetAddress));
        if (logger.isDebugEnabled()) {
            logger.debug("Obtained district result with assigned districts: " + FormatUtil.toJsonString(districtResult.getAssignedDistricts()));
        }

        setDistrictResultInfo(districtResult, districtRequest.isShowMaps(), districtRequest.isShowMembers(), requestId);
        return districtResult;
    }

    /**
     * Handle intersect requests and executes functions based on settings in the supplied DistrictRequest.
     *
     * @param districtRequest Contains the various parameters for the District Assign/Bluebird API
     * @return DistrictResult
     */
    public DistrictResult handleIntersectRequest(DistrictRequest districtRequest, int requestId) {
        /* Get the map, boundary data and intersect statistics */
        DistrictResult districtResult = districtProvider.assignIntersect(districtRequest.getDistrictType(),
                districtRequest.getDistrictId(), districtRequest.getIntersectType());
        setDistrictResultInfo(districtResult, districtRequest.isShowMaps(), districtRequest.isShowMembers(), requestId);
        return districtResult;
    }

    /**
     * Utilizes the service providers to perform batch address validation, geo-coding, and district assignment for an address.
     *
     * @return List<DistrictResult>
     */
    public List<DistrictResult> handleBatchDistrictRequest(BatchDistrictRequest batchRequest) {
        List<Point> points = batchRequest.getPoints();
        batchRequest.setAddresses(batchRequest.getAddresses());

        List<GeocodedAddress> geocodedAddresses;
        if (!batchRequest.getAddresses().isEmpty()) {
            geocodedAddresses = getGeocodedAddresses(batchRequest);
        }
        else if (!points.isEmpty()) {
            geocodedAddresses = points.stream().map(point -> new Geocode(point, GeocodeQuality.POINT, "User Supplied"))
                    .map(GeocodedAddress::new).toList();
            batchRequest.setDistrictStrategy(DistrictServiceProvider.DistrictStrategy.shapeOnly);
        }
        else {
            // No addresses and no points, nothing to do.
            logger.warn("No input for batch api request! Returning empty list.");
            return new ArrayList<>();
        }

        batchRequest.setGeocodedAddresses(geocodedAddresses);
        List<DistrictResult> districtResults = districtProvider.assignDistricts(batchRequest);
        if (BATCH_LOGGING_ENABLED) {
            sqlDistrictResultLogger.logBatchDistrictResults(batchRequest, districtResults, true);
        }
        return districtResults;
    }

    private List<GeocodedAddress> getGeocodedAddresses(BatchDistrictRequest batchRequest) {
        List<Address> addresses = batchRequest.getAddresses();
        /* Batch USPS validation */
        if (batchRequest.isUspsValidate()) {
            List<AddressResult> validationResult = addressProvider.validate(addresses, null, batchRequest.isUsePunct());
            if (validationResult != null && validationResult.size() == addresses.size()) {
                addresses = validationResult.stream().map(AddressResult::getAddress).toList();
            }
        }

        if (batchRequest.isSkipGeocode()) {
            return addresses.stream().map(GeocodedAddress::new).toList();
        }
        BatchGeocodeRequest batchGeocodeRequest = new BatchGeocodeRequest(batchRequest);
        List<GeocodeResult> geocodeResults = geocodeProvider.geocode(batchGeocodeRequest);
        for (int i = 0; i < geocodeResults.size(); i++) {
            GeocodeResult geocodeResult = geocodeResults.get(i);
            Address currAddress = addresses.get(i);
            // Use Address if good, otherwise just keep geocoded address
            if (!currAddress.isEmpty() && currAddress.isUspsValidated()) {
                geocodeResult.setAddress(currAddress);
            }
        }
        if (BATCH_LOGGING_ENABLED) {
            sqlGeocodeResultLogger.logBatchGeocodeResults(batchGeocodeRequest, geocodeResults, true);
        }

        return geocodeResults.stream().map(GeocodeResult::getGeocodedAddress).toList();
    }

    /**
     * Determines if a zip5 was specified in the input address.
     *
     * @param streetAddress Parsed input Address
     * @return True if zip5 was provided, false otherwise
     */
    private static boolean isZipProvided(StreetAddress streetAddress) {
        return streetAddress != null && streetAddress.getZip5() != null;
    }

    /**
     * Perform USPS address correction on either the geocoded address or the input address.
     * If the geocoded address is invalid, the original address will be corrected and set as the address
     * on the supplied geocodedAddress parameter.
     *
     * @return GeocodedAddress the address corrected geocodedAddress.
     */
    private Address performAddressCorrection(Address address, DistrictRequest districtRequest) {
        boolean usePunct = districtRequest != null && districtRequest.isUsePunct();
        AddressResult addressResult = addressProvider.validate(address, null, usePunct);
        if (addressResult != null && addressResult.isValidated()) {
            if (logger.isTraceEnabled()) {
                logger.trace("USPS Validated Address: " + addressResult.getAddress().toLogString());
            }
            var validatedAddress = addressResult.getAddress();
            if (validatedAddress != null && !validatedAddress.isEmpty()) {
                validatedAddress.setUspsValidated(true);
                return validatedAddress;
            }
        }
        return address;
    }

    private void setDistrictResultInfo(DistrictResult districtResult, boolean showMaps, boolean showMembers, int requestId) {
        if (districtResult.isSuccess()) {
            if (showMaps) {
                /* Add map and boundary information to the district result */
                mapProvider.assignMapsToDistrictInfo(districtResult.getDistrictInfo(), districtResult.getDistrictMatchLevel(), false);
            }
            if (showMembers) {
                /* Ensure all members are presented */
                districtMemberProvider.assignDistrictMembers(districtResult);
            }
        }

        if (SINGLE_LOGGING_ENABLED && requestId != -1) {
            sqlDistrictResultLogger.logDistrictResult(requestId, districtResult);
        }
    }

    /**
     * Performs geocoding using the default geocode service provider.
     *
     * @param geoRequest The GeocodeRequest to handle.
     * @return GeocodedAddress
     */
    private GeocodedAddress performGeocode(SingleGeocodeRequest geoRequest) {
        GeocodeResult geocodeResult;

        /* Address-to-point geocoding */
        if (!geoRequest.isReverse()) {
            geocodeResult = geocodeProvider.geocode(geoRequest);
        }
        /* Point-to-address geocoding */
        else {
            geocodeResult = revGeocodeProvider.reverseGeocode(geoRequest);
        }

        /* Log geocode request/result to database */
        if (SINGLE_LOGGING_ENABLED) {
            int requestId = sqlGeocodeRequestLogger.logGeocodeRequest(geoRequest);
            sqlGeocodeResultLogger.logGeocodeResult(requestId, geocodeResult);
        }

        return geocodeResult != null ? geocodeResult.getGeocodedAddress() : null;
    }

    private GeocodedAddress getGeocodedAddress(DistrictRequest districtRequest, Address address) {
        if (districtRequest.isSkipGeocode()) {
            return new GeocodedAddress(address);
        }
        var geocodeRequest = new SingleGeocodeRequest(districtRequest.getApiRequest(),
                address, districtRequest.getGeoProvider(), true, true);
        GeocodedAddress geocodedAddress = performGeocode(geocodeRequest);
        if (address.isUspsValidated() && geocodedAddress != null && geocodedAddress.isValidGeocode() &&
                (geocodedAddress.getGeocode().quality().compareTo(GeocodeQuality.HOUSE) >= 0)) {
            geocodedAddress = new GeocodedAddress(address, geocodedAddress.getGeocode());
            geocodedAddress.getAddress().setUspsValidated(true);
        }
        return geocodedAddress;
   }

    /**
     * Performs either single or multi-district assignment based on the quality of the geocode and the input address.
     * If either an address or geocode is missing, the method will set the appropriate error statuses to the DistrictResult.
     *
     * @param zipProvided     Set true if user input address included a zip5
     * @return DistrictResult
     */
    private DistrictResult performDistrictAssign(@Nonnull GeocodedAddress geocodedAddress, String provider, List<DistrictType> types,
                                                 DistrictServiceProvider.DistrictStrategy strategy, boolean zipProvided) {
        if (geocodedAddress.isValidAddress()) {
            if (!geocodedAddress.isValidGeocode()) {
                return new DistrictResult(null, geocodedAddress, INVALID_GEOCODE);
            }
            GeocodeQuality level = geocodedAddress.getGeocode().quality();
            if (logger.isTraceEnabled()) {
                logger.trace(FormatUtil.toJsonString(geocodedAddress));
            }
            /* House level matches and above can utilize default district assignment behaviour */
            if (level.compareTo(GeocodeQuality.HOUSE) >= 0) {
                return districtProvider.assignDistricts(geocodedAddress, provider, types, strategy);
            }
            /* All other level matches are routed to the overlap assignment method */
            return districtProvider.assignMultiMatchDistricts(geocodedAddress, zipProvided);
        } else if (geocodedAddress.isValidGeocode()) {
            return districtProvider.assignDistricts(geocodedAddress, provider,
                    types, DistrictServiceProvider.DistrictStrategy.shapeOnly);
        }
        return new DistrictResult(null, geocodedAddress, INSUFFICIENT_ADDRESS);
    }
}
