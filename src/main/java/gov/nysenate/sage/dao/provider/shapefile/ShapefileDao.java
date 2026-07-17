package gov.nysenate.sage.dao.provider.shapefile;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.model.county.CountyDao;
import gov.nysenate.sage.dao.model.townCity.TownCityDao;
import gov.nysenate.sage.model.district.*;
import gov.nysenate.sage.model.geo.*;
import gov.nysenate.sage.util.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static gov.nysenate.sage.dao.provider.shapefile.ShapefileQueries.*;

@Repository
public class ShapefileDao extends BaseDao {
    private static final Logger logger = LoggerFactory.getLogger(ShapefileDao.class);
    private static final String geometrySchema = "districts";
    private final CountyDao countyDao;
    private final TownCityDao townCityDao;

    @Autowired
    public ShapefileDao(CountyDao countyDao, TownCityDao townCityDao) {
        this.countyDao = countyDao;
        this.townCityDao = townCityDao;
    }

    /**
     * Retrieves a DistrictInfo object based on the districts that intersect the given point.
     * @param point          Point of interest
     * @param tableInfos     Collection of district table infos to resolve
     * @return  DistrictInfo if query was successful, null otherwise
     */
    public Map<DistrictType, String> getCodes(Point point, Set<DistrictTableInfo> tableInfos) {
        Map<DistrictType, String> typeToDistrictMap = new HashMap<>();
        for (DistrictTableInfo tableInfo : tableInfos) {
            Map<String, String> replacementMap;
            try {
                replacementMap = tableInfo.getReplacements("type");
            } catch (NoShapefileForDistrictTypeException ignored) {
                continue;
            }
            String sql = GET_DISTRICT_FROM_POINT.getSql(geometrySchema, replacementMap);
            var params = new MapSqlParameterSource("lat", point.lat()).addValue("lon", point.lon());
            try {
                String code = namedJdbcTemplate.queryForObject(sql, params, String.class);
                typeToDistrictMap.put(tableInfo.type(), code);
            } catch (EmptyResultDataAccessException ex) {
                if (tableInfo.type().coversState()) {
                    logger.warn("Could not place {} inside a {} district", point, tableInfo.type());
                }
            }
        }
        return typeToDistrictMap;
    }

    /**
     * Creates and returns a DistrictOverlap object which contains lists of all districts that contained
     * within a collection of other districts and maps of intersections for senate districts. This is used
     * for a region level match where given a collection of zip codes, gather the other types of districts
     * that overlap the zip area.
     * @param baseTypeInfo      What to get overlap info for.
     * @param intersectType     The DistrictType to base the intersections of off.
     * @param refCode           The code that represents the base area.
     */
    public List<IntersectInfo> getDistrictOverlap(DistrictTableInfo baseTypeInfo, DistrictTableInfo intersectType, String refCode) {
        Map<String, String> replacementMap = intersectType.getReplacements("intersectType");
        replacementMap.put("baseType", baseTypeInfo.type().name().toLowerCase());
        replacementMap.put("baseCodeColumn", baseTypeInfo.codeColumn());
        var params = new MapSqlParameterSource("districtCode", refCode);

        String sql = GET_INTERSECTION.getSql(geometrySchema, replacementMap);
        return namedJdbcTemplate.query(sql, params, (rs, rowNum) ->
                new IntersectInfo(rs.getString("code"), rs.getString("intersect_geo_json"), rs.getBigDecimal("area"))
        );
    }

    public SortedSet<DistrictMap> getDistrictMaps(DistrictTableInfo tableInfo) {
        String sql = GET_DISTRICT_MAPS.getSql(geometrySchema, tableInfo.getReplacements("type"));
        return new TreeSet<>(namedJdbcTemplate.query(sql, new DistrictCacheMapper(tableInfo)));
    }

    private class DistrictCacheMapper implements RowMapper<DistrictMap> {
        private final DistrictType type;
        private Set<County> counties = null;
        private Set<TownCity> townCities = null;

        private DistrictCacheMapper(DistrictTableInfo tableInfo) {
            this.type = tableInfo.type();
            if (type == DistrictType.COUNTY) {
                this.counties = countyDao.getCounties(tableInfo);
            }
            if (type == DistrictType.TOWN_CITY) {
                this.townCities = townCityDao.getTownCities(tableInfo);
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
                case SENATE -> "Senate District " + code;
                case ASSEMBLY -> "Assembly District " + code;
                case CONGRESSIONAL -> "Congressional District " + code;
                case ZIP -> "Zipcode " + code;
                case COUNTY -> rs.getString("name") + " County";
                case TOWN_CITY -> townCity.fullName();
                case CITY_COUNCIL -> "Council District " + code;
                case VILLAGE -> "Village of " + rs.getString("name");
                default -> rs.getString("name");
            };
            var map = new DistrictMap(type, name, code);
            map.setMapGeoJson(rs.getString("map"));
            map.setArea(rs.getBigDecimal("area"));
            // For COVID links
            if (type == DistrictType.COUNTY) {
                String baseName = rs.getString("name");
                Optional<County> countyOpt = counties.stream()
                        .filter(county -> county.name().equals(baseName)).findFirst();
                map.setLink(countyOpt.orElseThrow().link());
            }
            if (type == DistrictType.TOWN_CITY) {
                map.setBaseName(townCity.baseName());
            }
            return map;
        }
    }

    /**
     * Cleans the maps for a specific type. Note that the update_district_geometry.sh script handles
     * the initial insert of new geometry data.
     * @return null if the type's table is empty, true if all of type's geometry is valid, and false otherwise.
     */
    public Boolean cleanMaps(DistrictTableInfo tableInfo) {
        Map<String, String> replacementMap = tableInfo.getReplacements("type");
        // Leading zeroes are meaningful only in zip codes.
        if (tableInfo.type() != DistrictType.ZIP) {
            try {
                namedJdbcTemplate.update(TRIM_CODES.getSql(geometrySchema, replacementMap), Map.of());
            } catch (BadSqlGrammarException ex) {
                logger.warn("The code appears to be numeric. Skipping trimming...");
            }
        }
        var callbackHandler = new CodeCallbackHandler();
        namedJdbcTemplate.query(GET_CODES.getSql(geometrySchema, replacementMap), callbackHandler);
        for (var tuple : callbackHandler.codeList) {
            var params = new MapSqlParameterSource("code", tuple.first()).addValue("mainGid", tuple.second());
            namedJdbcTemplate.update(SET_UNION.getSql(geometrySchema, replacementMap), params);
            namedJdbcTemplate.update(DELETE_REDUNDANT_MAPS.getSql(geometrySchema, replacementMap), params);
        }
        return namedJdbcTemplate.getJdbcOperations().queryForObject(
                IS_TYPE_VALID.getSql(geometrySchema, replacementMap), Boolean.class
        );
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
}
