package gov.nysenate.sage.dao.model.county;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.district.County;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple Dao to retrieve county information.
 */
@Repository
public class SqlCountyDao implements CountyDao
{
    private Logger logger = LoggerFactory.getLogger(SqlCountyDao.class);
    private BaseDao baseDao;

    @Autowired
    public SqlCountyDao(BaseDao baseDao) {
        this.baseDao = baseDao;
    }

    private static Map<Integer, County> fipsCountyMap;

    /** {@inheritDoc} */
    public List<County> getCounties()
    {
        try {
            return baseDao.geoApiNamedJbdcTemaplate.query(
                    CountyQuery.GET_ALL_COUNTIES.getSql(baseDao.getPublicSchema()), new CountyHandler());
        }
        catch (Exception ex){
            logger.error("Failed to get counties! " + ex.getMessage());
        }
        return null;
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    public County getCountyById(int id)
    {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("id", id);

            List<County> countyList= baseDao.geoApiNamedJbdcTemaplate.query(
                    CountyQuery.GET_COUNTY_BY_ID.getSql(baseDao.getPublicSchema()), params, new CountyHandler());

            if (countyList != null && countyList.get(0) != null) {
                return countyList.get(0);
            }
        }
        catch (Exception ex){
            logger.error("Failed to get county by id:" + id + "\n" + ex.getMessage());
        }
        return null;
    }

    /** {@inheritDoc} */
    public County getCountyByName(String name)
    {
        try {

            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("name", name);

            List<County> countyList= baseDao.geoApiNamedJbdcTemaplate.query(
                    CountyQuery.GET_COUNTY_BY_NAME.getSql(baseDao.getPublicSchema()), params, new CountyHandler());

            if (countyList != null && countyList.get(0) != null) {
                return countyList.get(0);
            }
        }
        catch (Exception ex){
            logger.error("Failed to get county by name:" + name + "\n" + ex.getMessage());
        }
        return null;
    }

    /** {@inheritDoc} */
    public County getCountyByFipsCode(int fipsCode)
    {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("fipsCode", fipsCode);

            List<County> countyList= baseDao.geoApiNamedJbdcTemaplate.query(
                    CountyQuery.GET_COUNTY_BY_FIPS_CODE.getSql(baseDao.getPublicSchema()), params, new CountyHandler());

            if (countyList != null && countyList.get(0) != null) {
                return countyList.get(0);
            }
        }
        catch (Exception ex){
            logger.error("Failed to get county by fipsCode:" + fipsCode + "\n" + ex.getMessage());
        }
        return null;
    }

    private static class CountyHandler implements RowMapper<County> {
        @Override
        public County mapRow(ResultSet rs, int rowNum) throws SQLException {
            County county = new County();
            county.setId(rs.getInt("id"));
            county.setName(rs.getString("name"));
            county.setFipsCode(rs.getInt("fipsCode"));
            return county;
        }
    }
}
