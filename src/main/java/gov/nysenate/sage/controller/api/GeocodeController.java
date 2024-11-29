package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.client.response.base.BaseResponse;
import gov.nysenate.sage.client.response.geo.BatchGeocodeResponse;
import gov.nysenate.sage.client.response.geo.GeocodeResponse;
import gov.nysenate.sage.client.response.geo.RevGeocodeResponse;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.api.BatchGeocodeRequest;
import gov.nysenate.sage.model.api.SingleGeocodeRequest;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.provider.geocode.Geocoder;
import gov.nysenate.sage.service.address.AddressServiceProvider;
import gov.nysenate.sage.service.geo.RevGeocodeServiceProvider;
import gov.nysenate.sage.service.geo.SageGeocodeServiceProvider;
import gov.nysenate.sage.util.controller.ConstantUtil;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static gov.nysenate.sage.model.result.ResultStatus.*;
import static gov.nysenate.sage.util.controller.ApiControllerUtil.*;

/**
 * Handles Geo Api requests
 */
@Controller
@RequestMapping(value = ConstantUtil.REST_PATH + "geo")
public class GeocodeController {
    private final SageGeocodeServiceProvider geocodeServiceProvider;
    private final RevGeocodeServiceProvider revGeocodeServiceProvider;
    private final AddressServiceProvider addressProvider;

    @Autowired
    public GeocodeController(SageGeocodeServiceProvider geocodeServiceProvider,
                             RevGeocodeServiceProvider revGeocodeServiceProvider,
                             AddressServiceProvider addressProvider) {
        this.geocodeServiceProvider = geocodeServiceProvider;
        this.revGeocodeServiceProvider = revGeocodeServiceProvider;
        this.addressProvider = addressProvider;
    }

    /**
     * Geocode Api
     * ---------------------------
     * Geocode a single address
     * Usage:
     * (GET)    /api/v2/geo/geocode
     */
    @GetMapping(value = "/geocode")
    public BaseResponse geocode(@RequestParam(required = false) String provider,
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

        Geocoder geocoder = Geocoder.getGeocoder(provider);
        if (geocoder == null) {
            return new ApiError(this.getClass(), PROVIDER_NOT_SUPPORTED);
        }

        var geocodeRequest = new SingleGeocodeRequest(
                getAddressFromParams(addr, addr1, addr2, city, state, zip5, zip4), geocoder,
                useFallback, true, doNotCache, uspsValidate);
        // TODO: normalize address
        geocodeRequest.setAddress(geocodeRequest.getAddress());

        Address uspsAddress = performAddressCorrection(geocodeRequest.getAddress());
        if (uspsAddress != null) {
            geocodeRequest.setAddress(uspsAddress);
        }

        if (geocodeRequest.getAddress() == null || !geocodeRequest.getAddress().isValid()) {
            return new ApiError(this.getClass(), MISSING_ADDRESS);

        }
        return new GeocodeResponse(geocodeServiceProvider.geocode(geocodeRequest));
    }

    /**
     * Reverse Geocode Api
     * ---------------------------
     * Reverse geocode a single pair of latlon coordinates
     * Usage:
     * (GET)    /api/v2/geo/revgeocode
     */
    @GetMapping(value = "/revgeocode")
    public BaseResponse revGeocode(@RequestParam(required = false) String provider,
                                   @RequestParam String lat, @RequestParam String lon,
                                   @RequestParam(required = false, defaultValue = "true") boolean useFallback,
                                   @RequestParam(required = false, defaultValue = "false") boolean doNotCache,
                                   @RequestParam(required = false,  defaultValue = "true") boolean uspsValidate) {
        Geocoder geocoder = Geocoder.getGeocoder(provider);
        if (geocoder == null) {
            return new ApiError(this.getClass(), PROVIDER_NOT_SUPPORTED);
        }

        Point point = getPointFromParams(lat, lon);
        if (point == null) {
            return new ApiError(this.getClass(), MISSING_POINT);
        }
        var geocodeRequest = new SingleGeocodeRequest(
                new Address(), geocoder, useFallback, true, doNotCache, uspsValidate);
        geocodeRequest.setReverse(true);
        geocodeRequest.setPoint(point);
        GeocodeResult revGeocodeResult = revGeocodeServiceProvider.reverseGeocode(geocodeRequest);
        return new RevGeocodeResponse(revGeocodeResult);
    }

    /**
     * Batch Geocode Api
     * ---------------------------
     * Geocode a batch of addresses
     * Usage:
     * (POST)    /api/v2/geo/geocode/batch
     */
    @PostMapping(value = "/geocode/batch")
    public BaseResponse batchGeocode(HttpServletRequest request,
                                     @RequestParam(required = false) String provider,
                                     @RequestParam(required = false, defaultValue = "true") boolean useFallback,
                                     @RequestParam(required = false, defaultValue = "false") boolean doNotCache,
                                     @RequestParam(required = false,  defaultValue = "true") boolean uspsValidate)
            throws IOException {

        Geocoder geocoder = Geocoder.getGeocoder(provider);
        if (geocoder == null) {
            return new ApiError(this.getClass(), PROVIDER_NOT_SUPPORTED);
        }

        String batchJsonPayload = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
        List<Address> addresses = getAddressesFromJsonBody(batchJsonPayload);
        if (addresses.isEmpty()) {
            return new ApiError(this.getClass(), INVALID_BATCH_ADDRESSES);
        }
        var batchGeocodeRequest = new BatchGeocodeRequest(geocoder, useFallback, false, doNotCache, uspsValidate);
        batchGeocodeRequest.setAddresses(addresses);

        List<GeocodeResult> geocodeResults = geocodeServiceProvider.geocode(batchGeocodeRequest);
        return new BatchGeocodeResponse(geocodeResults);
    }

    /**
     * Batch Reverse Geocode Api
     * ---------------------------
     * Reverse geocode a batch of latlon coordinates
     * Usage:
     * (POST)    /api/v2/geo/revgeocode/batch
     */
    @PostMapping(value = "/revgeocode/batch")
    public BaseResponse batchRevGeocode(HttpServletRequest request,
                                @RequestParam(required = false) String provider,
                                @RequestParam(required = false, defaultValue = "true") boolean useFallback,
                                @RequestParam(required = false, defaultValue = "false") boolean doNotCache,
                                @RequestParam(required = false,  defaultValue = "true") boolean uspsValidate)
            throws IOException {

        Geocoder geocoder = Geocoder.getGeocoder(provider);
        if (geocoder == null) {
            return new ApiError(this.getClass(), PROVIDER_NOT_SUPPORTED);
        }

        String batchJsonPayload = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
        List<Point> points = getPointsFromJsonBody(batchJsonPayload);
        if (points.isEmpty()) {
            return new ApiError(this.getClass(), INVALID_BATCH_POINTS);
        }
        var batchGeocodeRequest = new BatchGeocodeRequest(geocoder, useFallback, true, doNotCache, uspsValidate);
        batchGeocodeRequest.setPoints(points);

        List<GeocodeResult> revGeocodeResults = revGeocodeServiceProvider.reverseGeocode(
                points, Geocoder.valueOf(provider.toUpperCase().trim())
        );
        return new BatchGeocodeResponse(revGeocodeResults);
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
            return addressResult.getAddress();
        }
        return null;
    }
}
