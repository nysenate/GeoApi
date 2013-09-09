package gov.nysenate.sage.dao.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.job.JobProcess;
import gov.nysenate.sage.model.job.JobProcessStatus;
import gov.nysenate.sage.model.job.JobUser;
import gov.nysenate.sage.util.FormatUtil;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static gov.nysenate.sage.model.job.JobProcessStatus.Condition;

/**
 * JobProcessDao provides persistence for submitted requests and process statuses.
 */
public class JobProcessDao extends BaseDao
{
    private static String SCHEMA = "job";
    private static String TABLE = "process";
    private static String STATUS_TABLE = "status";
    private Logger logger = Logger.getLogger(JobUserDao.class);
    private ResultSetHandler<JobProcess> processHandler = new JobProcessHandler();
    private ResultSetHandler<JobProcessStatus> statusHandler = new JobStatusHandler();
    private ResultSetHandler<List<JobProcess>> processListHandler = new JobProcessListHandler();
    private ResultSetHandler<List<JobProcessStatus>> statusListHandler = new JobProcessStatusListHandler();
    private QueryRunner run = getQueryRunner();

    public JobProcessDao() {}

    /**
     * Adds a new job process to the database queue.
     * @param p JobProcess to insert
     * @return The id of the inserted job process, or -1 on failure
     */
    public int addJobProcess(JobProcess p)
    {
        String sql = "INSERT INTO " + getProcessTableName() + " (userId, fileName, fileType, sourceFileName, requestTime, recordCount, validationReq, geocodeReq, districtReq) " +
                     "VALUES (?,?,?,?,?,?,?,?,?) RETURNING id";
        try {
            return run.query(sql, new ResultSetHandler<Integer>() {
                @Override public Integer handle(ResultSet rs)
                          throws SQLException { return (rs.next()) ? rs.getInt("id") : -1; }
            }, p.getRequestor().getId(), p.getFileName(), p.getFileType(), p.getSourceFileName(), p.getRequestTime(), p.getRecordCount(),
               p.isValidationRequired(), p.isGeocodeRequired(), p.isDistrictRequired());
        }
        catch (SQLException ex) {
            logger.error("Failed to add job process!", ex);
        }
        return -1;
    }

    /**
     * Get JobProcess by job process id.
     * @param id int
     * @return JobProcess
     */
    public JobProcess getJobProcessById(int id)
    {
        String sql = "SELECT * FROM " + getProcessTableName() + " WHERE id = ?";
        try {
            return run.query(sql, processHandler, id);
        }
        catch (SQLException ex) {
            logger.error("Failed to retrieve job process by id!", ex);
        }
        return null;
    }

    /**
     * Retrieve List of JobProcess objects by job userId.
     * @param userId int
     * @return List<JobProcess>
     */
    public List<JobProcess> getJobProcessesByUser(int userId)
    {
        String sql = "SELECT * FROM " + getProcessTableName() + " WHERE userId = ?";
        try {
            return run.query(sql, processListHandler, userId);
        }
        catch (SQLException ex) {
            logger.error("Failed to retrieve job processes by user id!", ex);
        }
        return null;
    }

    /**
     * Update or insert a JobProcessStatus. If a job status entry already exists, the record will be updated with the
     * new information. Otherwise a new row will be created.
     * @param jps JobProcessStatus
     * @return int (rows affected) or -1 if failed.
     */
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

    /**
     * Retrieve JobProcessStatus by processId.
     * @param processId int
     * @return JobProcessStatus
     */
    public JobProcessStatus getJobProcessStatus(int processId)
    {
        String sql = "SELECT * FROM " + getProcessTableName() + " \n" +
                     "LEFT JOIN " + getStatusTableName() + " status ON id = processId " +
                     "WHERE processId = ?";
        try {
            return run.query(sql, statusHandler, processId);
        }
        catch(SQLException ex) {
            logger.error("Failed to retrieve job process status for process " + processId, ex);
        }
        return null;
    }

    /**
     * Retrieves a List of JobProcessStatus matching the given Condition with no filtering on requestTime.
     * Delegates to getJobStatusesByConditions(), see method for details.
     * @return List<JobProcessStatus>
     */
    public List<JobProcessStatus> getJobStatusesByCondition(Condition condition, JobUser jobUser)
    {
        return getJobStatusesByCondition(condition, jobUser, null, null);
    }

    /**
     * Retrieves a List of JobProcessStatus matching the given Condition.
     * Delegates to getJobStatusesByConditions(), see method for details.
     * @return List<JobProcessStatus>
     */
    public List<JobProcessStatus> getJobStatusesByCondition(Condition condition, JobUser jobUser, Timestamp start, Timestamp end)
    {
        return getJobStatusesByConditions(Arrays.asList(condition), jobUser, start, end);
    }

    /**
     * Retrieves a List of JobProcessStatus matching the given Condition types.
     * @param conditions List of Condition objects to filter results by.
     * @param jobUser JobUser to retrieve results for. If null or admin user, all results returned.
     * @param start Start requestTime to filter results by. If null, start will be defaulted to 0 UTC.
     * @param end End requestTime to filter results by. If null, end will be defaulted to current time.
     * @return List<JobProcessStatus>
     */
    public List<JobProcessStatus> getJobStatusesByConditions(List<Condition> conditions, JobUser jobUser, Timestamp start, Timestamp end)
    {
        String sql = "SELECT * FROM " + getProcessTableName() + "\n" +
                     "LEFT JOIN " + getStatusTableName() + " status ON id = processId " +
                     "WHERE (%s) " +  // Condition Filter
                     "AND (%s) " +    // JobUser Filter
                     "AND (%s) " +    // Request Time Filter
                     "ORDER BY processId DESC";
        List<String> where = new ArrayList<>();
        for (Condition c : conditions) {
            where.add("status.condition = '" + c.name() + "'");
        }
        String conditionFilter = (!where.isEmpty()) ? StringUtils.join(where, " OR ") : "FALSE";
        String jobUserFilter = (jobUser != null && !jobUser.isAdmin()) ? "userId = " + jobUser.getId() : "TRUE";

        /** If start and end timestamps are null, set to earliest and current time respectively */
        start = (start == null) ? new Timestamp(0) : start;
        end = (end == null) ? new Timestamp(new Date().getTime()) : end;
        String requestTimeFilter = "requestTime >= ? AND requestTime <= ?";

        sql = String.format(sql, conditionFilter, jobUserFilter, requestTimeFilter);
        try {
            return run.query(sql, statusListHandler, start, end);
        }
        catch (SQLException ex){
            logger.error("Failed to retrieve statuses by conditions!", ex);
        }
        return null;
    }

    /**
     * Gets completed job statuses that finished on or after the 'afterThis' timestamp.
     * This method is different from getJobStatusesByCondition because the completeTime is filtered on
     * as opposed to the requestTime.
     * @param condition Condition to filter by. If null, no filtering will occur on condition.
     * @param jobUser JobUser to retrieve results for. If null or admin user, all results returned.
     * @param afterThis Filter results where completeTime is on or after the 'afterThis' timestamp.
     * @return List<JobProcessStatus>
     */
    public List<JobProcessStatus> getRecentlyCompletedJobStatuses(Condition condition, JobUser jobUser, Timestamp afterThis)
    {
        String sql = "SELECT * FROM " + getProcessTableName() + " " +
                     "LEFT JOIN " + getStatusTableName() + " status " + "ON id = processId " +
                     "WHERE status.completeTime >= ? " + // completeTime filter
                     "AND (%s) " + // condition filter
                     "AND (%s) " + // jobUser filter
                     "ORDER BY status.completeTime DESC";

        String conditionFilter = (condition != null) ? "status.condition = ?" : "TRUE";
        String jobUserFilter = (jobUser != null && !jobUser.isAdmin()) ? "userId = " + jobUser.getId() : "TRUE";
        sql = String.format(sql, conditionFilter, jobUserFilter);
        try {
            if (condition != null) {
                return run.query(sql, statusListHandler, afterThis, condition.name());
            }
            return run.query(sql, statusListHandler, afterThis);
        }
        catch (SQLException ex){
            logger.error("Failed to retrieve recent job statuses!", ex);
        }
        return null;
    }

    public List<JobProcessStatus> getActiveJobStatuses(JobUser jobUser)
    {
        return getJobStatusesByConditions(Condition.getActiveConditions(), jobUser, null, null);
    }

    public List<JobProcessStatus> getInactiveJobStatuses(JobUser jobUser)
    {
        return  getJobStatusesByConditions(Condition.getInactiveConditions(), jobUser, null, null);
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
        protected static Logger logger = Logger.getLogger(JobStatusHandler.class);
        protected JobUserDao jobUserDao = new JobUserDao();

        @Override
        public JobProcessStatus handle(ResultSet rs) throws SQLException {
            JobProcessStatus jps = null;
            if (rs.next()) {
                jps = getJobProcessStatusFromResultSet(rs, jobUserDao, logger);
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

    protected static class JobProcessStatusListHandler implements ResultSetHandler<List<JobProcessStatus>>
    {
        protected static Logger logger = Logger.getLogger(JobStatusHandler.class);
        protected JobUserDao jobUserDao = new JobUserDao();

        @Override
        public List<JobProcessStatus> handle(ResultSet rs) throws SQLException
        {
            List<JobProcessStatus> jobProcessStatuses = new ArrayList<>();
            while (rs.next()) {
                jobProcessStatuses.add(getJobProcessStatusFromResultSet(rs, jobUserDao, logger));
            }
            return jobProcessStatuses;
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
        jobProcess.setValidationRequired(rs.getBoolean("validationReq"));
        jobProcess.setGeocodeRequired(rs.getBoolean("geocodeReq"));
        jobProcess.setDistrictRequired(rs.getBoolean("districtReq"));
        return jobProcess;
    }

    protected static JobProcessStatus getJobProcessStatusFromResultSet(ResultSet rs, JobUserDao jud, Logger logger) throws SQLException
    {
        ObjectMapper jsonMapper = new ObjectMapper();
        JobProcessStatus jps;
        jps = new JobProcessStatus();
        jps.setProcessId(rs.getInt("processId"));
        jps.setJobProcess(getJobProcessFromResultSet(rs, jud));
        jps.setStartTime(rs.getTimestamp("startTime"));
        jps.setCompleteTime(rs.getTimestamp("completeTime"));
        jps.setCondition(Condition.valueOf(rs.getString("condition")));
        jps.setCompleted(rs.getBoolean("completed"));
        jps.setCompletedRecords(rs.getInt("completedRecords"));
        try {
            jps.setMessages(Arrays.asList(jsonMapper.readValue(rs.getString("messages"), String[].class)));
        }
        catch (Exception ex) {
            logger.error("Failed to retrieve job status messages list!", ex);
        }
        return jps;
    }

    private String getProcessTableName()
    {
        return SCHEMA + "." + TABLE;
    }

    private String getStatusTableName()
    {
        return SCHEMA + "." + STATUS_TABLE;
    }
}