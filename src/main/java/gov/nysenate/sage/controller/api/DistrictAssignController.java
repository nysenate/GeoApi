package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.client.response.base.BaseResponse;
import gov.nysenate.sage.client.response.base.MapResponse;
import gov.nysenate.sage.client.response.district.BatchDistrictResponse;
import gov.nysenate.sage.client.response.district.DistrictResponse;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.*;
import gov.nysenate.sage.provider.district.DistrictService;
import gov.nysenate.sage.provider.district.LocalSource;
import gov.nysenate.sage.provider.district.ShapefileService;
import gov.nysenate.sage.provider.geocode.GeocodeService;
import gov.nysenate.sage.provider.geocode.Geocoder;
import gov.nysenate.sage.service.address.AddressService;
import gov.nysenate.sage.service.district.DistrictMemberProvider;
import gov.nysenate.sage.service.district.DistrictNameCache;
import gov.nysenate.sage.util.controller.ConstantUtil;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static gov.nysenate.sage.model.result.ResultStatus.*;
import static gov.nysenate.sage.util.controller.ApiControllerUtil.*;

/**
 * Handles District Api requests
 */
@RestController
@RequestMapping(value = ConstantUtil.REST_PATH + "district")
public class DistrictAssignController extends BaseDistrictController<LocalSource> {
    private final AddressService addressService;
    private final GeocodeService geocodeService;
    private final DistrictService districtService;

    @Autowired
    public DistrictAssignController(DistrictNameCache nameCache, ShapefileService shapefileService,
                                    DistrictMemberProvider memberProvider, AddressService addressService,
                                    GeocodeService geocodeService, DistrictService districtService) {
        super(nameCache, shapefileService, memberProvider);
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
    public DistrictResponse districtAssign(
            @RequestParam(required = false) String districtSource,
            @RequestParam(required = false) String geocoder,
            @RequestParam(required = false, defaultValue = "true") boolean uspsValidate,
            @RequestParam(required = false) boolean usePunct,
            @RequestParam(required = false) boolean showMaps,
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

        List<Geocoder> currGeocoders = getListOrNull(Geocoder.class, geocoder);
        GeocodedAddress geocodedAddress = (point == null ?
                geocodeService.geocode(currGeocoders, uspsAddress) :
                geocodeService.reverseGeocode(currGeocoders, point)).getGeocodedAddress();

        List<LocalSource> currDistrictSources = getListOrNull(districtSource);
        DistrictResult initialResult = districtService.assignDistricts(currDistrictSources, geocodedAddress,
                Set.of(DistrictType.values()));
        if (!uspsValidate) {
            geocodedAddress = new GeocodedAddress(originalAddress, geocodedAddress.getGeocode());
        }
        var response = new DistrictResponse(initialResult, geocodedAddress, usePunct);
        assignData(response, true, showMaps);
        return response;
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
            @RequestParam(required = false, defaultValue = "true") boolean uspsValidate)
            throws IOException {

        String batchJsonPayload = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
        List<Address> originalAddresses = getAddressesFromJsonBody(batchJsonPayload);
        if (originalAddresses == null) {
            return new ApiError(this.getClass(), INVALID_BATCH_ADDRESSES);
        }
        List<Address> uspsAddresses = addressService.validateOrDefault(originalAddresses);

        List<GeocodedAddress> geocodedAddresses = geocodeService.geocode(uspsAddresses)
                    .stream().map(GeocodeResult::getGeocodedAddress).toList();
        List<DistrictResult> results =
                districtService.assignDistricts(geocodedAddresses, Set.of(DistrictType.values()));

        if (!uspsValidate) {
            List<GeocodedAddress> tempGeoAddrs = new ArrayList<>();
            for (int i = 0; i < geocodedAddresses.size(); i++) {
                tempGeoAddrs.add(new GeocodedAddress(originalAddresses.get(i), geocodedAddresses.get(i).getGeocode()));
            }
            geocodedAddresses = tempGeoAddrs;
        }
        var response = BatchDistrictResponse.of(results, geocodedAddresses);
        response.getResults().forEach(
                singleResponse -> assignData(singleResponse, false, false)
        );
        return response;
    }

    private void assignData(DistrictResponse response, boolean showMembers, boolean showMaps) {
        for (var view : response.getDistricts().values()) {
            assignData(view, showMembers, showMaps);
        }
    }

    /**
     * District Assignment Api
     * ---------------------------
     * Assign a postal address to its corresponding NY Districts
     * Usage:
     * (GET)    /api/v2/district/bluebird
     */
    @GetMapping(value = "/bluebird")
    public DistrictResponse bluebirdAssign(
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
                usePunct, false, lat, lon, addr, addr1, addr2, city, state, zip5, zip4);
    }

    /**
     * District Names Api
     * ---------------------------
     * Get a map from code -> name for a single district type.
     * Usage:
     * (GET)    /api/v2/district/names?type=SENATE
     */
    @GetMapping(value = "/names")
    public MapResponse<String, String> names(@RequestParam String type) {
        return new MapResponse<>(nameCache.get(getValue(DistrictType.class, type)));
    }
}
