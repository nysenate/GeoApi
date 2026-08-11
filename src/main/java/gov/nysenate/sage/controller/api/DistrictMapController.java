package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.client.response.base.BaseResponse;
import gov.nysenate.sage.client.response.district.DisplayEnumResponse;
import gov.nysenate.sage.client.response.district.IntersectResponse;
import gov.nysenate.sage.client.response.map.MapGeometryResponse;
import gov.nysenate.sage.client.response.map.MultipleMapGeometryResponse;
import gov.nysenate.sage.model.district.*;
import gov.nysenate.sage.model.result.IntersectResult;
import gov.nysenate.sage.model.result.MapListResult;
import gov.nysenate.sage.model.result.MapResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.provider.district.ShapefileService;
import gov.nysenate.sage.service.district.DistrictMemberProvider;
import gov.nysenate.sage.service.district.DistrictInfoCache;
import gov.nysenate.sage.util.controller.ConstantUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import static gov.nysenate.sage.model.result.ResultStatus.BAD_OVERLAY;

@RestController
@RequestMapping(value = ConstantUtil.REST_PATH + "map")
public class DistrictMapController extends BaseDistrictController<DistrictType> {
    private static final Logger logger = LoggerFactory.getLogger(DistrictMapController.class);
    private final ShapefileService shapefileService;

    @Autowired
    public DistrictMapController(DistrictInfoCache infoCache, ShapefileService shapefileService,
                                 DistrictMemberProvider memberProvider) {
        super(infoCache, shapefileService, memberProvider);
        this.shapefileService = shapefileService;
    }

    // The @GetMapping is inherited.
    @Override
    public List<DisplayEnumResponse> options() {
        return mapCache.getMap().keySet().stream().sorted().map(DisplayEnumResponse::new).toList();
    }

    /**
     * District Map Api
     * ---------------------------
     * Gets all maps of a single DistrictType, or a single district's map.
     * @see DistrictType
     * Usage:
     * (GET)    /api/v2/map/{distType}
     */
    @GetMapping(value = "/{distType}")
    public BaseResponse map(@PathVariable String distType,
                            @RequestParam(required = false) DistrictId district,
                            @RequestParam(required = false) boolean showMembers,
                            @RequestParam(required = false) boolean meta) {
        DistrictType districtType = getValue(distType);
        if (district == null) {
            logger.debug("Retrieving all {} district maps.", districtType.name());
            Map<DistrictId, DistrictMap> mapResult = shapefileService.getMapCache().get(districtType);
            if (mapResult == null) {
                return new ApiError(ResultStatus.UNSUPPORTED_DISTRICT_MAP);
            }

            var response = new MultipleMapGeometryResponse(new MapListResult(mapResult));
            response.getMapViews().forEach(
                    dmv -> assignData(dmv, showMembers, !meta)
            );
            if (districtType == DistrictType.COUNTY) {
                Map<DistrictId, DistrictInfo> countyInfoMap = infoCache.get(DistrictType.COUNTY);
                response.getMapViews().forEach(
                        dmv -> {
                            DistrictInfo currInfo = countyInfoMap.get(dmv.getId());
                            if (currInfo != null) {
                                dmv.setLink(currInfo.get("link"));
                            }
                        });
            }
            return response;
        }

        logger.debug("Retrieving {} district {} map.", districtType.name(), district);
        MapResult mapResult = shapefileService.getMapResult(districtType, district);
        var response = new MapGeometryResponse(mapResult);
        assignData(response.getMapView(), showMembers, !meta);
        return response;
    }

    /**
     * Intersect Api
     * ---------------------------
     * Find the intersection between one type of NY District and another
     * Usage:
     * (GET)    /api/v2/map/intersect
     */
    @GetMapping(value = "/intersect")
    public Object districtIntersect(@RequestParam String sourceType, @RequestParam DistrictId sourceId,
                                    @RequestParam String intersectType) {
        if (sourceType.equalsIgnoreCase(intersectType)) {
            return new BaseResponse(BAD_OVERLAY);
        }
        IntersectResult intersectResult = shapefileService.getIntersectResult(
                getValue(DistrictType.class, sourceType), sourceId, getValue(DistrictType.class, intersectType));
        var response = IntersectResponse.from(intersectResult);
        // The maps currently contain just the overlapped portion, which we don't want to override.
        response.overlaps().forEach(overlap -> assignData(overlap, true, false));
        return response;
    }
}
