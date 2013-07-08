package gov.nysenate.sage.dao.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.model.CountyDao;
import gov.nysenate.sage.model.district.*;
import gov.nysenate.sage.model.geo.GeometryTypes;
import gov.nysenate.sage.model.geo.Line;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.geo.Polygon;
import gov.nysenate.sage.util.FormatUtil;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ArrayListHandler;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.math.BigDecimal;
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

    private static CountyDao countyDao = new CountyDao();

    /** Memory Cached District Maps */
    private static Map<DistrictType, List<DistrictMap>> districtMapCache;
    private static Map<DistrictType, Map<String, DistrictMap>> districtMapLookup;

    private static Set<DistrictType> retrieveMapSet = new HashSet<>();
    static {
        retrieveMapSet.add(DistrictType.SCHOOL);
    }

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
                "SELECT '%s' AS type, %s AS name, %s as code " +
                        "%s" + // <- mapQuery
                "       ST_Distance(ST_Boundary(geom), ST_PointFromText('POINT(%f %f)' , " + "%s" + ")) As proximity " +
                "FROM " + SCHEMA + ".%s " +
                "WHERE ST_CONTAINS(geom, ST_PointFromText('POINT(%f %f)' , " + "%s" + "))";

        /** Iterate through all the requested types and format the template sql */
        ArrayList<String> queryList = new ArrayList<>();
        for (DistrictType districtType : districtTypes){
            if (DistrictShapeCode.contains(districtType)) {
                String nameColumn = DistrictShapeCode.getNameColumn(districtType);
                String codeColumn = resolveCodeColumn(districtType);
                String srid = resolveSRID(districtType);
                String mapQuery = ((getMaps && retrieveMapSet.contains(districtType)) ? ", ST_AsGeoJson(geom) AS map, "
                                                                                             : ", null as map, ");
                queryList.add(String.format(sqlTmpl, districtType, nameColumn, codeColumn, mapQuery, point.getLon(), point.getLat(),
                        srid, districtType, point.getLon(), point.getLat(), srid)); // lon,lat is correct order
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
     *
     * @param targetDistrictType
     * @param targetCodes
     * @param refDistrictType
     * @param refCodes
     */
    public DistrictOverlap getDistrictOverlap(DistrictType targetDistrictType, Set<String> targetCodes,
                      DistrictType refDistrictType, Set<String> refCodes)
    {
        if (targetCodes == null) targetCodes = new HashSet<>();

        String intersectSql = "ST_Intersection(ST_Transform(target.geom, %s), ST_Transform(source.geom, %s))";
        intersectSql = String.format(intersectSql, resolveSRID(refDistrictType), resolveSRID(refDistrictType));
        String intersectAreaSql = String.format(
                "ST_Area(" +
                    "ST_Transform(%s , utmzone(ST_Centroid(target.geom)))" +
                ")", intersectSql);
        String sqlTmpl =
            "SELECT target.%s AS code, '%s' as type, \n" +
             intersectAreaSql + " AS intersected_area, \n" +
            "    ST_Area( \n" +
            "        ST_Transform(source.geom, utmzone(ST_Centroid(source.geom)))\n" +
            "    ) AS source_area \n" +
            /** Get intersection polygon for senate districts */
            (targetDistrictType.equals(DistrictType.SENATE) ? "" +
                    String.format(", ST_AsGeoJson(ST_CollectionExtract(%s, 3)) AS intersect_geom \n", intersectSql) : "") +
            "FROM " + SCHEMA + ".%s target, " +
            "     (SELECT ST_Union(geom) AS geom FROM " + SCHEMA + ".%s WHERE %s) AS source\n" +
            "WHERE %s \n" +
            "ORDER BY intersected_area DESC";
        List<String> refWhereList = new ArrayList<>();
        for (String refCode : refCodes) {
            refWhereList.add(String.format("trim(leading '0' from %s) = trim(leading '0' from '%s')", resolveCodeColumn(refDistrictType), StringEscapeUtils.escapeSql(refCode)));
        }
        List<String> targetWhereList = new ArrayList<>();
        for (String targetCode : targetCodes) {
            targetWhereList.add(String.format("trim(leading '0' from %s) = trim(leading '0' from '%s')", resolveCodeColumn(targetDistrictType), StringEscapeUtils.escapeSql(targetCode)));
        }
        String refWhereSql = StringUtils.join(refWhereList, " OR ");
        String targetWhereSql = (targetWhereList.size() > 0) ? StringUtils.join(targetWhereList, " OR ") : intersectSql + " > 0";
        String sqlQuery = String.format(sqlTmpl, resolveCodeColumn(targetDistrictType), targetDistrictType.name(), targetDistrictType.name(), refDistrictType.name(),
                                                 refWhereSql, targetWhereSql);
        try {
            DistrictOverlap overlap = new DistrictOverlap(refDistrictType, targetDistrictType, refCodes, DistrictOverlap.AreaUnit.SQ_METERS);
            return run.query(sqlQuery, new DistrictOverlapHandler(overlap));
        }
        catch (SQLException ex) {
            logger.error("Failed to determine district overlap!", ex);
        }
        return null;
    }

    public Map<String, List<Line>> getIntersectingStreetLine(DistrictType districtType, Set<String> codes, String jsonGeom)
    {
        String sqlTmpl =
            "SELECT %s AS code, '%s' AS type, ST_AsGeoJson(" +
                "ST_CollectionExtract(" +
                    "ST_Intersection(geom, ST_SetSRID(ST_GeomFromGeoJson(?), %s))" +
                ", 2)" +
            ") AS street_intersect \n" +
            "FROM " + SCHEMA + "." + districtType.name() + "\n" +
            "WHERE %s";
        List<String> whereCodeList = new ArrayList<>();
        for (String code : codes) {
            whereCodeList.add(String.format("trim(leading '0' from %s) = trim(leading '0' from '%s')",
                    resolveCodeColumn(districtType), code));
        }
        String whereSql = StringUtils.join(whereCodeList, " OR ");
        String sql = String.format(sqlTmpl, resolveCodeColumn(districtType), districtType.name(), resolveSRID(districtType), whereSql);
        try {
            return run.query(sql, new StreetLineIntersectHandler(), jsonGeom);
        }
        catch (SQLException ex) {
            logger.error("Failed to retrieve intersecting district street lines!", ex);
        }
        return null;
    }

    /**
     *
     * @param refDistrictType
     * @param refCodes
     * @return
     */
    public DistrictMap getOverlapReferenceBoundary(DistrictType refDistrictType, Set<String> refCodes)
    {
        String sql = "SELECT ST_AsGeoJson(ST_CollectionExtract(source.geom, 3)) AS source_map \n" +
                     "FROM \n" +
                     "(SELECT ST_Union(geom) AS geom FROM " + SCHEMA + ".%s WHERE (%s)) AS source";
        List<String> refWhereList = new ArrayList<>();
        for (String refCode : refCodes) {
            refWhereList.add(String.format("trim(leading '0' from %s) = trim(leading '0' from '%s')", resolveCodeColumn(refDistrictType), StringEscapeUtils.escapeSql(refCode)));
        }
        String refWhereSql = StringUtils.join(refWhereList, " OR ");
        String sqlQuery = String.format(sql, refDistrictType.name(), refWhereSql);

        try {
            return run.query(sqlQuery, new ResultSetHandler<DistrictMap>(){
                @Override
                public DistrictMap handle(ResultSet rs) throws SQLException {
                    if (rs.next()) {
                        return getDistrictMapFromJson(rs.getString("source_map"));
                    }
                    return null;
                }
            });
        }
        catch (SQLException ex) {
            logger.debug("Failed to get overlap reference boundary!", ex);
        }
        return null;
    }

    /**
     * Retrieves a mapped collection of district code to DistrictMap that's grouped by DistrictType.
     * @return Map<DistrictType, Map<String, DistrictMap>>
     */
    public Map<DistrictType, Map<String, DistrictMap>> getDistrictMapLookup()
    {
        if (districtMapLookup == null) {
            cacheDistrictMaps();
        }
        return districtMapLookup;
    }

    /**
     * Retrieves a mapped collection of DistrictMaps.
     * @return Map<DistrictType, List<DistrictMap>>
     */
    public Map<DistrictType, List<DistrictMap>> getDistrictMaps()
    {
        if (districtMapCache == null) {
            cacheDistrictMaps();
        }
        return districtMapCache;
    }

    /**
     * Fetches all the district maps from the database and stores them in a collection as well as
     * a lookup cache for fast retrieval.
     */
    public boolean cacheDistrictMaps()
    {
        String sql = "SELECT '%s' AS type, %s as name, %s as code, ST_AsGeoJson(geom) AS map " +
                     "FROM " + SCHEMA + ".%s";

        /** Iterate through all the requested types and format the template sql */
        ArrayList<String> queryList = new ArrayList<>();
        for (DistrictType districtType : DistrictType.getStandardTypes()) {
            if (DistrictShapeCode.contains(districtType)) {
                String nameColumn = resolveNameColumn(districtType);
                String codeColumn = resolveCodeColumn(districtType);
                queryList.add(String.format(sql, districtType, nameColumn, codeColumn, districtType));
            }
        }

        /** Combine the queries using UNION ALL and set the sort condition */
        String sqlQuery = StringUtils.join(queryList, " UNION ALL ") + " ORDER BY type, code";

        try {
            run.query(sqlQuery, new DistrictMapsCacheHandler());
            logger.info("Cached standard district maps");
            return true;
        }
        catch (SQLException ex) {
            logger.error(ex);
            return false;
        }
    }

    /**
     * Obtain a list of districts that are closest to the given point. This list does not include the
     * district that the point actually resides within.
     * @param districtType
     * @param point
     * @return
     */
    public LinkedHashMap<String, DistrictMap> getNearbyDistricts(DistrictType districtType, Point point, boolean getMaps, int count)
    {
        if (DistrictShapeCode.contains(districtType)) {
            String srid = resolveSRID(districtType);
            String tmpl =
                "SELECT '%s' AS type, %s as name, %s AS code, " + ((getMaps) ? "ST_AsGeoJson(geom) AS map " : "null as map \n") +
                "FROM " + SCHEMA +".%s \n" +
                "WHERE ST_Contains(geom, %s) = false \n" +
                "ORDER BY ST_ClosestPoint(geom, %s) <-> %s \n" +
                "LIMIT %d;";

           String pointText = String.format("ST_PointFromText('POINT(%s %s)', %s)", point.getLon(), point.getLat(), srid);

            String sqlQuery = String.format(tmpl, districtType.name(), resolveNameColumn(districtType), resolveCodeColumn(districtType),
                    districtType.name(), pointText, pointText, pointText, count);
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

    /** Convenience method to access DistrictShapeCode */
    private String resolveNameColumn(DistrictType districtType)
    {
        return DistrictShapeCode.getNameColumn(districtType);
    }

    /** Convenience method to access DistrictShapeCode */
    private String resolveSRID(DistrictType districtType)
    {
        return DistrictShapeCode.getSridColumn(districtType);
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
    private class DistrictMapsCacheHandler implements ResultSetHandler<Map<DistrictType, Map<String, DistrictMap>>>
    {
        @Override
        public Map<DistrictType, Map<String, DistrictMap>> handle(ResultSet rs) throws SQLException
        {
            /** Initialize the cache maps */
            districtMapCache = new HashMap<>();
            districtMapLookup = new HashMap<>();

            while (rs.next()) {
                DistrictType type = DistrictType.resolveType(rs.getString("type"));
                if (type != null) {
                    if (!districtMapCache.containsKey(type)) {
                        districtMapCache.put(type, new ArrayList<DistrictMap>());
                        districtMapLookup.put(type, new HashMap<String, DistrictMap>());
                    }

                    String code = getDistrictCode(rs);
                    DistrictMetadata metadata = new DistrictMetadata(type, rs.getString("name"), code);

                    DistrictMap map = getDistrictMapFromJson(rs.getString("map"));
                    map.setDistrictMetadata(metadata);

                    /** Set values in the lookup HashMap */
                    if (code != null && map != null) {
                        districtMapCache.get(type).add(map);
                        districtMapLookup.get(type).put(code, map);
                    }
                }
            }
            logger.debug("Cache size: " + districtMapCache.get(DistrictType.SCHOOL).size());
            int lookupSize = 0;
            lookupSize = districtMapLookup.get(DistrictType.SCHOOL).size();
            logger.debug("Lookup cache size: " + lookupSize);
            return districtMapLookup;
        }
    }

    private class NearbyDistrictMapsHandler implements ResultSetHandler<LinkedHashMap<String, DistrictMap>>
    {
        @Override
        public LinkedHashMap<String, DistrictMap> handle(ResultSet rs) throws SQLException
        {
            LinkedHashMap<String, DistrictMap> nearbyDistrictMaps = new LinkedHashMap<>();
            while (rs.next()) {
                DistrictType type = DistrictType.resolveType(rs.getString("type"));
                String code = getDistrictCode(rs);
                DistrictMap map = getDistrictMapFromJson(rs.getString("map"));
                if (map == null) {
                    map = new DistrictMap();
                }
                map.setDistrictName(rs.getString("name"));
                map.setDistrictType(type);
                map.setDistrictCode(code);
                nearbyDistrictMaps.put(code, map);
            }
            return nearbyDistrictMaps;
        }
    }

    private class DistrictOverlapHandler implements ResultSetHandler<DistrictOverlap>
    {
        private DistrictOverlap districtOverlap;

        public DistrictOverlapHandler(DistrictOverlap districtOverlap) {
            this.districtOverlap = districtOverlap;
        }

        @Override
        public DistrictOverlap handle(ResultSet rs) throws SQLException {
            while (rs.next()) {
                String code = getDistrictCode(rs);
                BigDecimal area = rs.getBigDecimal("intersected_area");
                if (rs.getString("type") != null && rs.getString("type").equalsIgnoreCase("SENATE")) {
                    DistrictMap intersectMap = getDistrictMapFromJson(rs.getString("intersect_geom"));
                    this.districtOverlap.addIntersectionMap(code, intersectMap);
                }
                /** Only add districts that actually intersect */
                if (area != null && area.compareTo(BigDecimal.ZERO) == 1) {
                    this.districtOverlap.getTargetOverlap().put(code, area);
                }
                if (this.districtOverlap.getTotalArea() == null) {
                    this.districtOverlap.setTotalArea(rs.getBigDecimal("source_area"));
                }
            }
            return districtOverlap;
        }
    }

    private class StreetLineIntersectHandler implements ResultSetHandler<Map<String, List<Line>>>
    {
        @Override
        public Map<String, List<Line>> handle(ResultSet rs) throws SQLException
        {
            Map<String, List<Line>> intersectMap = new HashMap<>();
            while (rs.next()) {
                String code = getDistrictCode(rs);
                List<Line> lines = getLinesFromJson(rs.getString("street_intersect"));
                intersectMap.put(code, lines);
            }
            return intersectMap;
        }
    }

    /**
     * Retrieves the district code from the result set and performs any necessary corrections.
     * Requires that the result set contain 'type' and 'code' columns.
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
                code = Integer.toString(countyDao.getFipsCountyMap().get(rs.getInt("code")).getId());
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
     * This method does not set any other fields on the DistrictMap object.
     * @param jsonMap   GeoJson string containing the district geometry
     * @return          DistrictMap containing the geometry.
     *                  null if map string not present or error
     */
    private DistrictMap getDistrictMapFromJson(String jsonMap)
    {
        if (jsonMap != null && !jsonMap.isEmpty() && jsonMap != "null") {
            DistrictMap districtMap = new DistrictMap();
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                JsonNode mapNode = objectMapper.readTree(jsonMap);
                String type = mapNode.get("type").asText();
                GeometryTypes geoType;
                try {
                    geoType = GeometryTypes.valueOf(type.toUpperCase());
                    districtMap.setGeometryType(geoType.type);
                }
                catch (Exception ex) {
                    logger.debug("Geometry type " + type + " is not supported by this method!");
                    return null;
                }
                JsonNode coordinates = mapNode.get("coordinates");
                for (int i = 0; i < coordinates.size(); i++) {
                    List<Point> points = new ArrayList<>();
                    JsonNode polygon = (geoType.equals(GeometryTypes.MULTIPOLYGON)) ? coordinates.get(i).get(0) : coordinates.get(i);
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
