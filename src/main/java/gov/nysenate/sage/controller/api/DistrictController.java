package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.ApiError;
import gov.nysenate.sage.client.response.DistrictResponse;
import gov.nysenate.sage.client.response.MappedDistrictResponse;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.api.ApiRequest;
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
import gov.nysenate.sage.util.Config;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import static gov.nysenate.sage.model.result.ResultStatus.*;

public class DistrictController extends BaseApiController implements Observer
{
    private static Logger logger = Logger.getLogger(DistrictController.class);
    private static Config appConfig = ApplicationFactory.getConfig();
    private static AddressServiceProvider addressProvider = ApplicationFactory.getAddressServiceProvider();
    private static DistrictServiceProvider districtProvider = ApplicationFactory.getDistrictServiceProvider();
    private static GeocodeServiceProvider geocodeProvider = ApplicationFactory.getGeocodeServiceProvider();
    private static String bluebirdDistrictStrategy;

    @Override
    public void update(Observable o, Object arg) {
        bluebirdDistrictStrategy = appConfig.getValue("district.strategy.bluebird");
    }

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        logger.debug("Initialized " + this.getClass().getSimpleName());
        appConfig.notifyOnChange(this);
        update(null, null);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        this.doGet(request, response);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        Object districtResponse;

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

        /** Specify district strategy */
        String districtStrategy = request.getParameter("districtStrategy");

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

        /** Handle single request */
        if (!apiRequest.isBatch()) {

            Address address = getAddressFromParams(request);

            switch (apiRequest.getRequest())
            {
                case "assign":
                    if (address != null && !address.isEmpty()) {
                        DistrictResult districtResult = districtAssign(address, provider, geoProvider, uspsValidate,
                                !skipGeocode, showMembers, showMaps, districtStrategy);
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
                    break;

                case "bluebird":
                    if (address != null && !address.isEmpty()) {
                        districtResponse = new DistrictResponse(
                                districtAssign(address, null, null, true, true, false, false, bluebirdDistrictStrategy));
                    }
                    else {
                        districtResponse = new ApiError(this.getClass(), MISSING_ADDRESS);
                    }
                    break;

                default :
                    districtResponse = new ApiError(this.getClass(), SERVICE_NOT_SUPPORTED);
            }
        }
        /** Handle batch request */
        else {
            districtResponse = new ApiError(this.getClass(), FEATURE_NOT_SUPPORTED);
        }

        setApiResponse(districtResponse, request);
    }

    /**
     * Utilizes the service providers to perform address validation, geo-coding, and district assignment for an address.
     * @return DistrictResult
     */
    private DistrictResult districtAssign(Address address, String provider, String geoProvider, Boolean uspsValidate,
                                          Boolean performGeocode, Boolean showMembers, Boolean showMaps, String districtStrategy)
    {
        GeocodedAddress geocodedAddress = new GeocodedAddress(address);
        if (performGeocode) {
            GeocodeResult geocodeResult = (geoProvider != null) ? geocodeProvider.geocode(address, geoProvider, false, false)
                                                                : geocodeProvider.geocode(address);
            geocodedAddress = geocodeResult.getGeocodedAddress();
        }

        if (uspsValidate) {
            AddressResult addressResult = addressProvider.newInstance("usps").validate(address);
            if (addressResult.isValidated() && geocodedAddress != null) {
                geocodedAddress.setAddress(addressResult.getAddress());
            }
        }

        DistrictServiceProvider.DistrictStrategy strategy;
        try {
            strategy = DistrictServiceProvider.DistrictStrategy.valueOf(districtStrategy);
        }
        catch (Exception ex) {
            strategy = null;
        }

        return districtProvider.assignDistricts(geocodedAddress, provider, DistrictType.getStandardTypes(),
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
            GeocodeResult geocodeResult = (geoProvider != null) ? geocodeProvider.reverseGeocode(point, geoProvider, false)
                                                                : geocodeProvider.reverseGeocode(point);
            if (geocodeResult.isSuccess()) {
                Address revGeocodedAddress = geocodeResult.getAddress();
                if (uspsValidate) {
                    AddressResult addressResult = addressProvider.newInstance("usps").validate(revGeocodedAddress);
                    if (addressResult.isValidated() && geocodedAddress != null) {
                        revGeocodedAddress = addressResult.getAddress();
                    }
                }
                geocodedAddress.setAddress(revGeocodedAddress);
            }
        }

        DistrictServiceProvider.DistrictStrategy strategy;
        try {
            strategy = DistrictServiceProvider.DistrictStrategy.valueOf(districtStrategy);
        }
        catch (Exception ex) {
            strategy = null;
        }

        return districtProvider.assignDistricts(geocodedAddress, provider, DistrictType.getStandardTypes(),
                                                showMembers, showMaps, strategy);
    }
}