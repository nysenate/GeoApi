package gov.nysenate.sage.dao.provider.district;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.model.county.CountyDao;
import gov.nysenate.sage.model.district.*;
import gov.nysenate.sage.model.geo.GeometryTypes;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.geo.Polygon;
import gov.nysenate.sage.util.FormatUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * DistrictShapefileDao utilizes a PostGIS database loaded with Census shapefiles to
 * provide fast district resolution given a coordinate pair. It also allows for determining
 * overlaps and intersections between districts.
 */
// TODO: be sure to resolve county stuff correctly
// TODO: cache schools
@Repository
public class SqlDistrictShapefileDao extends BaseDao implements DistrictShapeFileDao {
    private static final Logger logger = LoggerFactory.getLogger(SqlDistrictShapefileDao.class);
    private final CountyDao countyDao;
    private ImmutableMultimap<DistrictType, DistrictMap> districtMapCache = ImmutableMultimap.of();

    @Autowired
    public SqlDistrictShapefileDao(CountyDao countyDao) {
        this.countyDao = countyDao;
    }

    @PostConstruct
    private void init() {
        if (!cacheDistrictMaps()) {
            throw new RuntimeException("Failed to initialize district map cache");
        }
    }

    /** {@inheritDoc} */
    public DistrictInfo getDistrictInfo(Point point, List<DistrictType> districtTypes) {
        String sqlTmpl =
                "SELECT '%s' AS type, %s AS name, %s as code " +
                "FROM districts.%s " +
                "WHERE ST_CONTAINS(geom, ST_PointFromText('POINT(%f %f)'))";

        // Iterate through all the requested types and format the template sql
        ArrayList<String> queryList = new ArrayList<>();
        for (DistrictType districtType : districtTypes) {
            String nameColumn = districtType.nameColumn();
            if (nameColumn != null) {
                queryList.add(String.format(sqlTmpl, districtType, nameColumn, districtType.codeColumn(),
                        districtType, point.lon(), point.lat())); // lon,lat is correct order
            }
        }

        // Combine the queries using UNION ALL
        String sqlQuery = StringUtils.join(queryList, " UNION ALL ");

        try {
            return jdbcTemplate.query(sqlQuery, new DistrictInfoHandler());
        } catch (Exception ex) {
            logger.error("{}", String.valueOf(ex));
        }
        return null;
    }

    /** {@inheritDoc} */
    public DistrictOverlap getDistrictOverlap(DistrictType baseType, DistrictType intersectType, String refCode) {
        var params = new MapSqlParameterSource("districtCode", refCode)
                .addValue("nameField", intersectType.nameColumn())
                .addValue("codeField", intersectType.codeColumn());
        // Replacing parameters doesn't play nice when they are followed by a period.
        String sql = ShapefileQueries.GET_INTERSECTION.getSql("districts")
                .replaceAll(":baseType", baseType.name())
                .replaceAll(":intersectType", intersectType.name());

        return namedJdbcTemplate.query(sql, params, new DistrictOverlapHandler());
    }

    /** {@inheritDoc} */
    public List<DistrictMap> getDistrictMaps(DistrictType type) {
        return districtMapCache.get(type).stream().toList();
    }

    /** {@inheritDoc} */
    public boolean cacheDistrictMaps() {
        Multimap<DistrictType, DistrictMap> tempMultimap = ArrayListMultimap.create();
        String baseSql = "SELECT %s as name, %s as code, ST_AsGeoJson(ST_Union(geom)) AS map " +
                     "FROM districts.%s " +
                     "GROUP BY %s, %s";

        Set<DistrictType> types = new HashSet<>(DistrictType.getStandardTypes());
        types.add(DistrictType.ELECTION);
        for (DistrictType districtType : types) {
            if (districtType.nameColumn() == null) {
                continue;
            }
            String nameColumn = districtType.nameColumn();
            String codeColumn = districtType.codeColumn();
            String currSql = String.format(baseSql, nameColumn, codeColumn, districtType,
                                                           nameColumn, codeColumn);
            List<DistrictMap> maps = namedJdbcTemplate.query(currSql, new DistrictCacheMapper(districtType));
            tempMultimap.putAll(districtType, maps);
        }
        this.districtMapCache = ImmutableMultimap.copyOf(tempMultimap);
        return true;
    }

    /**
     * Projects the result set into a DistrictInfo object.
     */
    private class DistrictInfoHandler implements ResultSetExtractor<DistrictInfo> {
        @Override
        public DistrictInfo extractData(ResultSet rs) throws SQLException {
            DistrictInfo districtInfo = new DistrictInfo();
            while (rs.next()) {
                DistrictType type = DistrictType.resolveType(rs.getString("type"));
                if (type != null) {
                    districtInfo.setDistName(type, rs.getString("name"));
                    districtInfo.setDistCode(type, getDistrictCode(rs, type));
                }
                else {
                    logger.error("Unsupported district type in results - {}", rs.getString("type"));
                }
            }
            return districtInfo;
        }
    }

    private class DistrictCacheMapper implements RowMapper<DistrictMap> {
        private final DistrictType type;

        private DistrictCacheMapper(DistrictType type) {
            this.type = type;
        }

        @Override
        public DistrictMap mapRow(@Nonnull ResultSet rs, int rowNum) throws SQLException {
            String code = getDistrictCode(rs, type);
            var metadata = new DistrictMetadata(type, rs.getString("name"), code);

            DistrictMap map = getDistrictMapFromJson(rs.getString("map"));
            map.setDistrictMetadata(metadata);
            return map;
        }
    }

    private class DistrictOverlapHandler implements ResultSetExtractor<DistrictOverlap> {
        private final DistrictOverlap districtOverlap = new DistrictOverlap();

        @Override
        public DistrictOverlap extractData(ResultSet rs) throws SQLException {
            while (rs.next()) {
                String code = getDistrictCode(rs, DistrictType.resolveType(rs.getString("type")));
                DistrictMap intersectMap = getDistrictMapFromJson(rs.getString("intersect_geo_json"));
                intersectMap.setArea(rs.getBigDecimal("area"));
                districtOverlap.addIntersectionMap(code, intersectMap);
            }
            return districtOverlap;
        }
    }

    /**
     * Retrieves the district code from the result set and performs any necessary corrections.
     * Requires that the result set contain 'type' and 'code' columns.
     */
    private String getDistrictCode(ResultSet rs, DistrictType type) throws SQLException {
        if (rs != null) {
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
        return null;
    }

    public DistrictMap getDistrictMap(DistrictType type, String district) {
        DistrictMap byName = districtMapCache.get(type).stream()
                .filter(dMap -> dMap.getDistrictName().equals(district))
                .findFirst().orElse(null);
        if (byName == null) {
            return districtMapCache.get(type).stream()
                    .filter(dMap -> dMap.getDistrictCode().equals(district))
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
    public static DistrictMap getDistrictMapFromJson(String jsonMap) {
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
}
