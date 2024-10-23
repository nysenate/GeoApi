package gov.nysenate.sage.dao.model.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.job.JobProcess;
import gov.nysenate.sage.model.job.JobProcessStatus;
import gov.nysenate.sage.model.job.JobUser;
import gov.nysenate.sage.util.FormatUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
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
@Repository
public class SqlJobProcessDao implements JobProcessDao {
    private static final Logger logger = LoggerFactory.getLogger(SqlJobProcessDao.class);
    private final RowMapper<JobProcessStatus> statusHandler;
    private final RowMapper<JobProcessStatus> statusListHandler;
    private final BaseDao baseDao;

    @Autowired
    public SqlJobProcessDao(BaseDao baseDao, SqlJobUserDao sqlJobUserDao) {
        this.baseDao = baseDao;
        this.statusHandler = new JobStatusHandler(sqlJobUserDao);
        this.statusListHandler = new JobProcessStatusListHandler(sqlJobUserDao);
    }

    /** {@inheritDoc} */
    public int addJobProcess(JobProcess p) {
        try {
            var params = new MapSqlParameterSource()
                    .addValue("userId", p.getRequestor().getId())
                    .addValue("fileName", p.getFileName())
                    .addValue("fileType", p.getFileType())
                    .addValue("sourceFileName", p.getSourceFileName())
                    .addValue("requestTime", p.getRequestTime())
                    .addValue("recordCount", p.getRecordCount())
                    .addValue("validationReq", p.isValidationRequired())
                    .addValue("geocodeReq", p.isGeocodeRequired())
                    .addValue("districtReq", p.isDistrictRequired());

            List<Integer> jobProcessIdList = baseDao.geoApiNamedJbdcTemplate.query(
                    JobProcessQuery.INSERT_JOB_PROCESS.getSql(baseDao.getJobSchema()), params, new JobProcessIdHandler());

            if (jobProcessIdList.get(0) != null) {
                return jobProcessIdList.get(0);
            }
        } catch (Exception ex) {
            logger.error("Failed to add job process!", ex);
        }
        return -1;
    }

    /** {@inheritDoc} */
    public int setJobProcessStatus(JobProcessStatus jps) {
        // In order to allow this method to both insert and update a status record, an update query is run first.
        // If it fails then we can insert a new record.
        if (jps != null) {
            int processId = jps.getProcessId();
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("processId", processId);
            params.addValue("condition", jps.getCondition().name());
            params.addValue("completedRecords", jps.getCompletedRecords());
            params.addValue("startTime", jps.getStartTime());
            params.addValue("completeTime", jps.getCompleteTime());
            params.addValue("completed", jps.isCompleted());
            params.addValue("messages", FormatUtil.toJsonString(jps.getMessages()));
            try {
                //insert sql
                return baseDao.geoApiNamedJbdcTemplate.update(
                        JobProcessQuery.INSERT_JOB_PROCESS_STATUS.getSql(baseDao.getJobSchema()), params);
            }
            catch (Exception ex) {
                //insert failed do update

                try {
                    return baseDao.geoApiNamedJbdcTemplate.update(
                            JobProcessQuery.UPDATE_JOB_PROCESS_STATUS.getSql(baseDao.getJobSchema()), params);
                }
                catch (Exception ex2) {
                    logger.error("Failed to set job process status for process {}", jps.getProcessId(), ex);
                }
            }
        } else {
            logger.warn("Tried to set job status but a null status was supplied.");
        }
        return -1;
    }

    /** {@inheritDoc} */
    public JobProcessStatus getJobProcessStatus(int processId) {
        try {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("processId", processId);

            List<JobProcessStatus> jobProcessStatusList = baseDao.geoApiNamedJbdcTemplate.query(
                    JobProcessQuery.GET_JOB_PROCESS_STATUS.getSql(baseDao.getJobSchema()), params, statusHandler);

            if (jobProcessStatusList.get(0) != null) {
                return jobProcessStatusList.get(0);
            }
        } catch (Exception ex) {
            logger.error("Failed to retrieve job process status for process " + processId, ex);
        }
        return null;
    }

    /** {@inheritDoc} */
    public List<JobProcessStatus> getJobStatusesByCondition(Condition condition, JobUser jobUser) {
        return getJobStatusesByCondition(condition, jobUser, null, null);
    }

    /** {@inheritDoc} */
    public List<JobProcessStatus> getJobStatusesByCondition(Condition condition, JobUser jobUser, Timestamp start, Timestamp end) {
        return getJobStatusesByConditions(List.of(condition), jobUser, start, end);
    }

    /** {@inheritDoc} */
    public List<JobProcessStatus> getJobStatusesByConditions(List<Condition> conditions, JobUser jobUser, Timestamp start, Timestamp end) {
        List<String> where = new ArrayList<>();
        for (Condition c : conditions) {
            where.add("status.condition = '" + c.name() + "'");
        }
        String conditionFilter = (!where.isEmpty()) ? StringUtils.join(where, " OR ") : "";
        String jobUserFilter = (jobUser != null && !jobUser.isAdmin()) ? " AND userId = " + jobUser.getId() : "";

        // If start and end timestamps are null, set to earliest and current time respectively
        start = (start == null) ? new Timestamp(0) : start;
        end = (end == null) ? new Timestamp(new Date().getTime()) : end;
        String requestTimeFilter = " AND requestTime >= '" + start + "' AND requestTime <= '" + end + "'";

        String restOfQuery = conditionFilter + " " + jobUserFilter + " " + requestTimeFilter + " ORDER BY processId DESC";
        try {

            return baseDao.geoApiNamedJbdcTemplate.query(
                    JobProcessQuery.GET_JOB_PROCESS_STATUS_BY_CONDITIONS.getSql(baseDao.getJobSchema()) + restOfQuery, statusListHandler);
        } catch (Exception ex) {
            logger.error("Failed to retrieve statuses by conditions!", ex);
        }
        return null;
    }

    /** {@inheritDoc} */
    public List<JobProcessStatus> getRecentlyCompletedJobStatuses(Condition condition, JobUser jobUser, Timestamp afterThis) {
        String conditionFilter = (condition != null) ? " AND status.condition = '" + condition.name() + "' ": " ";
        String jobUserFilter = (jobUser != null && !jobUser.isAdmin()) ? " AND userId = " + jobUser.getId() + " ": " ";

        String restOfQuery = conditionFilter + jobUserFilter + " ORDER BY status.completeTime DESC";

        try {
            var params = new MapSqlParameterSource("afterThis", afterThis);
            return baseDao.geoApiNamedJbdcTemplate.query(JobProcessQuery.GET_RECENTLY_COMPLETED_JOB_PROCESSES.getSql(baseDao.getJobSchema()) + restOfQuery, params, statusListHandler);
        } catch (Exception ex) {
            logger.error("Failed to retrieve recent job statuses!", ex);
        }
        return null;
    }

    /** {@inheritDoc} */
    public List<JobProcessStatus> getActiveJobStatuses(JobUser jobUser) {
        return getJobStatusesByConditions(Condition.getActiveConditions(), jobUser, null, null);
    }

    /** {@inheritDoc} */
    public List<JobProcessStatus> getInactiveJobStatuses(JobUser jobUser) {
        return getJobStatusesByConditions(Condition.getInactiveConditions(), jobUser, null, null);
    }

    private static class JobStatusHandler implements RowMapper<JobProcessStatus> {
        protected static Logger logger = LoggerFactory.getLogger(JobStatusHandler.class);
        protected SqlJobUserDao sqlJobUserDao;

        protected JobStatusHandler(SqlJobUserDao sqlJobUserDao) {
            this.sqlJobUserDao = sqlJobUserDao;
        }

        @Override
        public JobProcessStatus mapRow(@Nonnull ResultSet rs, int rowNum) throws SQLException {
            return getJobProcessStatusFromResultSet(rs, sqlJobUserDao, logger);
        }
    }

    private static class JobProcessStatusListHandler implements RowMapper<JobProcessStatus> {
        protected static Logger logger = LoggerFactory.getLogger(JobProcessStatusListHandler.class);
        protected SqlJobUserDao sqlJobUserDao;

        protected JobProcessStatusListHandler(SqlJobUserDao sqlJobUserDao) {
            this.sqlJobUserDao = sqlJobUserDao;
        }

        @Override
        public JobProcessStatus mapRow(@Nonnull ResultSet rs, int rowNum) throws SQLException {
            return getJobProcessStatusFromResultSet(rs, sqlJobUserDao, logger);
        }
    }

    protected static JobProcess getJobProcessFromResultSet(ResultSet rs, SqlJobUserDao juDao) throws SQLException {
        var jobProcess = new JobProcess();
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

    protected static JobProcessStatus getJobProcessStatusFromResultSet(ResultSet rs, SqlJobUserDao jud, Logger logger) throws SQLException {
        var jsonMapper = new ObjectMapper();
        JobProcessStatus jps = new JobProcessStatus();
        jps.setProcessId(rs.getInt("processId"));
        jps.setJobProcess(getJobProcessFromResultSet(rs, jud));
        jps.setStartTime(rs.getTimestamp("startTime"));
        jps.setCompleteTime(rs.getTimestamp("completeTime"));
        jps.setCondition(Condition.valueOf(rs.getString("condition")));
        jps.setCompleted(rs.getBoolean("completed"));
        jps.setCompletedRecords(rs.getInt("completedRecords"));
        try {
            jps.setMessages(Arrays.asList(jsonMapper.readValue(rs.getString("messages"), String[].class)));
        } catch (Exception ex) {
            logger.error("Failed to retrieve job status messages list!", ex);
        }
        return jps;
    }

    private static class JobProcessIdHandler implements RowMapper<Integer> {
        @Override
        public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getInt("id");
        }
    }
}