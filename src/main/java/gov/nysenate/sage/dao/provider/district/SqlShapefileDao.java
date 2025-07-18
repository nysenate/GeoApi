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
 * DistrictShapefileDao utilizes a PostGIS database loaded with LATFOR/GIS shapefiles to
 * provide fast district resolution given a coordinate pair. It also allows for determining
 * overlaps and intersections between districts.
 */
@Repository
public class SqlShapefileDao extends BaseDao implements ShapefileDao {
    private static final Logger logger = LoggerFactory.getLogger(SqlShapefileDao.class);
    private static final BigDecimal MIN_INTERSECT_SQ_KM = BigDecimal.ONE;
    private final CountyDao countyDao;
    private ImmutableMap<DistrictType, SortedSet<DistrictMap>> districtMapCache = ImmutableMap.of();

    @Autowired
    public SqlShapefileDao(CountyDao countyDao) {
        this.countyDao = countyDao;
    }

    @PostConstruct
    private void init() {
        cacheDistrictMaps();
    }

    /** {@inheritDoc} */
    public DistrictInfo getDistrictInfo(Geocode geocode, Set<DistrictType> districtTypes) {
        Map<DistrictType, SingleDistrict>  typeToDistrictMap = new HashMap<>();
        for (DistrictType districtType : districtTypes) {
            if (!districtType.hasShapefile()) {
                continue;
            }
            String sql = GET_DISTRICT_FROM_POINT.getSql("districts", getReplacements(districtType, "type"));
            SqlParameterSource params = new MapSqlParameterSource("lat", geocode.lat())
                    .addValue("lon", geocode.lon());
            SingleDistrict result = namedJdbcTemplate.queryForObject(sql, params,
                    new SingleDistrictMapper(districtType));
            typeToDistrictMap.put(districtType, result);
        }
        return new DistrictInfo(typeToDistrictMap, getMatchLevel(geocode.quality()));
    }

    /** {@inheritDoc} */
    public List<IntersectMap> getDistrictOverlap(DistrictType baseType, DistrictType intersectType, String refCode) {
        Map<String, String> replacementMap = getReplacements(intersectType, "intersectType");
        replacementMap.put("baseType", baseType.name().toLowerCase());
        replacementMap.put("baseCodeColumn", baseType.codeColumn());
        if (baseType == DistrictType.COUNTY) {
            refCode = countyDao.getFipsCode(refCode);
        }
        var params = new MapSqlParameterSource("districtCode", refCode);

        String sql = GET_INTERSECTION.getSql("districts", replacementMap);
        return namedJdbcTemplate.query(sql, params, (rs, rowNum) -> {
            IntersectMap intersectMap = getDistrictMapFromJson(rs.getString("intersect_geo_json"), new IntersectMap());
            intersectMap.setDistrictType(intersectType);
            String code = getDistrictCode(rs, intersectType);
            intersectMap.setDistrictCode(code);
            intersectMap.setDistrictName(getDistrictName(intersectType, code));
            intersectMap.setArea(rs.getBigDecimal("area"));
            intersectMap.setFullMapPolygons(getDistrictMap(intersectType, code).getPolygons());
            return intersectMap;
        }).stream().filter(dm -> dm.getArea().compareTo(MIN_INTERSECT_SQ_KM) > 0).toList();
    }

    /** {@inheritDoc} */
    public SortedSet<DistrictMap> getDistrictMaps(DistrictType type) {
        return districtMapCache.get(type);
    }

    /** {@inheritDoc} */
    public void cacheDistrictMaps() {
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
            String code = getDistrictCode(rs, type);
            // This is the place where names are actually assigned: everything else pulls from these cached values.
            String name = switch (type) {
                case SENATE -> "NY Senate District " + code;
                case ASSEMBLY -> "NY Assembly District " + code;
                case CONGRESSIONAL -> "NY Congressional District " + code;
                case TOWN_CITY -> (code.startsWith("-") ? "City" : "Town") + " of " + rs.getString("name");
                case ZIP -> "Zipcode " + code;
                case COUNTY -> rs.getString("name") + " County";
                default -> rs.getString("name");
            };
            var metadata = new DistrictMetadata(type, name, code);
            DistrictMap map = getDistrictMapFromJson(rs.getString("map"), new DistrictMap());
            map.setDistrictMetadata(metadata);
            map.setArea(rs.getBigDecimal("area"));
            // For COVID links
            if (type == DistrictType.COUNTY) {
                map.setLink(countyDao.getLinkBySenateCode(code));
            }
            return map;
        }
    }

    private class SingleDistrictMapper implements RowMapper<SingleDistrict> {
        private final DistrictType type;

        private SingleDistrictMapper(DistrictType type) {
            this.type = type;
        }

        @Override
        public SingleDistrict mapRow(@Nonnull ResultSet rs, int rowNum) throws SQLException {
            String code = getDistrictCode(rs, type);
            return new SingleDistrict(code, getDistrictName(type, code));
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
            code = countyDao.getSenateCodeStr(rs.getInt("code"));
        }
        // Normal district code
        else {
            code = rs.getString("code");
        }
        return FormatUtil.trimLeadingZeroes(code).trim();
    }

    @Override
    public DistrictMap getDistrictMap(DistrictType type, String code) {
        if (!districtMapCache.containsKey(type)) {
            return null;
        }
        return districtMapCache.get(type).stream()
                .filter(dMap -> dMap.getDistrictCode().equalsIgnoreCase(code))
                .findFirst().orElse(null);
    }

    @Override
    public String getDistrictName(DistrictType type, String code) {
        DistrictMap map = getDistrictMap(type, code);
        return map == null ? null : map.getDistrictName();
    }

    /**
     * Parses JSON map response and creates a DistrictMap object containing the district geometry.
     * This method does not set any other fields on the DistrictMap object.
     * @param jsonMap   GeoJson string containing the district geometry
     * @return          DistrictMap containing the geometry.
     *                  null if map string not present or error
     */
    private static <T extends DistrictMap> T getDistrictMapFromJson(String jsonMap, T districtMap) {
        if (jsonMap == null || jsonMap.isEmpty() || jsonMap.equals("null")) {
            return null;
        }
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
