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
import java.util.List;
import java.util.Set;

@Repository
public class SqlCityZipDBDao {
    private static final Logger logger = LoggerFactory.getLogger(SqlCityZipDBDao.class);
    private static final String SCHEMA = "public";
    private static final String TABLE = "cityzip";
    /** List of cities that should accept any location type */
    private static final Set<String> cityExceptions = Set.of("New York", "Manhattan", "Queens", "Brooklyn", "Bronx", "Staten Island");

    private final BaseDao baseDao;

    @Autowired
    public SqlCityZipDBDao(BaseDao baseDao) {
        this.baseDao = baseDao;
    }

    /**
     * Returns a list of zip codes given a city name.
     * @return List of matching zip5 strings.
     */
    public List<Integer> getZipsByCity(String city) {
        if (city == null || city.isEmpty()) {
            return null;
        }
        String sql = "SELECT DISTINCT zip5 \n" +
                     "FROM " + SCHEMA + "." + TABLE + "\n" +
                     "WHERE city = upper(trim(:city)) AND (type = 'STANDARD' OR type = 'PO BOX') \n" +
                     (!cityExceptions.contains(city) ? " AND (locationType = 'PRIMARY' OR locationType = 'ACCEPTABLE')"
                                                     : "");
        try {
            var params =  new MapSqlParameterSource("city", city);
            return baseDao.geoApiNamedJbdcTemplate.query(sql, params, new zip5Handler());
        }
        catch (Exception ex) {
            logger.error("Failed to get zip5 list by city!", ex);
        }
        return List.of();
    }

    private static class zip5Handler implements RowMapper<Integer> {
        @Override
        public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getInt("zip5");
        }
    }
}
