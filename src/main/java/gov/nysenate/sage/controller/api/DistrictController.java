package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.client.response.base.BaseResponse;
import gov.nysenate.sage.client.response.district.BatchDistrictResponse;
import gov.nysenate.sage.client.response.district.DistrictResponse;
import gov.nysenate.sage.client.response.district.IntersectResponse;
import gov.nysenate.sage.client.response.district.MultiDistrictResponse;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.api.IntersectRequest;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.model.result.IntersectResult;
import gov.nysenate.sage.provider.address.AddressSource;
import gov.nysenate.sage.provider.district.DistrictService;
import gov.nysenate.sage.provider.district.DistrictSource;
import gov.nysenate.sage.provider.geocode.GeocodeService;
import gov.nysenate.sage.provider.geocode.Geocoder;
import gov.nysenate.sage.service.address.AddressService;
import gov.nysenate.sage.service.district.IntersectService;
import gov.nysenate.sage.util.controller.ConstantUtil;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import static gov.nysenate.sage.provider.district.DistrictSource.SHAPEFILE;
import static gov.nysenate.sage.provider.district.DistrictSource.STREETFILE;
import static gov.nysenate.sage.util.controller.ApiControllerUtil.*;

/**
 * Handles District Api requests
 */
@Controller
@RequestMapping(value = ConstantUtil.REST_PATH + "district")
public class DistrictController {
    private final String bluebirdStrategy, defaultSingleStrategy, defaultBatchStrategy;
    private final IntersectService intersectService;
    private final AddressService addressService;
    private final GeocodeService geocodeService;
    private final DistrictService districtService;

    @Autowired
    public DistrictController(@Value("${district.strategy.bluebird}") String bluebirdDistrictStrategy,
                              @Value("${district.strategy.single}") String singleDistrictStrategy,
                              @Value("${district.strategy.batch}") String batchDistrictStrategy,
                              IntersectService intersectService, AddressService addressService,
                              GeocodeService geocodeService, DistrictService districtService) {
        this.bluebirdStrategy = bluebirdDistrictStrategy;
        this.defaultSingleStrategy = singleDistrictStrategy;
        this.defaultBatchStrategy = batchDistrictStrategy;
        this.districtService = districtService;
        this.intersectService = intersectService;
        this.addressService = addressService;
        this.geocodeService = geocodeService;
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
            @RequestParam(required = false) String geoProvider,
            @RequestParam(required = false, defaultValue = "true") boolean uspsValidate,
            @RequestParam(required = false) boolean showMultiMatch,
            @RequestParam(required = false) String districtStrategy,
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

        if (districtStrategy == null) {
            districtStrategy = defaultSingleStrategy;
        }
        List<DistrictSource> providers = getProviders(districtStrategy);
        if (providers == null) {
            return new ApiError(DistrictController.class, DISTRICT_PROVIDER_NOT_SUPPORTED);
        }
        Geocoder geocoder;
        try {
            geocoder = Geocoder.valueOf(geoProvider.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            return new ApiError(DistrictController.class, GEOCODE_PROVIDER_NOT_SUPPORTED);
        }

        Address address = getAddressFromParams(addr, addr1, addr2, city, state, zip5, zip4);
        if (uspsValidate) {
            address = addressService.validateOrDefault(address, AddressSource.AMS, usePunct);
        }
        Point point = getPointFromParams(lat, lon);

        // TODO: only geocode if shapefile used
        GeocodedAddress geocodedAddress = point == null ? geocodeService.getGeocodedAddress(List.of(geocoder), address) :
                geocodeService.getRevGeocodedAddress(List.of(geocoder), point);

        DistrictResult districtResult = districtService.assignDistricts(providers, geocodedAddress);
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
            @RequestParam(required = false) String geoProvider,
            @RequestParam(required = false, defaultValue = "true") boolean uspsValidate,
            @RequestParam(required = false) String districtStrategy,
            @RequestParam(required = false) boolean usePunct)
            throws IOException {

        if (districtStrategy == null) {
            districtStrategy = defaultBatchStrategy;
        }

        List<DistrictSource> providers = getProviders(districtStrategy);
        if (providers == null) {
            return new ApiError(DistrictController.class, DISTRICT_PROVIDER_NOT_SUPPORTED);
        }
        Geocoder geocoder;
        try {
            geocoder = Geocoder.valueOf(geoProvider.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            return new ApiError(DistrictController.class, GEOCODE_PROVIDER_NOT_SUPPORTED);
        }

        String batchJsonPayload = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
        List<Address> addresses = getAddressesFromJsonBody(batchJsonPayload);
        if (uspsValidate) {
            addresses = addressService.validateOrDefault(addresses, AddressSource.AMS, usePunct);
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
                geocodeService.getGeocodedAddresses(List.of(geocoder), addresses) :
                geocodeService.getRevGeocodedAddresses(List.of(geocoder), points);

        return new BatchDistrictResponse(districtService.assignDistricts(providers, geocodedAddresses));
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
            @RequestParam(required = false) String geoProvider,
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
        return districtAssign(geoProvider, true, false, bluebirdStrategy,
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
            @RequestParam(required = false) String geoProvider,
            @RequestParam(required = false) boolean usePunct) throws IOException {
        return districtBatchAssign(request, geoProvider, true, bluebirdStrategy, usePunct);
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
        var intersectRequest = new IntersectRequest(DistrictType.resolveType(sourceType),
                sourceId, DistrictType.resolveType(intersectType));
        IntersectResult intersectResult = intersectService.handleIntersectRequest(intersectRequest);
        return new IntersectResponse(intersectResult, intersectRequest.intersectWith());
    }

    private static List<DistrictSource> getProviders(String strategy) {
        return switch (strategy) {
            case "streetFallback" -> List.of(SHAPEFILE, STREETFILE);
            case "shapeFallback" -> List.of(STREETFILE, SHAPEFILE);
            case "streetOnly" -> List.of(STREETFILE);
            case "shapeOnly" -> List.of(SHAPEFILE);
            default -> null;
        };
    }
}
