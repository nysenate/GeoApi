package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.client.response.base.BaseResponse;
import gov.nysenate.sage.client.response.district.BatchDistrictResponse;
import gov.nysenate.sage.client.response.district.DistrictResponse;
import gov.nysenate.sage.client.response.district.IntersectResponse;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.model.result.DistrictResultWithMembers;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.model.result.IntersectResult;
import gov.nysenate.sage.provider.district.DistrictService;
import gov.nysenate.sage.provider.district.LocalSource;
import gov.nysenate.sage.provider.district.ShapefileService;
import gov.nysenate.sage.provider.geocode.GeocodeService;
import gov.nysenate.sage.provider.geocode.Geocoder;
import gov.nysenate.sage.service.address.AddressService;
import gov.nysenate.sage.service.district.DistrictMemberProvider;
import gov.nysenate.sage.util.controller.ConstantUtil;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static gov.nysenate.sage.model.result.ResultStatus.BAD_OVERLAY;
import static gov.nysenate.sage.model.result.ResultStatus.INVALID_BATCH_ADDRESSES;
import static gov.nysenate.sage.util.controller.ApiControllerUtil.*;

/**
 * Handles District Api requests
 */
@RestController
@RequestMapping(value = ConstantUtil.REST_PATH + "district")
public class DistrictController extends BaseController {
    private final ShapefileService shapefileService;
    private final AddressService addressService;
    private final GeocodeService geocodeService;
    private final DistrictService districtService;
    private final DistrictMemberProvider memberProvider;

    @Autowired
    public DistrictController(ShapefileService shapefileService, AddressService addressService,
                              GeocodeService geocodeService, DistrictService districtService,
                              DistrictMemberProvider memberProvider) {
        this.shapefileService = shapefileService;
        this.addressService = addressService;
        this.geocodeService = geocodeService;
        this.districtService = districtService;
        this.memberProvider = memberProvider;
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

        Address originalAddress = getAddressFromParams(addr, addr1, addr2, city, state, zip5, zip4);
        Address uspsAddress = addressService.validateOrDefault(originalAddress);
        Point point = getPointFromParams(lat, lon);

        List<Geocoder> currGeocoders = null;
        if (geocoder != null) {
            currGeocoders = List.of(getValue(geocoder, Geocoder.class));
        }

        GeocodedAddress geocodedAddress = (point == null ?
                geocodeService.geocode(currGeocoders, uspsAddress) :
                geocodeService.reverseGeocode(currGeocoders, point)).getGeocodedAddress();
        List<LocalSource> currDistrictSources = null;
        if (districtSource != null) {
            currDistrictSources = List.of(getValue(districtSource, LocalSource.class));
        }
        DistrictResult initialResult = districtService.assignDistricts(currDistrictSources, geocodedAddress,
                List.of(DistrictType.values()));
        if (!uspsValidate) {
            geocodedAddress = new GeocodedAddress(originalAddress, geocodedAddress.getGeocode());
        }
        return new DistrictResponse(memberProvider.assignMembers(initialResult), geocodedAddress, usePunct);
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
        List<Address> originalAddresses = getAddressesFromJsonBody(batchJsonPayload);
        List<Address> uspsAddresses = addressService.validateOrDefault(originalAddresses);
        List<Point> points = List.of();
        if (uspsAddresses.isEmpty()) {
            points = getPointsFromJsonBody(batchJsonPayload);
            if (points.isEmpty()) {
                return new ApiError(this.getClass(), INVALID_BATCH_ADDRESSES);
            }
        }

        List<GeocodedAddress> geocodedAddresses = (points.isEmpty() ?
                geocodeService.geocode(uspsAddresses) : geocodeService.reverseGeocode(points))
                    .stream().map(GeocodeResult::getGeocodedAddress).toList();
        List<DistrictResultWithMembers> results =
                districtService.assignDistricts(geocodedAddresses, List.of(DistrictType.values()))
                        .stream().map(memberProvider::assignMembers).toList();

        if (!uspsValidate) {
            List<GeocodedAddress> tempGeoAddrs = new ArrayList<>();
            for (int i = 0; i < geocodedAddresses.size(); i++) {
                tempGeoAddrs.add(new GeocodedAddress(originalAddresses.get(i), geocodedAddresses.get(i).getGeocode()));
            }
            geocodedAddresses = tempGeoAddrs;
        }
        return BatchDistrictResponse.of(results, geocodedAddresses);
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
        return districtAssign(null, null, true,
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
                getValue(sourceType, DistrictType.class), sourceId, getValue(intersectType, DistrictType.class));
        return IntersectResponse.from(intersectResult);
    }
}
