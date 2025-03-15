package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.client.response.base.BaseResponse;
import gov.nysenate.sage.client.response.district.BatchDistrictResponse;
import gov.nysenate.sage.client.response.district.DistrictResponse;
import gov.nysenate.sage.client.response.district.IntersectResponse;
import gov.nysenate.sage.client.response.district.MultiDistrictResponse;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.model.result.IntersectResult;
import gov.nysenate.sage.provider.district.DistrictService;
import gov.nysenate.sage.provider.district.DistrictSource;
import gov.nysenate.sage.provider.district.ShapefileService;
import gov.nysenate.sage.provider.geocode.GeocodeService;
import gov.nysenate.sage.provider.geocode.Geocoder;
import gov.nysenate.sage.service.address.AddressService;
import gov.nysenate.sage.util.controller.ConstantUtil;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static gov.nysenate.sage.model.result.ResultStatus.*;
import static gov.nysenate.sage.util.controller.ApiControllerUtil.*;

/**
 * Handles District Api requests
 */
@RestController
@RequestMapping(value = ConstantUtil.REST_PATH + "district")
public class DistrictController {
    private final List<DistrictSource> districtSourceRanking = new ArrayList<>();
    private final List<Geocoder> geocoderRanking = new ArrayList<>();
    private final ShapefileService shapefileService;
    private final AddressService addressService;
    private final GeocodeService geocodeService;
    private final DistrictService districtService;

    @Autowired
    public DistrictController(@Value("${district.ranking}") String districtRanking,
                              @Value("${geocoder.ranking}") String geocoderRankingStr,
                              ShapefileService shapefileService, AddressService addressService,
                              GeocodeService geocodeService, DistrictService districtService) {
        for (String districtSource : districtRanking.split(", *")) {
            districtSourceRanking.add(DistrictSource.valueOf(districtSource.toUpperCase()));
        }
        for (String geocoder : geocoderRankingStr.split(", *")) {
            geocoderRanking.add(Geocoder.valueOf(geocoder.toUpperCase()));
        }
        this.shapefileService = shapefileService;
        this.addressService = addressService;
        this.geocodeService = geocodeService;
        this.districtService = districtService;
    }

    /**
     * District Assignment Api
     * ---------------------------
     * Assign a postal address to its corresponding NY Districts
     * Usage:
     * (GET)    /api/v2/district/assign
     */
    @GetMapping(value = "/assign")
    public BaseResponse districtAssign(
            @RequestParam(required = false) String districtSource,
            @RequestParam(required = false) String geocoder,
            @RequestParam(required = false, defaultValue = "true") boolean uspsValidate,
            @RequestParam(required = false) boolean showMultiMatch,
            @RequestParam(required = false) boolean usePunct,
            @RequestParam(required = false) String lat,
            @RequestParam(required = false) String lon,
            @RequestParam(required = false) String addr,
            @RequestParam(required = false) String addr1,
            @RequestParam(required = false) String addr2,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String zip5,
            @RequestParam(required = false) String zip4) {

        Address address = getAddressFromParams(addr, addr1, addr2, city, state, zip5, zip4);
        if (uspsValidate) {
            address = addressService.validateOrDefault(address, usePunct);
        }
        Point point = getPointFromParams(lat, lon);

        List<Geocoder> currGeocoders = geocoderRanking;
        if (geocoder != null) {
            try {
                currGeocoders = List.of(Geocoder.valueOf(geocoder.trim().toUpperCase()));
            }
            catch (IllegalArgumentException e) {
                return new ApiError(DistrictController.class, GEOCODE_PROVIDER_NOT_SUPPORTED);
            }
        }

        GeocodedAddress geocodedAddress = point == null ? geocodeService.getGeocodedAddress(currGeocoders, address) :
                geocodeService.getRevGeocodedAddress(currGeocoders, point);
        List<DistrictSource> currDistrictSources = districtSourceRanking;
        if (districtSource != null) {
            try {
                currDistrictSources = List.of(DistrictSource.valueOf(districtSource.trim().toUpperCase()));
            }
            catch (IllegalArgumentException e) {
                return new ApiError(DistrictController.class, DISTRICT_PROVIDER_NOT_SUPPORTED);
            }
        }
        DistrictResult districtResult = districtService.assignDistricts(currDistrictSources, geocodedAddress);
        if (districtResult.isMultiMatch() && showMultiMatch) {
            return new MultiDistrictResponse(districtResult);
        } else {
            return new DistrictResponse(districtResult);
        }
    }

    /**
     * District Assignment Api
     * ---------------------------
     * Assign a postal address to its corresponding NY Districts
     * Usage:
     * (POST)    /api/v2/district/assign/batch
     */
    @PostMapping(value = "/assign/batch")
    public BaseResponse districtBatchAssign(
            HttpServletRequest request,
            @RequestParam(required = false, defaultValue = "true") boolean uspsValidate,
            @RequestParam(required = false) boolean usePunct)
            throws IOException {

        String batchJsonPayload = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
        List<Address> addresses = getAddressesFromJsonBody(batchJsonPayload);
        if (uspsValidate) {
            addresses = addressService.validateOrDefault(addresses, usePunct);
        }
        List<Point> points = List.of();
        if (addresses.isEmpty()) {
            points = getPointsFromJsonBody(batchJsonPayload);
            if (points.isEmpty()) {
                return new ApiError(this.getClass(), INVALID_BATCH_ADDRESSES);
            }
        }

        // TODO: only geocode if shapefile used?
        List<GeocodedAddress> geocodedAddresses = points.isEmpty() ?
                geocodeService.getGeocodedAddresses(geocoderRanking, addresses) :
                geocodeService.getRevGeocodedAddresses(geocoderRanking, points);

        return new BatchDistrictResponse(districtService.assignDistricts(districtSourceRanking, geocodedAddresses));
    }

    /**
     * District Assignment Api
     * ---------------------------
     * Assign a postal address to its corresponding NY Districts
     * Usage:
     * (GET)    /api/v2/district/bluebird
     */
    @GetMapping(value = "/bluebird")
    public BaseResponse bluebirdAssign(
            @RequestParam(required = false) boolean usePunct,
            @RequestParam(required = false) String lat,
            @RequestParam(required = false) String lon,
            @RequestParam(required = false) String addr,
            @RequestParam(required = false) String addr1,
            @RequestParam(required = false) String addr2,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String zip5,
            @RequestParam(required = false) String zip4) {
        return districtAssign(null, null, true, false,
                usePunct, lat, lon, addr, addr1, addr2, city, state, zip5, zip4);
    }

    /**
     * District Assignment Api
     * ---------------------------
     * Assign a postal address to its corresponding NY Districts
     * Usage:
     * (POST)    /api/v2/district/bluebird/batch
     */
    @PostMapping(value = "/bluebird/batch")
    public BaseResponse bluebirdBatchAssign(
            HttpServletRequest request,
            @RequestParam(required = false) boolean usePunct) throws IOException {
        return districtBatchAssign(request, true, usePunct);
    }

    /**
     * Intersect Api
     * ---------------------------
     * Find the intersection between one type of NY District and another
     * Usage:
     * (GET)    /api/v2/district/intersect
     */
    @GetMapping(value = "/intersect")
    public Object districtIntersect(@RequestParam String sourceType, @RequestParam String sourceId,
                                    @RequestParam String intersectType) {
        if (sourceId == null || sourceId.equals("null") || sourceId.isEmpty() || sourceType.equals(intersectType)) {
            return new BaseResponse(BAD_OVERLAY);
        }
        IntersectResult intersectResult = shapefileService.getIntersectionResult(
                DistrictType.resolveType(sourceType), sourceId, DistrictType.resolveType(intersectType));
        return IntersectResponse.from(intersectResult);
    }
}
