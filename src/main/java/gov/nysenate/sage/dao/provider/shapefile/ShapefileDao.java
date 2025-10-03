package gov.nysenate.sage.dao.provider.shapefile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.model.county.CountyDao;
import gov.nysenate.sage.dao.model.townCity.TownCityDao;
import gov.nysenate.sage.dao.provider.DistrictNameDao;
import gov.nysenate.sage.model.district.*;
import gov.nysenate.sage.model.geo.*;
import gov.nysenate.sage.util.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static gov.nysenate.sage.dao.provider.shapefile.ShapefileQueries.*;

@Repository
public class ShapefileDao extends BaseDao implements DistrictNameDao {
    private static final Logger logger = LoggerFactory.getLogger(ShapefileDao.class);
    private static final BigDecimal MIN_INTERSECT_SQ_KM = BigDecimal.ONE;
    private static final String geometrySchema = "districts";
    private final CountyDao countyDao;
    private final TownCityDao townCityDao;
    private ImmutableMap<DistrictType, SortedSet<DistrictMap>> districtMapCache;

    @Autowired
    public ShapefileDao(CountyDao countyDao, TownCityDao townCityDao) {
        this.countyDao = countyDao;
        this.townCityDao = townCityDao;
    }

    /**
     * Retrieves a DistrictInfo object based on the districts that intersect the given point.
     * @param geocode        Geocode of interest
     * @param districtTypes  Collection of district types to resolve
     * @return  DistrictInfo if query was successful, null otherwise
     */
    public DistrictInfo getDistrictInfo(Geocode geocode, Set<DistrictType> districtTypes) {
        Map<DistrictType, SingleDistrict> typeToDistrictMap = new HashMap<>();
        for (DistrictType districtType : districtTypes) {
            if (!districtType.hasShapefile()) {
                continue;
            }
            String sql = GET_DISTRICT_FROM_POINT.getSql(geometrySchema, getReplacements(districtType, "type"));
            var params = new MapSqlParameterSource("lat", geocode.lat()).addValue("lon", geocode.lon());
            try {
                SingleDistrict result = namedJdbcTemplate.queryForObject(sql, params,
                        new SingleDistrictMapper(districtType));
                typeToDistrictMap.put(districtType, result);
            } catch (EmptyResultDataAccessException ex) {
                logger.warn("Could not place {} inside a {} district", geocode.point(), districtType);
            }
        }
        return new DistrictInfo(typeToDistrictMap, getMatchLevel(geocode.quality()));
    }

    /**
     * Creates and returns a DistrictOverlap object which contains lists of all districts that contained
     * within a collection of other districts and maps of intersections for senate districts. This is used
     * for a zip/city level match where given a collection of zip codes, gather the other types of districts
     * that overlap the zip area.
     * @param baseType The DistrictType of get overlap info for.
     * @param intersectType    The DistrictType to base the intersections of off.
     * @param refCode           The code that represents the base area.
     */
    public List<IntersectMap> getDistrictOverlap(DistrictType baseType, DistrictType intersectType, String refCode) {
        Map<String, String> replacementMap = getReplacements(intersectType, "intersectType");
        replacementMap.put("baseType", baseType.name().toLowerCase());
        replacementMap.put("baseCodeColumn", baseType.codeColumn());
        var params = new MapSqlParameterSource("districtCode", refCode);

        String sql = GET_INTERSECTION.getSql(geometrySchema, replacementMap);
        return namedJdbcTemplate.query(sql, params, (rs, rowNum) -> {
            String code = rs.getString("code");
            var baseMap = new IntersectMap(intersectType, getDistrictName(intersectType, code), code);
            IntersectMap intersectMap = getDistrictMapFromJson(rs.getString("intersect_geo_json"), baseMap);
            intersectMap.setArea(rs.getBigDecimal("area"));
            intersectMap.setFullMapPolygons(getDistrictMap(intersectType, code).getPolygons());
            return intersectMap;
        }).stream().filter(dm -> dm.getArea().compareTo(MIN_INTERSECT_SQ_KM) > 0).toList();
    }

    /**
     * Retrieves a mapped collection of DistrictMaps.
     * @return Map<DistrictType, List<DistrictMap>>
     */
    public SortedSet<DistrictMap> getDistrictMaps(DistrictType type) {
        return districtMapCache.get(type);
    }

    /**
     * Caches all the district maps from the database.
     */
    @PostConstruct
    public void cacheDistrictMaps() {
        Map<DistrictType, SortedSet<DistrictMap>> tempCache = new HashMap<>();
        for (DistrictType districtType : DistrictType.values()) {
            if (!districtType.hasShapefile()) {
                continue;
            }
            String sql = GET_DISTRICT_MAPS.getSql(geometrySchema, getReplacements(districtType, "type"));
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
        private Set<County> counties = null;
        private Set<TownCity> townCities = null;

        private DistrictCacheMapper(DistrictType type) {
            this.type = type;
            if (type == DistrictType.COUNTY) {
                this.counties = countyDao.getCounties();
            }
            if (type == DistrictType.TOWN_CITY) {
                this.townCities = townCityDao.getTownCities();
            }
        }

        @Override
        public DistrictMap mapRow(@Nonnull ResultSet rs, int rowNum) throws SQLException {
            String code = rs.getString("code");
            // This is the place where names are actually assigned: everything else pulls from these cached values.
            TownCity townCity = null;
            if (type == DistrictType.TOWN_CITY) {
                townCity = townCities.stream().filter(tc -> tc.code().equals(code))
                        .findFirst().orElseThrow();
            }
            String name = switch (type) {
                case SENATE -> "NY Senate District " + code;
                case ASSEMBLY -> "NY Assembly District " + code;
                case CONGRESSIONAL -> "NY Congressional District " + code;
                case TOWN_CITY -> townCity.fullName();
                case ZIP -> "Zipcode " + code;
                case COUNTY -> rs.getString("name") + " County";
                default -> rs.getString("name");
            };
            DistrictMap map = getDistrictMapFromJson(rs.getString("map"), new DistrictMap(type, name, code));
            map.setArea(rs.getBigDecimal("area"));
            // For COVID links
            if (type == DistrictType.COUNTY) {
                Optional<County> countyOpt = counties.stream()
                        .filter(county -> String.valueOf(county.senateCode()).equals(code)).findFirst();
                map.setLink(countyOpt.orElseThrow().link());
            }
            if (type == DistrictType.TOWN_CITY) {
                map.setBaseName(townCity.baseName());
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
            String code = rs.getString("code");
            return new SingleDistrict(code, getDistrictName(type, code));
        }
    }

    public DistrictMap getDistrictMap(DistrictType type, String code) {
        SortedSet<DistrictMap> maps = districtMapCache.get(type);
        if (maps == null) {
            return null;
        }
        return maps.stream().filter(dMap -> dMap.getDistrictCode().equalsIgnoreCase(code))
                .findFirst().orElse(null);
    }

    @Override
    public String getDistrictName(DistrictType type, String code) {
        DistrictMap map = getDistrictMap(type, code);
        return map == null ? null : map.getDistrictName();
    }

    public void cleanMaps(DistrictType type) {
        var replacementMap = getReplacements(type, "type");
        // Leading zeroes are meaningful only in zip codes.
        if (type != DistrictType.ZIP) {
            namedJdbcTemplate.update(CLEAN_CODES.getSql(geometrySchema, replacementMap), Map.of());
        }
        var callbackHandler = new CodeCallbackHandler();
        namedJdbcTemplate.query(GET_CODES.getSql(geometrySchema, replacementMap), callbackHandler);
        for (var tuple : callbackHandler.codeList) {
            var params = new MapSqlParameterSource("code", tuple.first()).addValue("mainGid", tuple.second());
            namedJdbcTemplate.update(SET_UNION.getSql(geometrySchema, replacementMap), params);
            namedJdbcTemplate.update(DELETE_REDUNDANT_MAPS.getSql(geometrySchema, replacementMap), params);
        }
    }

    private static class CodeCallbackHandler implements RowCallbackHandler {
        private final List<Tuple<String, Integer>> codeList = new ArrayList<>();

        @Override
        public void processRow(@Nonnull ResultSet rs) throws SQLException {
            if (rs.getInt("code_count") > 1) {
                codeList.add(new Tuple<>(rs.getString("code"), rs.getInt("main_gid")));
            }
        }
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
