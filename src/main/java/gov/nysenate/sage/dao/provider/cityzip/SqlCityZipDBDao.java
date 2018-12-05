package gov.nysenate.sage.dao.provider.cityzip;

import gov.nysenate.sage.dao.base.BaseDao;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
public class SqlCityZipDBDao
{
    private Logger logger = LoggerFactory.getLogger(SqlCityZipDBDao.class);
    private QueryRunner run;
    private BaseDao baseDao;

    private static String SCHEMA = "public";
    private static String TABLE = "cityzip";

    /** List of cities that should accept any location type */
    private static Set<String> cityExceptions = new HashSet<>(
            Arrays.asList("New York", "Manhattan", "Queens", "Brooklyn", "Bronx", "Staten Island"));

    @Autowired
    public SqlCityZipDBDao(BaseDao baseDao) {
        this.baseDao = baseDao;
        run = this.baseDao.getQueryRunner();
    }

    /**
     * Returns a list of zip codes given a city name.
     * @param city
     * @return List of matching zip5 strings.
     */
    public List<String> getZipsByCity(String city)
    {
        if (city == null || city.isEmpty()) return null; // Short circuit
        String sql = "SELECT DISTINCT zip5 \n" +
                     "FROM " + SCHEMA + "." + TABLE + "\n" +
                     "WHERE city = upper(trim(?)) AND (type = 'STANDARD' OR type = 'PO BOX') \n" +
                     (!cityExceptions.contains(city) ? " AND (locationType = 'PRIMARY' OR locationType = 'ACCEPTABLE')"
                                                     : "");
        try {
            return run.query(sql, new ResultSetHandler<List<String>>() {
                @Override
                public List<String> handle(ResultSet rs) throws SQLException {
                    List<String> zip5s = new ArrayList<>();
                    while (rs.next()) {
                        zip5s.add(rs.getString(1));
                    }
                    return zip5s;
                }
            }, city);
        }
        catch (SQLException ex) {
            logger.error("Failed to get zip5 list by city!", ex);
        }
        return new ArrayList<>();
    }
}
