package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.ApiError;
import gov.nysenate.sage.client.response.DistrictResponse;
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
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static gov.nysenate.sage.model.result.ResultStatus.*;

public class DistrictController extends BaseApiController
{
    private static Logger logger = Logger.getLogger(DistrictController.class);
    private static AddressServiceProvider addressProvder = ApplicationFactory.getAddressServiceProvider();
    private static DistrictServiceProvider districtProvider = ApplicationFactory.getDistrictServiceProvider();
    private static GeocodeServiceProvider geocodeProvider = ApplicationFactory.getGeocodeServiceProvider();


    @Override
    public void init(ServletConfig config) throws ServletException
    {
        logger.debug("Initialized " + this.getClass().getSimpleName());
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
            switch (apiRequest.getRequest())
            {
                case "assign":
                    Address address = getAddressFromParams(request);
                    if (address != null && !address.isEmpty()) {

                        /** Perform geocoding */
                        GeocodeResult geocodeResult = (geoProvider != null) ? geocodeProvider.geocode(address, geoProvider, false, false)
                                                                            : geocodeProvider.geocode(address);
                        GeocodedAddress geocodedAddress = geocodeResult.getGeocodedAddress();

                        /** Perform USPS validation if required */
                        if (uspsValidate) {
                            AddressResult addressResult = addressProvder.newInstance("usps").validate(address);
                            if (addressResult.isValidated() && geocodedAddress != null) {
                                geocodedAddress.setAddress(addressResult.getAddress());
                            }
                        }

                        DistrictResult districtResult = districtProvider.assignDistricts(
                                geocodedAddress, provider, DistrictType.getStandardTypes(), showMembers, showMaps);

                        districtResponse = new DistrictResponse(districtResult);
                    }
                    /** If a point is supplied, proceed with district assignment.
                     *  If a district provider is not specified then the point will be reverse geocoded and the
                     *  resulting geocoded address will be sent along the default district assignment pipeline.
                     */
                    else {
                        Point point = getPointFromParams(request);
                        if (point != null) {
                            GeocodedAddress geocodedAddress = new GeocodedAddress(null, new Geocode(point, GeocodeQuality.POINT));
                            if (provider != "shapefile") {
                                GeocodeResult geocodeResult = (geoProvider != null) ? geocodeProvider.reverseGeocode(point, geoProvider, false)
                                                                                    : geocodeProvider.reverseGeocode(point);
                                if (geocodeResult.isSuccess()) {
                                    geocodedAddress.setAddress(geocodeResult.getAddress());
                                }
                            }

                            DistrictResult districtResult = districtProvider.assignDistricts(geocodedAddress, provider,
                                    DistrictType.getStandardTypes(), showMembers, showMaps);

                            districtResponse = new DistrictResponse(districtResult);
                        }
                        else {
                            districtResponse = new ApiError(this.getClass(), MISSING_ADDRESS);
                        }
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
}