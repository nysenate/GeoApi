package gov.nysenate.sage.dao.stats.exception;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.logger.apirequest.SqlApiRequestLogger;
import gov.nysenate.sage.dao.stats.deployment.SqlDeploymentStatsDao;
import gov.nysenate.sage.model.stats.ExceptionInfo;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class SqlExceptionInfoDao
{
    private static Logger logger = LoggerFactory.getLogger(SqlDeploymentStatsDao.class);

    private String SCHEMA = "log";
    private String TABLE = "exception";

    private QueryRunner run;

    private SqlApiRequestLogger sqlApiRequestLogger;
    private BaseDao baseDao;

    @Autowired
    public SqlExceptionInfoDao(SqlApiRequestLogger sqlApiRequestLogger, BaseDao baseDao) {
        this.sqlApiRequestLogger = sqlApiRequestLogger;
        this.baseDao = baseDao;
        run = baseDao.getQueryRunner();
    }

    /**
     * Retrieves a list of all unhandled exceptions.
     * @param excludeHidden If true only non-hidden exceptions will be retrieved.
     * @return List<ExceptionInfo>
     */
    public List<ExceptionInfo> getExceptionInfoList(Boolean excludeHidden)
    {
        String sql = "SELECT * FROM " + SCHEMA + "." + TABLE + "\n" +
                     ((excludeHidden) ? "WHERE hidden = false \n" : "") +
                     "ORDER BY catchTime DESC";
        try {
            return run.query(sql, new ExceptionInfoListHandler(this.sqlApiRequestLogger));
        }
        catch (SQLException ex) {
            logger.error("Failed to retrieve exception info list!", ex);
        }
        return null;
    }

    /**
     * Marks an exception as hidden so that it won't appear in the interface.
     * @param id Id of the exception info.
     */
    public int hideExceptionInfo(int id)
    {
        String sql = "UPDATE " + SCHEMA + "." + TABLE + "\n" +
                     "SET hidden = true \n" +
                     "WHERE id = ?";
        try {
            return run.update(sql, id);
        }
        catch (SQLException ex) {
            logger.error("Failed to hide exception with id: " + id, ex);
        }
        return 0;
    }

    /**
     * Handler implementation to create List<ExceptionInfo> from the result set.
     */
    private static class ExceptionInfoListHandler implements ResultSetHandler<List<ExceptionInfo>>
    {
        private SqlApiRequestLogger sqlApiRequestLogger;

        public ExceptionInfoListHandler(SqlApiRequestLogger sqlApiRequestLogger) {
            this.sqlApiRequestLogger = sqlApiRequestLogger;
        }

        @Override
        public List<ExceptionInfo> handle(ResultSet rs) throws SQLException {
            List<ExceptionInfo> exceptionInfoList = new ArrayList<>();
            while (rs.next()) {
                ExceptionInfo ei = new ExceptionInfo();
                ei.setId(rs.getInt("id"));
                ei.setApiRequest(sqlApiRequestLogger.getApiRequest(rs.getInt("apiRequestId")));
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
