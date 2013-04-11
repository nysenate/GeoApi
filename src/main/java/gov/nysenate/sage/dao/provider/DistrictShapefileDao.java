package gov.nysenate.sage.dao.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.model.CountyDao;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictShapeCode;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.geo.Polygon;
import gov.nysenate.sage.util.FormatUtil;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * DistrictShapefileDao utilizes a PostGIS database loaded with Census shapefiles to
 * provide fast district resolution given a coordinate pair.
 */
public class DistrictShapefileDao extends BaseDao
{
    private static final String SCHEMA = "districts";   // Move to app.properties
    private static final String SRID = "4326";
    private final Logger logger = Logger.getLogger(DistrictShapefileDao.class);
    private QueryRunner run = getQueryRunner();

    /** Cached State District Maps */
    private static Map<DistrictType, Map<String, DistrictMap>> stateDistrictMaps;

    public DistrictShapefileDao() {}

    /**
     * Retrieves a DistrictInfo object based on the districts that intersect the given point.
     * @param point          Point of interest
     * @param districtTypes  Collection of district types to resolve
     * @param getMaps        If true then query will return DistrictMap values as well
     * @return  DistrictInfo if query was successful, null otherwise
     */
    public DistrictInfo getDistrictInfo(Point point, List<DistrictType> districtTypes, boolean getMaps)
    {
        /** Template SQL for looking up district given a point */
        String sqlTmpl =
                "SELECT '%s' AS type, %s AS name, %s as code " + ((getMaps) ? ", ST_AsGeoJson(geom) AS map, " : ", null as map, ") +
                "       ST_Distance(ST_Boundary(geom), ST_PointFromText('POINT(%f %f)' , " + SRID + ")) As proximity " +
                "FROM " + SCHEMA + ".%s " +
                "WHERE ST_CONTAINS(geom, ST_PointFromText('POINT(%f %f)' , " + SRID + "))";

        /** Iterate through all the requested types and format the template sql */
        ArrayList<String> queryList = new ArrayList<>();
        for (DistrictType districtType : districtTypes){
            if (DistrictShapeCode.contains(districtType)) {
                String nameColumn = DistrictShapeCode.getNameColumn(districtType);
                String codeColumn = resolveCodeColumn(districtType);

                queryList.add(String.format(sqlTmpl, districtType, nameColumn, codeColumn, point.getLon(), point.getLat(),
                        districtType, point.getLon(), point.getLat())); // lon,lat is correct order
            }
        }

        /** Combine the queries using UNION ALL */
        String sqlQuery = StringUtils.join(queryList, " UNION ALL ");

        try {
            return run.query(sqlQuery, new DistrictInfoHandler());
        }
        catch (Exception ex){
            logger.error(ex);
        }
        return null;
    }

    /**
     * Retrieves a mapped collection of district code to DistrictMap that's grouped by DistrictType.
     *
     * @return Map<DistrictType, Map<String, DistrictMap>>
     */
    public Map<DistrictType, Map<String, DistrictMap>> getStateDistrictMaps()
    {
        if (stateDistrictMaps == null) {
            String sql = "SELECT '%s' AS type, %s as code, ST_AsGeoJson(geom) AS map " +
                    "FROM " + SCHEMA + ".%s";

            /** Iterate through all the requested types and format the template sql */
            ArrayList<String> queryList = new ArrayList<>();
            for (DistrictType districtType : DistrictType.getStateBasedTypes()) {
                if (DistrictShapeCode.contains(districtType)) {
                    String codeColumn = resolveCodeColumn(districtType);
                    queryList.add(String.format(sql, districtType, codeColumn, districtType));
                }
            }

            /** Combine the queries using UNION ALL */
            String sqlQuery = StringUtils.join(queryList, " UNION ALL ");

            try {
                stateDistrictMaps = run.query(sqlQuery, new DistrictMapsHandler());
                logger.info("Cached state based maps");
            }
            catch (SQLException ex) {
                logger.error(ex);
            }
        }
        return stateDistrictMaps;
    }

    /**
     * Obtain a list of districts that are closest to the given point. This list does not include the
     * district that the point actually resides within.
     * @param districtType
     * @param point
     * @return
     */
    public LinkedHashMap<String, DistrictMap> getNearbyDistricts(DistrictType districtType, Point point, int count)
    {
        String tmpl =
            "SELECT '%s' AS type, %s AS code, ST_AsGeoJson(geom) AS map \n" +
            "FROM " + SCHEMA +".%s \n" +
            "WHERE ST_Contains(geom, ST_PointFromText('POINT(%f %f)'," + SRID +")) = false \n" +
            "ORDER BY geom <-> ST_PointFromText('POINT(%f %f)'," + SRID + ") \n" +
            "LIMIT %d;";

        if (DistrictShapeCode.contains(districtType)) {
            String sqlQuery = String.format(tmpl, districtType.name(), resolveCodeColumn(districtType), districtType.name(),
                    point.getLon(), point.getLat(), point.getLon(), point.getLat(), count);
            try {
                return run.query(sqlQuery, new NearbyDistrictMapsHandler());
            }
            catch (SQLException ex) {
                logger.error(ex);
            }
        }
        return null;
    }

    /** Convenience method to access DistrictShapeCode */
    private String resolveCodeColumn(DistrictType districtType)
    {
        return DistrictShapeCode.getCodeColumn(districtType);
    }

    /**
     * Projects the result set into a DistrictInfo object.
     */
    private class DistrictInfoHandler implements ResultSetHandler<DistrictInfo>
    {
        @Override
        public DistrictInfo handle(ResultSet rs) throws SQLException {
            DistrictInfo districtInfo = new DistrictInfo();
            while (rs.next()) {
                DistrictType type = DistrictType.resolveType(rs.getString("type"));
                if (type != null) {
                    /** District name */
                    districtInfo.setDistName(type, rs.getString("name"));

                    /** District code */
                    districtInfo.setDistCode(type, getDistrictCode(rs));

                    /** District map */
                    districtInfo.setDistMap(type, getDistrictMapFromJson(rs.getString("map")));

                    /** District proximity */
                    districtInfo.setDistProximity(type, rs.getDouble("proximity"));
                }
                else {
                    logger.error("Unsupported district type in results - " + rs.getString("type"));
                }
            }
            return districtInfo;
        }
    }

    /**
     * Projects the result set into the following map structure for purposes of caching and retrieving map
     * data based on district type and code:
     * { DistrictType:type -> { String:code -> DistrictMap:map} }
     */
    private class DistrictMapsHandler implements ResultSetHandler<Map<DistrictType, Map<String, DistrictMap>>>
    {
        @Override
        public Map<DistrictType, Map<String, DistrictMap>> handle(ResultSet rs) throws SQLException
        {
            Map<DistrictType, Map<String, DistrictMap>> districtMaps = new HashMap<>();

            while (rs.next()) {
                DistrictType type = DistrictType.resolveType(rs.getString("type"));
                if (type != null) {
                    if (!districtMaps.containsKey(type)) {
                        districtMaps.put(type, new HashMap<String, DistrictMap>());
                    }

                    String code = getDistrictCode(rs);
                    DistrictMap map = getDistrictMapFromJson(rs.getString("map"));
                    map.setDistrictType(type);
                    map.setDistrictCode(code);

                    /** Set values in the lookup HashMap */
                    if (code != null && map != null) {
                        districtMaps.get(type).put(code, map);
                    }
                }
            }
            return districtMaps;
        }
    }

    private class NearbyDistrictMapsHandler implements ResultSetHandler<LinkedHashMap<String, DistrictMap>>
    {
        @Override
        public LinkedHashMap<String, DistrictMap> handle(ResultSet rs) throws SQLException
        {
            LinkedHashMap<String, DistrictMap> nearbyDistrictMaps = new LinkedHashMap<>();
            while (rs.next()) {
                String code = getDistrictCode(rs);
                DistrictMap map = getDistrictMapFromJson(rs.getString("map"));
                nearbyDistrictMaps.put(code, map);
            }
            return nearbyDistrictMaps;
        }
    }

    /**
     * Retrieves the district code from the result set and performs any necessary corrections.
     * @param rs
     * @return
     * @throws SQLException
     */
    private String getDistrictCode(ResultSet rs) throws SQLException
    {
        if (rs != null) {
            DistrictType type = DistrictType.resolveType(rs.getString("type"));
            String code;

            /** County codes need to be mapped from FIPS code */
            if (type == DistrictType.COUNTY){
                code = Integer.toString(new CountyDao().getFipsCountyMap().get(rs.getInt("code")).getId());
            }
            /** Normal district code */
            else {
                code = rs.getString("code");
                if (code != null) { code = code.trim(); }
            }
            return FormatUtil.trimLeadingZeroes(code);
        }
        return null;
    }

    /**
     * Parses JSON map response and creates a DistrictMap object containing the district geometry.
     * @param jsonMap   GeoJson string containing the district geometry
     * @return          DistrictMap containing the geometry.
     *                  null if map string not present or error
     */
    private DistrictMap getDistrictMapFromJson(String jsonMap)
    {
        DistrictMap districtMap = new DistrictMap();
        if (jsonMap != null && !jsonMap.isEmpty()) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                JsonNode mapNode = objectMapper.readTree(jsonMap);
                JsonNode coordinates = mapNode.get("coordinates");
                for (int i = 0; i < coordinates.size(); i++) {
                    List<Point> points = new ArrayList<>();
                    JsonNode polygon = coordinates.get(i).get(0);
                    for (int j = 0; j < polygon.size(); j++){
                        points.add(new Point(polygon.get(j).get(1).asDouble(), polygon.get(j).get(0).asDouble()));
                    }
                    districtMap.addPolygon(new Polygon(points));
                }
                return districtMap;
            }
            catch (IOException ex) {
                logger.error(ex);
            }
        }
        return null;
    }
}
