package gov.nysenate.sage.dao.provider.district;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.model.county.CountyDao;
import gov.nysenate.sage.model.district.*;
import gov.nysenate.sage.model.geo.*;
import gov.nysenate.sage.util.FormatUtil;
import gov.nysenate.sage.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static gov.nysenate.sage.dao.provider.district.ShapefileQueries.*;

/**
 * DistrictShapefileDao utilizes a PostGIS database loaded with Census shapefiles to
 * provide fast district resolution given a coordinate pair. It also allows for determining
 * overlaps and intersections between districts.
 */
// TODO: be sure to resolve county stuff correctly
// TODO: cache schools
@Repository
public class SqlShapefileDao extends BaseDao implements ShapefileDao {
    private static final Logger logger = LoggerFactory.getLogger(SqlShapefileDao.class);
    private final CountyDao countyDao;
    private ImmutableMap<DistrictType, SortedSet<DistrictMap>> districtMapCache = ImmutableMap.of();

    @Autowired
    public SqlShapefileDao(CountyDao countyDao) {
        this.countyDao = countyDao;
    }

    @PostConstruct
    private void init() {
        if (!cacheDistrictMaps()) {
            throw new RuntimeException("Failed to initialize district map cache");
        }
    }

    /** {@inheritDoc} */
    public DistrictInfo getDistrictInfo(Geocode geocode, List<DistrictType> districtTypes) {
        var districtInfo = new DistrictInfo();
        districtInfo.setMatchLevel(getMatchLevel(geocode.quality()));
        for (DistrictType districtType : districtTypes) {
            if (!districtType.hasShapefile()) {
                continue;
            }
            String sql = GET_DISTRICT_FROM_POINT.getSql("districts", getReplacements(districtType, "type"));
            SqlParameterSource params = new MapSqlParameterSource("lat", geocode.point().lat())
                    .addValue("lon", geocode.point().lon());
            Pair<String> result = namedJdbcTemplate.queryForObject(sql, params,
                    (rs, rowNum) -> new Pair<>(rs.getString("name"), rs.getString("code"))
            );
            districtInfo.setDistName(districtType, result.first());
            districtInfo.setDistCode(districtType, result.second());
        }
        return districtInfo;
    }

    /** {@inheritDoc} */
    public List<DistrictMap> getDistrictOverlap(DistrictType baseType, DistrictType intersectType, String refCode) {
        Map<String, String> replacementMap = getReplacements(intersectType, "intersectType");
        replacementMap.put("baseType", baseType.name().toLowerCase());
        replacementMap.put("baseCodeColumn", baseType.codeColumn());
        // Only town/city has codes that aren't numbers.
        SqlParameterSource params = new MapSqlParameterSource("districtCode",
                baseType == DistrictType.TOWN_CITY ? refCode : Integer.parseInt(refCode));

        String sql = GET_INTERSECTION.getSql("districts", replacementMap);
        return namedJdbcTemplate.query(sql, params, (rs, rowNum) -> {
            DistrictMap intersectMap = getDistrictMapFromJson(rs.getString("intersect_geo_json"));
            intersectMap.setDistrictType(intersectType);
            intersectMap.setDistrictName(rs.getString("name"));
            intersectMap.setDistrictCode(getDistrictCode(rs, intersectType));
            intersectMap.setArea(rs.getBigDecimal("area"));
            return intersectMap;
        }).stream().filter(dm -> !dm.getArea().equals(BigDecimal.ZERO)).toList();
    }

    /** {@inheritDoc} */
    public List<DistrictMap> getDistrictMaps(DistrictType type) {
        SortedSet<DistrictMap> districtMaps = districtMapCache.get(type);
        return districtMaps == null ? List.of() : districtMaps.stream().toList();
    }

    /** {@inheritDoc} */
    public boolean cacheDistrictMaps() {
        Map<DistrictType, SortedSet<DistrictMap>> tempCache = new HashMap<>();
        for (DistrictType districtType : DistrictType.values()) {
            if (!districtType.hasShapefile()) {
                continue;
            }
            String sql = GET_DISTRICT_MAP.getSql("districts", getReplacements(districtType, "type"));
            SortedSet<DistrictMap> currDistrictMapSet = new TreeSet<>(
                    namedJdbcTemplate.query(sql, new DistrictCacheMapper(districtType))
            );
            tempCache.put(districtType, currDistrictMapSet);
        }
        this.districtMapCache = ImmutableSortedMap.copyOf(tempCache);
        return true;
    }

    private static Map<String, String> getReplacements(DistrictType districtType, String typeReplacementName) {
        return new HashMap<>(Map.of(typeReplacementName, districtType.name().toLowerCase(),
                "codeColumn", districtType.codeColumn(), "nameColumn", districtType.nameColumn()));
    }

    private class DistrictCacheMapper implements RowMapper<DistrictMap> {
        private final DistrictType type;

        private DistrictCacheMapper(DistrictType type) {
            this.type = type;
        }

        @Override
        public DistrictMap mapRow(@Nonnull ResultSet rs, int rowNum) throws SQLException {
            var metadata = new DistrictMetadata(type, rs.getString("name"), getDistrictCode(rs, type));
            DistrictMap map = getDistrictMapFromJson(rs.getString("map"));
            map.setDistrictMetadata(metadata);
            map.setArea(rs.getBigDecimal("area"));
            return map;
        }
    }

    /**
     * Retrieves the district code from the result set and performs any necessary corrections.
     * Requires that the result set contains the 'code' column.
     */
    private String getDistrictCode(ResultSet rs, DistrictType type) throws SQLException {
        if (rs == null) {
            return null;
        }
        String code;
        // County codes need to be mapped from FIPS code
        if (type == DistrictType.COUNTY) {
            // TODO: new county data
            code = Integer.toString(countyDao.getSenateCode(rs.getInt("code")));
        }
        // Normal district code
        else {
            code = rs.getString("code");
            if (code != null) {
                code = code.trim();
            }
        }
        return FormatUtil.trimLeadingZeroes(code);
    }

    @Override
    public DistrictMap getDistrictMap(DistrictType type, String district) {
        DistrictMap byName = districtMapCache.get(type).stream()
                .filter(dMap -> dMap.getDistrictName().equalsIgnoreCase(district))
                .findFirst().orElse(null);
        if (byName == null) {
            return districtMapCache.get(type).stream()
                    .filter(dMap -> dMap.getDistrictCode().equalsIgnoreCase(district))
                    .findFirst().orElse(null);
        }
        return byName;
    }

    /**
     * Parses JSON map response and creates a DistrictMap object containing the district geometry.
     * This method does not set any other fields on the DistrictMap object.
     * @param jsonMap   GeoJson string containing the district geometry
     * @return          DistrictMap containing the geometry.
     *                  null if map string not present or error
     */
    private static DistrictMap getDistrictMapFromJson(String jsonMap) {
        if (jsonMap == null || jsonMap.isEmpty() || jsonMap.equals("null")) {
            return null;
        }
        var districtMap = new DistrictMap();
        var objectMapper = new ObjectMapper();
        try {
            JsonNode mapNode = objectMapper.readTree(jsonMap);
            String type = mapNode.get("type").asText();
            GeometryTypes geoType;
            try {
                geoType = GeometryTypes.valueOf(type.toUpperCase());
                districtMap.setGeometryType(geoType.getType());
            }
            catch (Exception ex) {
                logger.debug("Geometry type {} is not supported by this method!", type);
                return null;
            }
            JsonNode coordinates = mapNode.get("coordinates");
            for (int i = 0; i < coordinates.size(); i++) {
                List<Point> points = new ArrayList<>();
                JsonNode polygon = (geoType.equals(GeometryTypes.MULTIPOLYGON)) ? coordinates.get(i).get(0) : coordinates.get(i);
                for (int j = 0; j < polygon.size(); j++){
                    points.add(new Point(polygon.get(j).get(1).asText(), polygon.get(j).get(0).asText()));
                }
                districtMap.addPolygon(new Polygon(points));
            }
            return districtMap;
        }
        catch (IOException ex) {
            logger.error("{}", String.valueOf(ex));
            return null;
        }
    }

    private static DistrictMatchLevel getMatchLevel(GeocodeQuality quality) {
        return switch (quality) {
            case POINT, HOUSE -> DistrictMatchLevel.HOUSE;
            case ZIP_EXT, STREET -> DistrictMatchLevel.STREET;
            case ZIP -> DistrictMatchLevel.ZIP5;
            case CITY -> DistrictMatchLevel.CITY;
            default -> DistrictMatchLevel.NOMATCH;
        };
    }
}
