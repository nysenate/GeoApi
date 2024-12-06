package gov.nysenate.sage.provider.district;

import gov.nysenate.sage.dao.model.county.CountyDao;
import gov.nysenate.sage.dao.provider.district.SqlDistrictShapefileDao;
import gov.nysenate.sage.model.address.DistrictedAddress;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.district.*;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.model.result.IntersectResult;
import gov.nysenate.sage.model.result.MapResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.util.FormatUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static gov.nysenate.sage.model.result.ResultStatus.INSUFFICIENT_GEOCODE;

@Service
public class DistrictShapefile extends DistrictService implements MapService {
    private final SqlDistrictShapefileDao sqlDistrictShapefileDao;

    private final CountyDao countyDao;

    /** We should only attempt to assign districts to a geocode if it is accurate enough.
     * i.e. We can't accurately assign a district to a ZIP, CITY, or STATE quality geocode. */
    private static final List<GeocodeQuality> DISTRICT_ASSIGNABLE_GEOCODE_QUALITIES =
            List.of(GeocodeQuality.HOUSE, GeocodeQuality.POINT);

    @Autowired
    public DistrictShapefile(SqlDistrictShapefileDao sqlDistrictShapefileDao, CountyDao countyDao) {
        this.sqlDistrictShapefileDao = sqlDistrictShapefileDao;
        this.countyDao = countyDao;
    }

    /** {@inheritDoc} */
    public DistrictResult assignDistricts(GeocodedAddress geocodedAddress, List<DistrictType> reqTypes, boolean getSpecialMaps) {
        var districtResult = new DistrictResult(districtSource(), geocodedAddress, true, false);
        if (!districtResult.isSuccess()) {
            return districtResult;
        }
        if (!DISTRICT_ASSIGNABLE_GEOCODE_QUALITIES.contains(geocodedAddress.getGeocode().quality())) {
            districtResult.setStatusCode(INSUFFICIENT_GEOCODE);
            return districtResult;
        }
        Geocode geocode = geocodedAddress.getGeocode();
        DistrictInfo districtInfo = sqlDistrictShapefileDao.getDistrictInfo(geocode.point(), reqTypes, getSpecialMaps);
        districtResult.setDistrictedAddress(new DistrictedAddress(geocodedAddress, districtInfo, DistrictMatchLevel.HOUSE));
        districtResult.setResultTime();

        return districtResult;
    }

    @Override
    public DistrictSource districtSource() {
        return DistrictSource.SHAPEFILE;
    }

    /** {@inheritDoc} */
    @Override
    public DistrictResult assignDistricts(GeocodedAddress geocodedAddress, List<DistrictType> reqTypes) {
        return assignDistricts(geocodedAddress, reqTypes, true);
    }

    /** {@inheritDoc} */
    @Override
    public DistrictResult assignDistrictsForBatch(GeocodedAddress geocodedAddress, List<DistrictType> reqTypes) {
        return assignDistricts(geocodedAddress, reqTypes, false);
    }

    /** {@inheritDoc} */
    @Override
    public MapResult getDistrictMap(DistrictType districtType, String code) {
        var mapResult = new MapResult(MapSource.SHAPEFILE);
        if (code != null && !code.isEmpty()) {
            code = FormatUtil.trimLeadingZeroes(code);
            var strToDistMap = sqlDistrictShapefileDao.getCodeToDistrictMapMap(districtType);
            if (strToDistMap != null) {
                DistrictMap map = strToDistMap.get(code);
                if (map != null) {
                    if (districtType.equals(DistrictType.COUNTY)) { //This if block is for the COVID19 links
                        map.setLink(countyDao.getCountyBySenateCode(Integer.parseInt(code)).link());
                    }
                    mapResult.setDistrictMap(map);
                    mapResult.setStatusCode(ResultStatus.SUCCESS);
                }
                else {
                    mapResult.setStatusCode(ResultStatus.NO_MAP_RESULT);
                }
            }
            else {
                mapResult.setStatusCode(ResultStatus.UNSUPPORTED_DISTRICT_MAP);
            }
        }
        else {
            mapResult.setStatusCode(ResultStatus.MISSING_DISTRICT_CODE);
        }
        return mapResult;
    }

    /** {@inheritDoc} */
    @Override
    public MapResult getDistrictMaps(DistrictType districtType) {
        MapResult mapResult = new MapResult(MapSource.SHAPEFILE);
        List<DistrictMap> mapCollection = sqlDistrictShapefileDao.getDistrictMaps(districtType);
        if (mapCollection != null) {
            mapResult.setDistrictMaps(mapCollection);
            mapResult.setStatusCode(ResultStatus.SUCCESS);
            if (districtType.equals(DistrictType.COUNTY)) {
                for (DistrictMap map : mapCollection) {
                    map.setLink(countyDao.getCountyBySenateCode(Integer.parseInt(map.getDistrictCode())).link());
                }
            }
        }
        else {
            mapResult.setStatusCode(ResultStatus.NO_MAP_RESULT);
        }
        return mapResult;
    }

    /**
     * Attempts to obtain overlapping district information for a specific district of arbitrary type.
     * @param districtType DistrictType the DistrictType of the district to get intersections with
     * @param districtId String the id of the district to get intersections with
     * @param intersectType DistrictType the type of district we are searching for intersections with districtId
     * @return DistrictResult with overlaps set.
     */
    public IntersectResult getIntersectionResult(DistrictType districtType, String districtId, DistrictType intersectType) {
        DistrictMap sourceMap = sqlDistrictShapefileDao.getOverlapReferenceBoundary(districtType, Set.of(districtId));
        // We only need the overlap for the specified intersect type
        DistrictOverlap overlap = sqlDistrictShapefileDao.getDistrictOverlap(intersectType, null,
                districtType, Set.of(districtId));
        return new IntersectResult(MapSource.SHAPEFILE, sourceMap, overlap);
    }
}
