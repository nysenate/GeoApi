package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.client.response.district.BatchDistrictResponse;
import gov.nysenate.sage.client.response.district.DistrictResponse;
import gov.nysenate.sage.client.response.district.MappedDistrictResponse;
import gov.nysenate.sage.dao.logger.DistrictRequestLogger;
import gov.nysenate.sage.dao.logger.DistrictResultLogger;
import gov.nysenate.sage.dao.logger.GeocodeRequestLogger;
import gov.nysenate.sage.dao.logger.GeocodeResultLogger;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.api.ApiRequest;
import gov.nysenate.sage.model.api.DistrictRequest;
import gov.nysenate.sage.model.api.GeocodeRequest;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.service.address.AddressServiceProvider;
import gov.nysenate.sage.service.district.DistrictServiceProvider;
import gov.nysenate.sage.service.geo.GeocodeServiceProvider;
import gov.nysenate.sage.service.geo.RevGeocodeServiceProvider;
import gov.nysenate.sage.util.Config;
import gov.nysenate.sage.util.FormatUtil;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import static gov.nysenate.sage.model.result.ResultStatus.*;
import static gov.nysenate.sage.service.district.DistrictServiceProvider.*;

/** Handles District Api requests */
public class DistrictController extends BaseApiController implements Observer
{
    private static Logger logger = Logger.getLogger(DistrictController.class);
    private static Config appConfig = ApplicationFactory.getConfig();

    /** Service Providers */
    private static AddressServiceProvider addressProvider = ApplicationFactory.getAddressServiceProvider();
    private static DistrictServiceProvider districtProvider = ApplicationFactory.getDistrictServiceProvider();
    private static GeocodeServiceProvider geocodeProvider = ApplicationFactory.getGeocodeServiceProvider();
    private static RevGeocodeServiceProvider revGeocodeProvider = ApplicationFactory.getRevGeocodeServiceProvider();

    /** Loggers */
    private static GeocodeRequestLogger geocodeRequestLogger;
    private static GeocodeResultLogger geocodeResultLogger;
    private static DistrictRequestLogger districtRequestLogger;
    private static DistrictResultLogger districtResultLogger;

    private static String BLUEBIRD_DISTRICT_STRATEGY;

    @Override
    public void update(Observable o, Object arg) {
        BLUEBIRD_DISTRICT_STRATEGY = appConfig.getValue("district.strategy.bluebird");
    }

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        appConfig.notifyOnChange(this);
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
        Boolean multiMatch = Boolean.parseBoolean(request.getParameter("multiMatch"));

        /** Specify district strategy */
        String districtStrategy = request.getParameter("districtStrategy");

        logger.info("--------------------------------------");
        logger.info(String.format("District Request | Mode: %s", apiRequest.getRequest()));
        logger.info("--------------------------------------");

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
            case "assign":
            {
                /** Handle single assign request */
                if (!apiRequest.isBatch()) {
                    Address address = getAddressFromParams(request);
                    if (address != null && !address.isEmpty()) {
                        DistrictResult districtResult = districtAssign(
                                apiRequest, address, provider, geoProvider, uspsValidate, !skipGeocode, showMembers, showMaps, districtStrategy);
                        districtResponse = (showMaps) ? new MappedDistrictResponse(districtResult) : new DistrictResponse(districtResult);
                    }
                    else {
                        Point point = getPointFromParams(request);
                        if (point != null) {
                            DistrictResult districtResult = districtAssign(point, provider, geoProvider,uspsValidate,
                                    !skipGeocode, showMembers, showMaps, districtStrategy);
                            districtResponse = (showMaps) ? new MappedDistrictResponse(districtResult) : new DistrictResponse(districtResult);
                        }
                        else {
                            districtResponse = new ApiError(this.getClass(), MISSING_ADDRESS);
                        }
                    }
                }
                /** Handle batch assign request */
                else {
                    List<Address> addresses = getAddressesFromJsonBody(request);
                    if (addresses.size() > 0) {
                        List<DistrictResult> districtResults =
                                districtAssign(addresses, provider, geoProvider, uspsValidate, !skipGeocode, showMembers,
                                               false, districtStrategy);
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
                    Address address = getAddressFromParams(request);
                    if (address != null && !address.isEmpty()) {
                        districtResponse = new DistrictResponse(
                                districtAssign(apiRequest, address, null, null, true, true, false, false, BLUEBIRD_DISTRICT_STRATEGY));
                    }
                    else {
                        districtResponse = new ApiError(this.getClass(), MISSING_ADDRESS);
                    }
                }
                /** Handle batch bluebird assign */
                else {
                    logger.info("Batch bluebird district assign");
                    List<Address> addresses = getAddressesFromJsonBody(request);
                    if (addresses.size() > 0) {
                        List<DistrictResult> districtResults =
                                districtAssign(addresses, null, null, true, true, false, false, BLUEBIRD_DISTRICT_STRATEGY);
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
     * Utilizes the service providers to perform address validation, geo-coding, and district assignment for an address.
     * @return DistrictResult
     */
    private DistrictResult districtAssign(ApiRequest apiRequest, Address address, String provider, String geoProvider, Boolean uspsValidate,
                                          Boolean performGeocode, Boolean showMembers, Boolean showMaps, String districtStrategy)
    {
        /** Perform geocoding if necessary */
        GeocodedAddress geocodedAddress = new GeocodedAddress(address);
        if (performGeocode) {
            GeocodeResult geocodeResult = (geoProvider != null) ? geocodeProvider.geocode(address, geoProvider, false, false)
                                                                : geocodeProvider.geocode(address);
            geocodedAddress = geocodeResult.getGeocodedAddress();

            /** Log geocode request/result to database */
            int requestId = geocodeRequestLogger.logGeocodeRequest(new GeocodeRequest(apiRequest, address, provider, (geoProvider == null), (geoProvider == null)));
            geocodeResultLogger.logGeocodeResult(requestId, geocodeResult);
        }

        /** Perform USPS address correction if requested on either the geocoded address or the input address. */
        if (uspsValidate) {
            Address addressToCorrect = (geocodedAddress != null && geocodedAddress.getAddress() != null)
                                       ? geocodedAddress.getAddress() : address;
            AddressResult addressResult = addressProvider.newInstance("usps").validate(addressToCorrect);
            if (addressResult.isValidated() && geocodedAddress != null) {
                logger.debug("Setting USPS corrected address: " + addressResult.getAddress());
                geocodedAddress.setAddress(addressResult.getAddress());
            }
        }

        DistrictStrategy strategy;
        try {
            strategy = DistrictStrategy.valueOf(districtStrategy);
        }
        catch (Exception ex) {
            strategy = null;
        }

        /** Log district request to database */
        int requestId = districtRequestLogger.logDistrictRequest(
                new DistrictRequest(apiRequest, (geocodedAddress != null) ? geocodedAddress.getAddress() : null,
                                    provider, geoProvider, showMembers, showMaps, uspsValidate, !performGeocode, strategy));

        /** Obtain district result */
        DistrictResult districtResult = districtProvider.assignDistricts(geocodedAddress, provider, DistrictType.getStandardTypes(),
                                                                         showMembers, showMaps, strategy);

        /** Log district result to database */
        districtResultLogger.logDistrictResult(requestId, districtResult);
        return districtResult;
    }

    /**
     * Utilizes the service providers to perform batch address validation, geo-coding, and district assignment for an address.
     * @return List<DistrictResult>
     */
    private List<DistrictResult> districtAssign(List<Address> addresses, String provider, String geoProvider, Boolean uspsValidate,
                                          Boolean performGeocode, Boolean showMembers, Boolean showMaps, String districtStrategy)
    {
        List<GeocodedAddress> geocodedAddresses = new ArrayList<>();
        for (Address address : addresses) {
            geocodedAddresses.add(new GeocodedAddress(address));
        }

        if (performGeocode) {
            List<GeocodeResult> geocodeResults =
                    (geoProvider != null) ? geocodeProvider.geocode(addresses, geoProvider, false, false) : geocodeProvider.geocode(addresses);
            for (int i = 0; i < geocodeResults.size(); i++) {
                geocodedAddresses.set(i, geocodeResults.get(i).getGeocodedAddress());
            }
        }

        if (uspsValidate) {
            List<AddressResult> addressResults = addressProvider.newInstance("usps").validate((ArrayList) addresses);
            for (int i = 0; i < addressResults.size(); i++) {
                if (addressResults.get(i).isValidated() && !geocodedAddresses.isEmpty()) {
                    geocodedAddresses.get(i).setAddress(addressResults.get(i).getAddress());
                }
            }
        }

        DistrictStrategy strategy;
        try {
            strategy = DistrictStrategy.valueOf(districtStrategy);
        }
        catch (Exception ex) {
            strategy = null;
        }

        return districtProvider.assignDistricts(geocodedAddresses, provider, DistrictType.getStandardTypes(),
                                                showMembers, showMaps, strategy);
    }

    /**
     * Utilizes the service providers to perform address validation, geo-coding, and district assignment for a point.
     * @return DistrictResult
     */
    private DistrictResult districtAssign(Point point, String provider, String geoProvider, Boolean uspsValidate,
                                          Boolean performGeocode, Boolean showMembers, Boolean showMaps, String districtStrategy)
    {
        GeocodedAddress geocodedAddress = new GeocodedAddress(null, new Geocode(point, GeocodeQuality.POINT));

        /** Note: If the provider is `streetfile` then we must resolve the point into an address */
        if (performGeocode || (provider != null && provider.equalsIgnoreCase("streetfile"))) {
            GeocodeResult geocodeResult = (geoProvider != null) ? revGeocodeProvider.reverseGeocode(point, geoProvider, false)
                                                                : revGeocodeProvider.reverseGeocode(point);
            if (geocodeResult.isSuccess()) {
                Address revGeocodedAddress = geocodeResult.getAddress();
                if (uspsValidate) {
                    AddressResult addressResult = addressProvider.newInstance("usps").validate(revGeocodedAddress);
                    if (addressResult.isValidated()) {
                        revGeocodedAddress = addressResult.getAddress();
                    }
                }
                geocodedAddress.setAddress(revGeocodedAddress);
            }
        }

        DistrictStrategy strategy;
        try {
            strategy = DistrictStrategy.valueOf(districtStrategy);
        }
        catch (Exception ex) {
            strategy = null;
        }

        return districtProvider.assignDistricts(geocodedAddress, provider, DistrictType.getStandardTypes(),
                                                showMembers, showMaps, strategy);
    }
}