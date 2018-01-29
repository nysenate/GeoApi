package gov.nysenate.sage.dao.model;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.district.County;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple Dao to retrieve county information.
 */
@Repository
public class CountyDao extends BaseDao
{
    private Logger logger = LogManager.getLogger(CountyDao.class);
    private ResultSetHandler<County> handler = new BeanHandler<>(County.class);
    private ResultSetHandler<List<County>> listHandler = new BeanListHandler<>(County.class);
    private QueryRunner run = getQueryRunner();

    private static Map<Integer, County> fipsCountyMap;

    public List<County> getCounties()
    {
        String sql = "SELECT id, name, fips_code AS fipsCode FROM county";
        try {
            return run.query(sql, listHandler);
        }
        catch (SQLException ex){
            logger.error("Failed to get counties! " + ex.getMessage());
        }
        return null;
    }

    public Map<Integer, County> getFipsCountyMap()
    {
        if (fipsCountyMap == null) {
            List<County> counties = this.getCounties();
            if (counties != null) {
                fipsCountyMap = new HashMap<>();
                for (County c : counties){
                    fipsCountyMap.put(c.getFipsCode(), c);
                }
            }
        }
        return fipsCountyMap;
    }

    public County getCountyById(int id)
    {
        String sql = "SELECT id, name, fips_code AS fipsCode FROM county WHERE id = ?";
        try {
            return run.query(sql, handler, id);
        }
        catch (SQLException ex){
            logger.error("Failed to get county by id:" + id + "\n" + ex.getMessage());
        }
        return null;
    }

    public County getCountyByName(String name)
    {
        String sql = "SELECT id, name, fips_code AS fipsCode FROM county WHERE LOWER(name) = LOWER(?)";
        try {
            return run.query(sql, handler, name);
        }
        catch (SQLException ex){
            logger.error("Failed to get county by name:" + name + "\n" + ex.getMessage());
        }
        return null;
    }

    public County getCountyByFipsCode(int fipsCode)
    {
        String sql = "SELECT id, name, fips_code AS fipsCode FROM county WHERE fips_code = ?";
        try {
            return run.query(sql, handler, fipsCode);
        }
        catch (SQLException ex){
            logger.error("Failed to get county by fipsCode:" + fipsCode + "\n" + ex.getMessage());
        }
        return null;
    }
}
