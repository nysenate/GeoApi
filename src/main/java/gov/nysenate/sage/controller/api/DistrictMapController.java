package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.client.response.base.ApiError;
import gov.nysenate.sage.client.response.base.BaseResponse;
import gov.nysenate.sage.client.response.district.DisplayEnumResponse;
import gov.nysenate.sage.client.response.district.IntersectResponse;
import gov.nysenate.sage.client.response.map.MapResponse;
import gov.nysenate.sage.client.response.map.MultipleMapResponse;
import gov.nysenate.sage.dao.model.county.CountyDao;
import gov.nysenate.sage.dao.provider.DistrictNameDao;
import gov.nysenate.sage.model.district.County;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.IntersectResult;
import gov.nysenate.sage.model.result.MapListResult;
import gov.nysenate.sage.model.result.MapResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.provider.district.ShapefileService;
import gov.nysenate.sage.service.ImmutableCache;
import gov.nysenate.sage.service.district.DistrictMemberProvider;
import gov.nysenate.sage.util.FormatUtil;
import gov.nysenate.sage.util.controller.ConstantUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static gov.nysenate.sage.model.result.ResultStatus.BAD_OVERLAY;

@RestController
@RequestMapping(value = ConstantUtil.REST_PATH + "map")
public class DistrictMapController extends BaseDistrictController<DistrictType> {
    private static final Logger logger = LoggerFactory.getLogger(DistrictMapController.class);
    private final ShapefileService shapefileService;
    private final ImmutableCache<String, URI> linkCache;

    @Autowired
    public DistrictMapController(DistrictNameDao nameDao, ShapefileService shapefileService,
                                 DistrictMemberProvider memberProvider, CountyDao countyDao) {
        super(nameDao, shapefileService, memberProvider);
        this.shapefileService = shapefileService;
        this.linkCache = new ImmutableCache<>(() -> countyDao.getCounties()
                .stream().collect(Collectors.toMap(county -> String.valueOf(county.code()), County::link)));
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
                            @RequestParam(required = false) String district,
                            @RequestParam(required = false) boolean showMembers,
                            @RequestParam(required = false) boolean meta) {
        DistrictType districtType = getValue(distType);
        if (district == null) {
            logger.debug("Retrieving all {} district maps.", districtType.name());
            Map<String, DistrictMap> mapResult = shapefileService.getMapCache().get(districtType);
            if (mapResult == null) {
                return new ApiError(ResultStatus.UNSUPPORTED_DISTRICT_MAP);
            }

            var response = new MultipleMapResponse(new MapListResult(mapResult));
            response.getMapViews().forEach(
                    dmv -> assignData(dmv, showMembers, !meta)
            );
            if (districtType == DistrictType.COUNTY) {
                response.getMapViews().forEach(
                        dmv -> dmv.setLink(linkCache.get(dmv.getDistrict()))
                );
            }
            return response;
        }

        district = FormatUtil.cleanString(district);
        logger.debug("Retrieving {} district {} map.", districtType.name(), district);
        MapResult mapResult = shapefileService.getMapResult(districtType, district);
        var response = new MapResponse(mapResult);
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
    public Object districtIntersect(@RequestParam String sourceType, @RequestParam String sourceId,
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
