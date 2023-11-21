package gov.nysenate.sage.dao.provider.cityzip;

import gov.nysenate.sage.dao.base.BaseDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
public class SqlCityZipDBDao implements CityZipDBDao
{
    private Logger logger = LoggerFactory.getLogger(SqlCityZipDBDao.class);
    private BaseDao baseDao;

    private static String SCHEMA = "public";
    private static String TABLE = "cityzip";

    /** List of cities that should accept any location type */
    private static Set<String> cityExceptions = new HashSet<>(
            Arrays.asList("New York", "Manhattan", "Queens", "Brooklyn", "Bronx", "Staten Island"));

    @Autowired
    public SqlCityZipDBDao(BaseDao baseDao) {
        this.baseDao = baseDao;
    }

    /** {@inheritDoc} */
    public List<String> getZipsByCity(String city)
    {
        if (city == null || city.isEmpty()) return null; // Short circuit
        String sql = "SELECT DISTINCT zip5 \n" +
                     "FROM " + SCHEMA + "." + TABLE + "\n" +
                     "WHERE city = upper(trim(:city)) AND (type = 'STANDARD' OR type = 'PO BOX') \n" +
                     (!cityExceptions.contains(city) ? " AND (locationType = 'PRIMARY' OR locationType = 'ACCEPTABLE')"
                                                     : "");
        try {
            MapSqlParameterSource params =  new MapSqlParameterSource();
            params.addValue("city", city);

            return baseDao.geoApiNamedJbdcTemplate.query(sql, params, new zip5Handler());
        }
        catch (Exception ex) {
            logger.error("Failed to get zip5 list by city!", ex);
        }
        return new ArrayList<>();
    }

    private static class zip5Handler implements RowMapper<String> {
        @Override
        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getString("zip5");
        }
    }
}
