package gov.nysenate.sage.dao.model.county;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.district.County;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

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
    private static final Logger logger = LoggerFactory.getLogger(SqlCountyDao.class);
    private final BaseDao baseDao;

    @Autowired
    public SqlCountyDao(BaseDao baseDao) {
        this.baseDao = baseDao;
    }

    private static Map<Integer, County> fipsCountyMap;

    /** {@inheritDoc} */
    public List<County> getCounties()
    {
        try {
            return baseDao.geoApiNamedJbdcTemplate.query(
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
                    fipsCountyMap.put(c.fipsCode(), c);
                }
            }
        }
        return fipsCountyMap;
    }

    /** {@inheritDoc} */
    public County getCountyById(int id)
    {
        return getCounty("senateCode", id, CountyQuery.GET_COUNTY_BY_ID);
    }

    private County getCounty(String paramName, Object param, CountyQuery query) {
        try {
            var params = new MapSqlParameterSource(paramName, param);
            List<County> countyList = baseDao.geoApiNamedJbdcTemplate
                    .query(query.getSql(baseDao.getPublicSchema()), params, new CountyHandler());
            if (countyList.get(0) != null) {
                return countyList.get(0);
            }
        }
        catch (Exception ex) {
            logger.error("Failed to get county by %s: %s%n%s".formatted(paramName, param, ex.getMessage()));
        }
        return null;
    }

    private static class CountyHandler implements RowMapper<County> {
        @Override
        public County mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new County(rs.getInt("senate_code"), rs.getInt("fips_code"), rs.getInt("voterfile_code"),
                    rs.getString("name"), rs.getString("link"), rs.getString("streetfile_name"));
        }
    }
}
