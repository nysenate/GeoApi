package gov.nysenate.sage.dao.provider;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.address.DistrictedAddress;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.geo.Point;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.sql.Connection;
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

    protected static HashMap<DistrictType, String> distNameCol = new HashMap<>();
    protected static HashMap<DistrictType, String> distCodeCol = new HashMap<>();
    static {
        distNameCol.put(DistrictType.SENATE, null);
        distNameCol.put(DistrictType.CONGRESSIONAL, null);
        distNameCol.put(DistrictType.ASSEMBLY, null);
        distNameCol.put(DistrictType.COUNTY, "namelsad");
        distNameCol.put(DistrictType.SCHOOL, "eaname1");
        distNameCol.put(DistrictType.TOWN, "name");
        distNameCol.put(DistrictType.ELECTION, null);

        distCodeCol.put(DistrictType.SENATE, "district");
        distCodeCol.put(DistrictType.CONGRESSIONAL, "district");
        distCodeCol.put(DistrictType.ASSEMBLY, "district");
        distCodeCol.put(DistrictType.COUNTY, "countyfp");
        distCodeCol.put(DistrictType.SCHOOL, "tfcode");
        distCodeCol.put(DistrictType.TOWN, "abbrev");
        distCodeCol.put(DistrictType.ELECTION, null);
    }

    public DistrictShapefileDao()
    {

    }

    public DistrictedAddress getDistrictInfo(Point point, List<DistrictType> districtTypes, boolean getMaps)
    {
        /** Template SQL for looking up district given a point */
        String sqlTmpl =
                "SELECT '%s' AS type, %s AS name, %s as code " + ((getMaps) ? ", ST_AsGeoJson(geom) AS map " : "") +
                "FROM " + SCHEMA + ".%s " +
                "WHERE ST_CONTAINS(geom, ST_PointFromText('POINT(%f %f)' , " + SRID + "))";

        /** Iterate through all the requested types and format the template sql */
        ArrayList<String> queryList = new ArrayList<>();
        for (DistrictType dType : districtTypes){
            queryList.add(String.format(sqlTmpl, dType, distNameCol.get(dType), distCodeCol.get(dType), dType,
                                        point.getLon(), point.getLat())); // lat/lon is reversed here
        }

        /** Combine the queries using UNION ALL */
        String sqlQuery = StringUtils.join(queryList, " UNION ALL ");

        try {
            List<Map<String,Object>> result = run.query(sqlQuery, new MapListHandler());
        }
        catch (Exception ex){
            logger.error(ex);
        }
        return null;
    }
}
