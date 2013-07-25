package gov.nysenate.sage.dao.stats;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.logger.ApiRequestLogger;
import gov.nysenate.sage.model.stats.Deployment;
import gov.nysenate.sage.model.stats.ExceptionInfo;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ExceptionInfoDao extends BaseDao
{
    private static Logger logger = Logger.getLogger(DeploymentStatsDao.class);

    private String SCHEMA = "log";
    private String TABLE = "exception";

    private QueryRunner run = getQueryRunner();

    public List<ExceptionInfo> getExceptionInfoList()
    {
        String sql = "SELECT * FROM " + SCHEMA + "." + TABLE + "\n" +
                     "ORDER BY catchTime DESC";
        try {
            return run.query(sql, new ExceptionInfoListHandler());
        }
        catch (SQLException ex) {
            logger.error("Failed to retrieve exception info list!", ex);
        }
        return null;
    }

    private static class ExceptionInfoListHandler implements ResultSetHandler<List<ExceptionInfo>>
    {
        private static ApiRequestLogger apiRequestLogger = new ApiRequestLogger();

        @Override
        public List<ExceptionInfo> handle(ResultSet rs) throws SQLException {
            List<ExceptionInfo> exceptionInfoList = new ArrayList<>();
            while (rs.next()) {
                ExceptionInfo ei = new ExceptionInfo();
                ei.setApiRequest(apiRequestLogger.getApiRequest(rs.getInt("apiRequestId")));
                ei.setExceptionType(rs.getString("type"));
                ei.setMessage(rs.getString("message"));
                ei.setStackTrace(rs.getString("stackTrace"));
                ei.setCatchTime(rs.getTimestamp("catchTime"));
                exceptionInfoList.add(ei);
            }
            return exceptionInfoList;
        }
    }
}
