package gov.nysenate.sage.dao.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.job.JobProcess;
import gov.nysenate.sage.model.job.JobProcessStatus;
import gov.nysenate.sage.util.FormatUtil;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * JobProcessDao provides persistence for submitted requests and process statuses.
 */
public class JobProcessDao extends BaseDao
{
    private String SCHEMA = "job";
    private String TABLE = "process";
    private String STATUS_TABLE = "status";
    private Logger logger = Logger.getLogger(JobUserDao.class);
    private ResultSetHandler<JobProcess> processHandler = new JobProcessHandler();
    private ResultSetHandler<JobProcessStatus> statusHandler = new JobStatusHandler();
    private ResultSetHandler<List<JobProcess>> processListHandler = new JobProcessListHandler();
    private QueryRunner run = getQueryRunner();

    public JobProcessDao() {}

    /**
     * Adds a new job process to the database queue.
     * @param p JobProcess to insert
     * @return The id of the inserted job process, or -1 on failure
     */
    public int addJobProcess(JobProcess p)
    {
        String sql = "INSERT INTO " + getTableName() + " (userId, fileName, fileType, sourceFileName, requestTime, recordCount) " +
                     "VALUES (?,?,?,?,?,?) RETURNING id";
        try {
            return run.query(sql, new ResultSetHandler<Integer>() {
                @Override public Integer handle(ResultSet rs)
                          throws SQLException { return (rs.next()) ? rs.getInt("id") : -1; }
            }, p.getRequestor().getId(), p.getFileName(), p.getFileType(), p.getSourceFileName(), p.getRequestTime(), p.getRecordCount());
        }
        catch (SQLException ex) {
            logger.error("Failed to add job process!", ex);
        }
        return -1;
    }

    public JobProcess getJobProcessById(int id)
    {
        String sql = "SELECT * FROM " + getTableName() + " WHERE id = ?";
        try {
            return run.query(sql, processHandler, id);
        }
        catch (SQLException ex) {
            logger.error("Failed to retrieve job process by id!", ex);
        }
        return null;
    }

    public List<JobProcess> getJobProcessesByUser(int userId)
    {
        String sql = "SELECT * FROM " + getTableName() + " WHERE userId = ?";
        try {
            return run.query(sql, processListHandler, userId);
        }
        catch (SQLException ex) {
            logger.error("Failed to retrieve job processes by user id!", ex);
        }
        return null;
    }

    public int setJobProcessStatus(JobProcessStatus jps)
    {
        /** In order to allow this method to both insert and update a status record, an update query is run first.
         *  If it fails then we can insert a new record. */
        if (jps != null) {
            int processId = jps.getProcessId();
            String updateSql = "UPDATE " + getStatusTableName() + " SET processId = ?, condition = ?, completedRecords = ?, startTime = ?, completeTime = ?, completed = ?, messages = ? WHERE processId = ?";
            String insertSql = "INSERT INTO " + getStatusTableName() + " (processId, condition, completedRecords, startTime, completeTime, completed, messages) " +
                               "VALUES (?,?,?,?,?,?,?)";
            try {
                int res = run.update(updateSql, processId, jps.getCondition().name(), jps.getCompletedRecords(), jps.getStartTime(),
                              jps.getCompleteTime(), jps.isCompleted(), FormatUtil.toJsonString(jps.getMessages()), processId);
                if (res != 1) {
                    res = run.update(insertSql, processId, jps.getCondition().name(), jps.getCompletedRecords(), jps.getStartTime(),
                              jps.getCompleteTime(), jps.isCompleted(), FormatUtil.toJsonString(jps.getMessages()));
                }
                return res;
            }
            catch (SQLException ex) {
                logger.error("Failed to set job process status for process " + jps.getProcessId(), ex);
            }
        }
        else {
            logger.warn("Tried to set job status but a null status was supplied.");
        }
        return -1;
    }

    public JobProcessStatus getJobProcessStatus(int processId)
    {
        String sql = "SELECT * FROM " + getStatusTableName() + " WHERE processId = ?";
        try {
            return run.query(sql, statusHandler, processId);
        }
        catch(SQLException ex) {
            logger.error("Failed to retrieve job process status for process " + processId);
        }
        return null;
    }

    protected static class JobProcessHandler implements ResultSetHandler<JobProcess>
    {
        @Override
        public JobProcess handle(ResultSet rs) throws SQLException
        {
            JobProcess jobProcess = null;
            JobUserDao jobUserDao = new JobUserDao();
            if (rs.next()) {
                jobProcess = getJobProcessFromResultSet(rs, jobUserDao);
            }
            return jobProcess;
        }
    }

    protected static class JobStatusHandler implements ResultSetHandler<JobProcessStatus>
    {
        protected static ObjectMapper jsonMapper = new ObjectMapper();
        protected static Logger logger = Logger.getLogger(JobStatusHandler.class);

        @Override
        public JobProcessStatus handle(ResultSet rs) throws SQLException {
            JobProcessStatus jps = null;
            if (rs.next()) {
                jps = new JobProcessStatus();
                jps.setProcessId(rs.getInt("processId"));
                jps.setStartTime(rs.getTimestamp("startTime"));
                jps.setCompleteTime(rs.getTimestamp("completeTime"));
                jps.setCondition(JobProcessStatus.Condition.valueOf(rs.getString("condition")));
                jps.setCompleted(rs.getBoolean("completed"));
                jps.setCompletedRecords(rs.getInt("completedRecords"));
                try {
                    jps.setMessages(Arrays.asList(jsonMapper.readValue(rs.getString("messages"), String[].class)));
                }
                catch (Exception ex) {
                    logger.error("Failed to retrieve job status messages list!", ex);
                }
            }
            return jps;
        }
    }

    protected static class JobProcessListHandler implements ResultSetHandler<List<JobProcess>>
    {
        @Override
        public List<JobProcess> handle(ResultSet rs) throws SQLException
        {
            List<JobProcess> jobProcesses = new ArrayList<>();
            JobUserDao jobUserDao = new JobUserDao();
            while (rs.next()) {
                jobProcesses.add(getJobProcessFromResultSet(rs, jobUserDao));
            }
            return jobProcesses;
        }
    }

    protected static JobProcess getJobProcessFromResultSet(ResultSet rs, JobUserDao juDao) throws SQLException
    {
        JobProcess jobProcess = new JobProcess();
        jobProcess.setId(rs.getInt("id"));
        jobProcess.setRequestor(juDao.getJobUserById(rs.getInt("userId")));
        jobProcess.setFileName(rs.getString("fileName"));
        jobProcess.setFileType(rs.getString("fileType"));
        jobProcess.setSourceFileName(rs.getString("sourceFileName"));
        jobProcess.setRequestTime(rs.getTimestamp("requestTime"));
        jobProcess.setRecordCount(rs.getInt("recordCount"));
        return jobProcess;
    }

    private String getTableName()
    {
        return SCHEMA + "." + TABLE;
    }

    private String getStatusTableName()
    {
        return SCHEMA + "." + STATUS_TABLE;
    }
}