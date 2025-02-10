package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.client.response.base.BaseResponse;
import gov.nysenate.sage.client.response.geo.BatchGeocodeResponse;
import gov.nysenate.sage.client.response.geo.GeocodeResponse;
import gov.nysenate.sage.client.response.geo.RevGeocodeResponse;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.provider.geocode.GeocodeService;
import gov.nysenate.sage.provider.geocode.Geocoder;
import gov.nysenate.sage.service.address.AddressService;
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
    private final AddressService addressService;
    private final GeocodeService geocodeService;

    @Autowired
    public GeocodeController(AddressService addressService, GeocodeService geocodeService) {
        this.addressService = addressService;
        this.geocodeService = geocodeService;
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
                                @RequestParam(required = false,  defaultValue = "true") boolean uspsValidate) {

        Geocoder geocoder = Geocoder.getGeocoder(provider);
        if (geocoder == null) {
            return new ApiError(this.getClass(), PROVIDER_NOT_SUPPORTED);
        }

        Address address = getAddressFromParams(addr, addr1, addr2, city, state, zip5, zip4);
        List<Geocoder> geocoders = Geocoder.getGeocoders(geocoder, true, useFallback);
        if (uspsValidate) {
            address = addressService.validateOrDefault(address, false);
        }

        if (address == null || !address.isValid()) {
            return new ApiError(this.getClass(), MISSING_ADDRESS);

        }
        return new GeocodeResponse(geocodeService.geocode(geocoders, address));
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
                                   @RequestParam(required = false,  defaultValue = "true") boolean uspsValidate) {
        Geocoder geocoder = Geocoder.getGeocoder(provider);
        if (geocoder == null) {
            return new ApiError(this.getClass(), PROVIDER_NOT_SUPPORTED);
        }

        Point point = getPointFromParams(lat, lon);
        if (point == null) {
            return new ApiError(this.getClass(), MISSING_POINT);
        }
        GeocodeResult result = geocodeService.reverseGeocode(
                Geocoder.getGeocoders(geocoder, false, useFallback), point);
        if (uspsValidate) {
            result.setAddress(addressService.validateOrDefault(result.getAddress(), false));
        }
        return new RevGeocodeResponse(result);
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
                                     @RequestParam(required = false,  defaultValue = "true") boolean uspsValidate)
            throws IOException {

        Geocoder geocoder = Geocoder.getGeocoder(provider);
        if (geocoder == null) {
            return new ApiError(this.getClass(), PROVIDER_NOT_SUPPORTED);
        }

        String batchJsonPayload = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
        List<Address> addresses = getAddressesFromJsonBody(batchJsonPayload);
        if (uspsValidate) {
            addresses = addressService.validateOrDefault(addresses, false);
        }
        if (addresses.isEmpty()) {
            return new ApiError(this.getClass(), INVALID_BATCH_ADDRESSES);
        }

        List<GeocodeResult> geocodeResults = geocodeService.geocode(Geocoder.getGeocoders(geocoder, true, useFallback), addresses);
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

        List<Geocoder> geocoders = Geocoder.getGeocoders(geocoder, false, useFallback);
        List<GeocodeResult> revGeocodeResults = geocodeService.reverseGeocode(geocoders, points);
        if (uspsValidate) {
            List<Address> addresses = revGeocodeResults.stream().map(GeocodeResult::getAddress).toList();
            addresses = addressService.validateOrDefault(addresses, false);
            for (int i = 0; i < addresses.size(); i++) {
                revGeocodeResults.get(i).setAddress(addresses.get(i));
            }
        }
        return new BatchGeocodeResponse(revGeocodeResults);
    }
}
