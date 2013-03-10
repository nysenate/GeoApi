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
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ArrayHandler;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DistrictShapefileDao utilizes a PostGIS database loaded with Census shapefiles to
 * provide fast district resolution given a coordinate pair.
 */
public class DistrictShapefileDao extends BaseDao
{
    private static final String SCHEMA = "districts";   // Move to app.properties
    private static final String SRID = "4326";
    private final Logger logger = Logger.getLogger(DistrictShapefileDao.class);
    QueryRunner run = getQueryRunner();

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
                "SELECT '%s' AS type, %s AS name, %s as code " + ((getMaps) ? ", ST_AsGeoJson(geom) AS map " : ", null as map ") +
                "FROM " + SCHEMA + ".%s " +
                "WHERE ST_CONTAINS(geom, ST_PointFromText('POINT(%f %f)' , " + SRID + "))";

        /** Iterate through all the requested types and format the template sql */
        ArrayList<String> queryList = new ArrayList<>();
        for (DistrictType districtType : districtTypes){

            String nameColumn = DistrictShapeCode.getNameColumn(districtType);
            String codeColumn = resolveCodeColumn(districtType);

            queryList.add(String.format(sqlTmpl, districtType, nameColumn, codeColumn, districtType,
                                        point.getLon(), point.getLat())); // lat/lon is reversed here
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
     * This method is useful for constructing a static map cache for quick retrieval of a DistrictMap
     * given the DistrictType and code.
     *
     * NOTE: This method will succeed if the district types specified are state level districts. Lower
     * level districts do not have unique codes on a state wide basis and therefore will be inaccurate.
     *
     * @param districtTypes  Collection of district types to get maps for
     * @return               @see method signature
     */
    public Map<DistrictType, Map<String, DistrictMap>> getDistrictMaps(List<DistrictType> districtTypes)
    {
        String sql = "SELECT '%s' AS type, %s as code, ST_AsGeoJson(geom) AS map " +
                     "FROM " + SCHEMA + ".%s";

        /** Iterate through all the requested types and format the template sql */
        ArrayList<String> queryList = new ArrayList<>();
        for (DistrictType districtType : districtTypes){
            String codeColumn = resolveCodeColumn(districtType);
            queryList.add(String.format(sql, districtType, codeColumn, districtType));
        }

        /** Combine the queries using UNION ALL */
        String sqlQuery = StringUtils.join(queryList, " UNION ALL ");

        try {
            return run.query(sqlQuery, new DistrictMapsHandler());
        }
        catch (SQLException ex) {
            logger.error(ex);
        }
        return null;
    }

    /** Some code columns have special cases */
    private String resolveCodeColumn(DistrictType districtType)
    {
        String codeColumn = DistrictShapeCode.getCodeColumn(districtType);

        /** Election shapefile has an integer code so we should convert to string */
        if (districtType == DistrictType.ELECTION){
            codeColumn = "to_char("+ codeColumn + ", '999')";
        }

        return codeColumn;
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

                    /** County codes need to be mapped from FIPS code */
                    if (type == DistrictType.COUNTY){
                        String code = Integer.toString(new CountyDao().getFipsCountyMap().get(rs.getInt("code")).getId());
                        districtInfo.setDistCode(type, code);
                    }
                    /** Normal district code */
                    else {
                        String code = rs.getString("code");
                        if (code != null) {
                            districtInfo.setDistCode(type, code.trim());
                        }
                    }
                    /** District Map */
                    if (rs.getString("map") != null && !rs.getString("map").isEmpty()){
                        districtInfo.setDistMap(type, getDistrictMapFromJson(rs.getString("map")));
                    }
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

                    String code = null;
                    DistrictMap map = null;

                    /** County codes need to be mapped from FIPS code */
                    if (type == DistrictType.COUNTY){
                        code = Integer.toString(new CountyDao().getFipsCountyMap().get(rs.getInt("code")).getId());
                    }
                    /** Normal district code */
                    else {
                        code = rs.getString("code");
                    }
                    /** District Map */
                    if (rs.getString("map") != null && !rs.getString("map").isEmpty()){
                        map = getDistrictMapFromJson(rs.getString("map"));
                    }
                    /** Set values in the lookup HashMap */
                    if (code != null && map != null) {
                        districtMaps.get(type).put(code, map);
                    }
                }
            }
            return districtMaps;
        }
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
        List<Point> points = new ArrayList<>();

        /** The geometry response comes in as a quadruply nested array */
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode mapNode = objectMapper.readTree(jsonMap);
            JsonNode coordinates = mapNode.get("coordinates").get(0).get(0);
            for (int i = 0; i < coordinates.size(); i++){
                points.add(new Point(coordinates.get(i).get(1).asDouble(), coordinates.get(i).get(0).asDouble()));
            }
            districtMap.setPolygon(new Polygon(points));
            return districtMap;
        }
        catch (IOException ex) {
            logger.error(ex);
        }

        return null;
    }
}
