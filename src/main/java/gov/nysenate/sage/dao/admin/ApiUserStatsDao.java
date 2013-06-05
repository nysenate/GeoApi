package gov.nysenate.sage.dao.admin;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.admin.ApiUserStats;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ApiUserStatsDao extends BaseDao
{
    private static Logger logger = Logger.getLogger(ApiUserStatsDao.class);
    private QueryRunner run = getQueryRunner();

    public List<ApiUserStats> getAllApiUserStats()
    {
        String sql = "";
        try {
            return run.query(sql, null);
        }
        catch (SQLException ex) {
            logger.error("Failed to get ApiUser stats!", ex);
        }
        return null;
    }

    public static class ApiUserStatsListHandler implements ResultSetHandler<List<ApiUserStats>>
    {
        @Override
        public List<ApiUserStats> handle(ResultSet rs) throws SQLException {
            List<ApiUserStats> apiUserStats = new ArrayList<>();
            while (rs.next()) {

            }
            return null;
        }
    }
}
