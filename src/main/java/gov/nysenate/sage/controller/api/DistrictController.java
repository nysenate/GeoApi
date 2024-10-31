package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.client.response.base.BaseResponse;
import gov.nysenate.sage.client.response.district.BatchDistrictResponse;
import gov.nysenate.sage.client.response.district.DistrictResponse;
import gov.nysenate.sage.client.response.district.MappedMultiDistrictResponse;
import gov.nysenate.sage.client.response.district.MultiDistrictResponse;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.api.ApiRequest;
import gov.nysenate.sage.model.api.BatchDistrictRequest;
import gov.nysenate.sage.model.api.DistrictRequest;
import gov.nysenate.sage.model.api.IntersectRequest;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.model.result.IntersectResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.provider.district.DistrictSource;
import gov.nysenate.sage.provider.geocode.Geocoder;
import gov.nysenate.sage.service.district.IntersectService;
import gov.nysenate.sage.service.district.TopLevelDistrictService;
import gov.nysenate.sage.util.TimeUtil;
import gov.nysenate.sage.util.controller.ConstantUtil;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static gov.nysenate.sage.controller.api.DistrictUtil.*;
import static gov.nysenate.sage.controller.api.filter.ApiFilter.getApiRequest;
import static gov.nysenate.sage.model.result.ResultStatus.*;
import static gov.nysenate.sage.util.controller.ApiControllerUtil.*;

/**
 * Handles District Api requests
 */
@Controller
@RequestMapping(value = ConstantUtil.REST_PATH + "district")
public class DistrictController {
    private static final Logger logger = LoggerFactory.getLogger(DistrictController.class);
    private final String bluebirdDistrictStrategy;
    private final TopLevelDistrictService districtService;
    private final IntersectService intersectService;

    @Autowired
    public DistrictController(@Value("${district.strategy.bluebird:streetFallback}") String bluebirdDistrictStrategy,
                              TopLevelDistrictService districtService, IntersectService intersectService) {
        this.bluebirdDistrictStrategy = bluebirdDistrictStrategy;
        this.districtService = districtService;
        this.intersectService = intersectService;
    }

    /**
     * District Assignment Api
     * ---------------------------
     * Assign a postal address to its corresponding NY Districts
     * Usage:
     * (GET)    /api/v2/district/assign
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param provider String
     * @param usePunct boolean
     * @param addr String
     * @param addr1 String
     * @param addr2 String
     * @param city String
     * @param state String
     * @param zip5 String
     * @param zip4 String
     * @param lat String
     * @param lon String
     * @param districtStrategy String
     * @param geoProvider String
     * @param uspsValidate boolean
     * @param showMultiMatch boolean
     * @param skipGeocode boolean
     */
    @RequestMapping(value = "/assign", method = RequestMethod.GET)
    public void districtAssign(HttpServletRequest request, HttpServletResponse response,
                               @RequestParam(required = false) String provider,
                               @RequestParam(required = false) String geoProvider,
                               @RequestParam(required = false, defaultValue = "true") boolean uspsValidate,
                               @RequestParam(required = false, defaultValue = "false") boolean skipGeocode,
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
        Object districtResponse;
        Timestamp startTime = TimeUtil.currentTimestamp();
        int requestId = -1;

        /* Get the ApiRequest */
        ApiRequest apiRequest = getApiRequest(request);
        Address address = getAddressFromParams(addr, addr1, addr2, city, state, zip5, zip4);
        DistrictRequest districtRequest = createFullDistrictRequest(apiRequest, address,
                getPointFromParams(lat, lon), provider, geoProvider, uspsValidate, usePunct, skipGeocode, districtStrategy);

        districtRequest.setAddress(districtRequest.getAddress());

        districtService.logDistrictRequest(apiRequest, districtRequest);

        if (providersUnsupported(provider, geoProvider, request)) {
            return;
        }

        DistrictResult districtResult = districtService.handleDistrictRequest(districtRequest, requestId);
        if (districtResult.isMultiMatch() && showMultiMatch) {
            districtResponse = new MultiDistrictResponse(districtResult);
        } else {
            districtResponse = new DistrictResponse(districtResult);
        }

        setApiResponse(districtResponse, request);
        logElapsedTime(startTime, apiRequest);
    }

    /**
     * District Assignment Api
     * ---------------------------
     * Assign a postal address to its corresponding NY Districts
     * Usage:
     * (POST)    /api/v2/district/assign/batch
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param provider String
     * @param usePunct boolean
     * @param districtStrategy String
     * @param geoProvider String
     * @param uspsValidate boolean
     * @param skipGeocode boolean
     *
     */
    @RequestMapping(value = "/assign/batch", method = RequestMethod.POST)
    public void districtBatchAssign(HttpServletRequest request, HttpServletResponse response,
                                    @RequestParam(required = false) String provider,
                                    @RequestParam(required = false) String geoProvider,
                                    @RequestParam(required = false, defaultValue = "true") boolean uspsValidate,
                                    @RequestParam(required = false, defaultValue = "false") boolean skipGeocode,
                                    @RequestParam(required = false) String districtStrategy,
                                    @RequestParam(required = false) boolean usePunct)
            throws IOException {
        Object districtResponse;
        Timestamp startTime = TimeUtil.currentTimestamp();
        int requestId = -1;
        ApiRequest apiRequest = getApiRequest(request);


        DistrictRequest districtRequest = createBatchAssignDistrictRequest(apiRequest, provider, geoProvider,
                uspsValidate, usePunct, skipGeocode, districtStrategy);

        districtService.logDistrictRequest(apiRequest, districtRequest);

        if (providersUnsupported(provider, geoProvider, request)) {
            return;
        }

        String batchJsonPayload = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
        List<Address> addresses;
        List<Point> points = new ArrayList<>();
        addresses = getAddressesFromJsonBody(batchJsonPayload);
        if (addresses.isEmpty()) {
            points = getPointsFromJsonBody(batchJsonPayload);
        }
        if (!addresses.isEmpty() || !points.isEmpty()) {
            BatchDistrictRequest batchDistrictRequest = new BatchDistrictRequest(districtRequest);
            batchDistrictRequest.setAddresses(addresses);
            batchDistrictRequest.setPoints(points);

            List<DistrictResult> districtResults = districtService.handleBatchDistrictRequest(batchDistrictRequest);
            districtResponse = new BatchDistrictResponse(districtResults);
        } else {
            districtResponse = new ApiError(this.getClass(), INVALID_BATCH_ADDRESSES);
        }

        setApiResponse(districtResponse, request);
        logElapsedTime(startTime, apiRequest);
    }

    /**
     * District Assignment Api
     * ---------------------------
     * Find the intersection between one type of NY District and another
     * Usage:
     * (GET)    /api/v2/district/intersect
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param sourceType String
     * @param sourceId String
     * @param intersectType String
     */
    @RequestMapping(value = "/intersect", method = RequestMethod.GET)
    public void districtIntersect(HttpServletRequest request, HttpServletResponse response,
                                  @RequestParam String sourceType, @RequestParam String sourceId,
                                  @RequestParam String intersectType) {
        Timestamp startTime = TimeUtil.currentTimestamp();
        ApiRequest apiRequest = getApiRequest(request);

        if (sourceId == null || sourceId.equals("null") || sourceId.isEmpty() || sourceType.equals(intersectType)) {
            var districtResponse = new BaseResponse(ResultStatus.BAD_OVERLAY);
            setApiResponse(districtResponse, request);
        }
        else {
            var intersectRequest = new IntersectRequest(apiRequest, DistrictType.resolveType(sourceType),
                    sourceId, DistrictType.resolveType(intersectType));
            IntersectResult intersectResult = intersectService.handleIntersectRequest(intersectRequest);
            MappedMultiDistrictResponse districtResponse = new MappedMultiDistrictResponse(intersectResult, intersectRequest.intersectWith());
            setApiResponse(districtResponse, request);
        }
        logElapsedTime(startTime, apiRequest);
    }

    /**
     * District Assignment Api
     * ---------------------------
     * Assign a postal address to its corresponding NY Districts
     * Usage:
     * (GET)    /api/v2/district/bluebird
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param provider String
     * @param usePunct boolean
     * @param addr String
     * @param addr1 String
     * @param addr2 String
     * @param city String
     * @param state String
     * @param zip5 String
     * @param zip4 String
     * @param lat String
     * @param lon String
     * @param geoProvider String
     *
     */
    @RequestMapping(value = "/bluebird", method = RequestMethod.GET)
    public void bluebirdAssign(HttpServletRequest request, HttpServletResponse response,
                               @RequestParam(required = false) String provider,
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
        Object districtResponse;
        Timestamp startTime = TimeUtil.currentTimestamp();
        int requestId = -1;
        ApiRequest apiRequest = getApiRequest(request);
        Address address = getAddressFromParams(addr, addr1, addr2, city, state, zip5, zip4);
        DistrictRequest districtRequest = createBlueBirdDistrictRequest(apiRequest, provider, geoProvider, usePunct,
                address, getPointFromParams(lat, lon));

        districtRequest.setAddress(districtRequest.getAddress());

        districtService.logDistrictRequest(apiRequest, districtRequest);

        if (providersUnsupported(provider, geoProvider, request)) {
            return;
        }

        DistrictRequest bluebirdRequest = DistrictRequest.buildBluebirdRequest(districtRequest, bluebirdDistrictStrategy);
        DistrictResult districtResult = districtService.handleDistrictRequest(bluebirdRequest, requestId);
        districtResponse = new DistrictResponse(districtResult);

        setApiResponse(districtResponse, request);
        logElapsedTime(startTime, apiRequest);
    }

    /**
     * District Assignment Api
     * ---------------------------
     * Assign a postal address to its corresponding NY Districts
     * Usage:
     * (POST)    /api/v2/district/bluebird/batch
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param provider String
     * @param usePunct boolean
     * @param geoProvider String
     */
    @RequestMapping(value = "/bluebird/batch", method = RequestMethod.POST)
    public void bluebirdBatchAssign(HttpServletRequest request, HttpServletResponse response,
                                    @RequestParam(required = false) String provider,
                                    @RequestParam(required = false) String geoProvider,
                                    @RequestParam(required = false) boolean usePunct) throws IOException {
        Object districtResponse;
        Timestamp startTime = TimeUtil.currentTimestamp();

        /* Get the ApiRequest */
        ApiRequest apiRequest = getApiRequest(request);

        DistrictRequest districtRequest = createBatchBlueBirdDistrictRequest(apiRequest, provider, geoProvider, usePunct);

        districtService.logDistrictRequest(apiRequest, districtRequest);

        if (providersUnsupported(provider, geoProvider, request)) {
            return;
        }

        String batchJsonPayload = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
        List<Address> addresses = getAddressesFromJsonBody(batchJsonPayload);
        List<Point> points = new ArrayList<>();
        if (addresses.isEmpty()) {
            points = getPointsFromJsonBody(batchJsonPayload);
        }
        if (!addresses.isEmpty() || !points.isEmpty()) {
            DistrictRequest bluebirdRequest = DistrictRequest.buildBluebirdRequest(districtRequest, bluebirdDistrictStrategy);
            BatchDistrictRequest batchBluebirdRequest = new BatchDistrictRequest(bluebirdRequest);
            batchBluebirdRequest.setAddresses(addresses);
            batchBluebirdRequest.setPoints(points);

            List<DistrictResult> districtResults = districtService.handleBatchDistrictRequest(batchBluebirdRequest);
            districtResponse = new BatchDistrictResponse(districtResults);
        } else {
            districtResponse = new ApiError(this.getClass(), INVALID_BATCH_ADDRESSES);
        }

        setApiResponse(districtResponse, request);
        logElapsedTime(startTime, apiRequest);
    }

    private static void logElapsedTime(Timestamp startTime, ApiRequest apiRequest) {
        long elapsedTimeMs = TimeUtil.getElapsedMs(startTime);
        logger.info("{}District Response {} sent in {} ms.", (apiRequest.isBatch() ? " Batch " : " "), apiRequest.getId(), elapsedTimeMs);
    }

    /**
     * If providers are specified then make sure they match the available providers. Send an
     * api error and return if the provider is not supported.
     */
    public static boolean providersUnsupported(String provider, String geoProvider, HttpServletRequest request) {
        ResultStatus errorStatus = null;
        if (provider != null && !provider.isEmpty()) {
            try {
                DistrictSource.valueOf(provider.toUpperCase());
            } catch (IllegalArgumentException e) {
                errorStatus = DISTRICT_PROVIDER_NOT_SUPPORTED;
            }
        }
        try {
            Geocoder.valueOf(geoProvider.toLowerCase().trim());
        } catch (IllegalArgumentException ex) {
            errorStatus = GEOCODE_PROVIDER_NOT_SUPPORTED;
        }
        if (errorStatus != null) {
            setApiResponse(new ApiError(DistrictController.class, errorStatus), request);
        }
        return errorStatus != null;
    }
}
