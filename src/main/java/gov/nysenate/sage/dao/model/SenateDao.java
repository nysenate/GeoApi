package gov.nysenate.sage.dao.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.util.FormatUtil;
import gov.nysenate.services.model.District;
import gov.nysenate.services.model.Senator;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class SenateDao extends BaseDao
{
    private static Logger logger = Logger.getLogger(SenateDao.class);
    private QueryRunner run = getQueryRunner();

    /** Mapper used to serialize into json */
    private ObjectMapper mapper = new ObjectMapper();

    /** Cached district code, Senator map */
    protected static Map<Integer, Senator> senatorMap;

    /**
     * Retrieves senators from the database and puts Senator objects into the cache map.
     * The senator data is deserialzed from the JSON representation of the Senator object
     * that is stored in the database.
     */
    private class SenatorMapHandler implements ResultSetHandler<Map<Integer,Senator>>
    {
        @Override
        public Map<Integer,Senator> handle(ResultSet rs) throws SQLException
        {
            while (rs.next()) {
                Integer senateCode = rs.getInt("district");
                String name = rs.getString("name");
                String json = rs.getString("data");
                try {
                    Senator senator = mapper.readValue(json, Senator.class);
                    senatorMap.put(senateCode, senator);
                }
                catch (Exception ex){
                    logger.error("Failed to get senator data for " + name, ex);
                }
            }
            logger.info("Cached " + senatorMap.size() + " senators.");
            return senatorMap;
        }
    }

    public SenateDao()
    {
        getSenatorMap();
    }

    /**
     * Retrieve a collection of all Senators.
     * @return Collection of Senator
     */
    public Collection<Senator> getSenators()
    {
        return getSenatorMap().values();
    }

    /**
     * Retrieve a senator by the senate district number.
     * @param senateCode Senate district number
     * @return           Senator
     */
    public Senator getSenatorByDistrict(int senateCode)
    {
        return getSenatorMap().get(senateCode);
    }

    /**
     * Creates the senate map if it does not exist and then returns it.
     * @return
     */
    private Map<Integer,Senator> getSenatorMap()
    {
        if (senatorMap == null) {
            senatorMap = new HashMap<>();

            String sql = "SELECT * FROM senator";
            try {
                run.query(sql, new SenatorMapHandler());
            }
            catch (SQLException ex) {
                logger.error(ex.getMessage());
            }
        }
        return senatorMap;
    }

    /**
     * Inserts a Senator into the database.
     * @param senator
     */
    public void insertSenator(Senator senator)
    {
        String sql = "INSERT INTO senator (district, name, data) VALUES (?,?,?);";

        int senateCode = senator.getDistrict().getNumber();
        String senatorName = senator.getName();
        String json = FormatUtil.toJsonString(senator);

        try {
            int numRows = run.update(sql, senateCode, senatorName, json);
            if (numRows > 0) { logger.info("Added data for Senator " + senatorName); }
        }
        catch (SQLException ex) {
            logger.error(ex.getMessage());
        }
    }

    /**
     * Inserts a senate district and it's associated url into the database.
     * @param district
     */
    public void insertSenate(District district)
    {
        Integer senateCode = district.getNumber();
        String url = district.getUrl();

        String sql = "INSERT INTO senate (district, url) VALUES (?,?);";
        try {
            int numRows = run.update(sql, senateCode, url);
            if (numRows > 0) { logger.info("Added data for senate district " + senateCode); }
        }
        catch (SQLException ex) {
            logger.error(ex.getMessage());
        }
    }

    /**
     * Clears the senate table. Note that this method can only be called
     * after clearing the senator table since there is a foreign key constraint.
     */
    public void deleteSenateDistricts()
    {
        String sql = "DELETE FROM senate";
        try {
            run.update(sql);
        }
        catch (SQLException ex) {
            logger.error("Failed to delete senate districts " + ex.getMessage());
        }
    }

    /**
     * Clears the senator table.
     */
    public void deleteSenators()
    {
        String sql = "DELETE FROM senator";
        try {
            run.update(sql);
        }
        catch (SQLException ex) {
            logger.error("Failed to delete senators " + ex.getMessage());
        }
    }
}
