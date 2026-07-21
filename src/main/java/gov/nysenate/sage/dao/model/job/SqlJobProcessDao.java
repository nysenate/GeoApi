package gov.nysenate.sage.dao.model.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.job.JobProcess;
import gov.nysenate.sage.model.job.JobProcessStatus;
import gov.nysenate.sage.model.job.JobUser;
import gov.nysenate.sage.util.FormatUtil;
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
import java.util.List;

import static gov.nysenate.sage.model.job.JobProcessStatus.Condition;

/**
 * JobProcessDao provides persistence for submitted requests and process statuses.
 */
@Repository
public class SqlJobProcessDao extends BaseDao implements JobProcessDao {
    private static final Logger logger = LoggerFactory.getLogger(SqlJobProcessDao.class);
    private final SqlJobUserDao jobUserDao;

    @Autowired
    public SqlJobProcessDao(SqlJobUserDao sqlJobUserDao) {
        this.jobUserDao = sqlJobUserDao;
    }

    /** {@inheritDoc} */
    public int addJobProcess(JobProcess p) {
        try {
            var params = new MapSqlParameterSource("userId", p.getRequestor().getId())
                    .addValue("fileName", p.getFileName())
                    .addValue("fileType", p.getFileType())
                    .addValue("sourceFileName", p.getSourceFileName())
                    .addValue("requestTime", p.getRequestTime())
                    .addValue("recordCount", p.getRecordCount())
                    .addValue("validationReq", p.isValidationRequired())
                    .addValue("geocodeReq", p.isGeocodeRequired())
                    .addValue("districtReq", p.isDistrictRequired());

            List<Integer> jobProcessIdList = namedJdbcTemplate.query(
                    JobProcessQuery.INSERT_JOB_PROCESS.getSql(getJobSchema()), params, new JobProcessIdHandler());

            if (jobProcessIdList.getFirst() != null) {
                return jobProcessIdList.getFirst();
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
            var params = new MapSqlParameterSource("processId", processId)
                    .addValue("condition", jps.getCondition().name())
                    .addValue("completedRecords", jps.getCompletedRecords())
                    .addValue("startTime", jps.getStartTime())
                    .addValue("completeTime", jps.getCompleteTime())
                    .addValue("completed", jps.isCompleted())
                    .addValue("messages", FormatUtil.toJsonString(jps.getMessages()));
            try {
                return namedJdbcTemplate.update(
                        JobProcessQuery.INSERT_JOB_PROCESS_STATUS.getSql(getJobSchema()), params);
            }
            catch (Exception ex) {
                try {
                    return namedJdbcTemplate.update(
                            JobProcessQuery.UPDATE_JOB_PROCESS_STATUS.getSql(getJobSchema()), params);
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
            var params = new MapSqlParameterSource("processId", processId);
            List<JobProcessStatus> jobProcessStatusList = namedJdbcTemplate.query(
                    JobProcessQuery.GET_JOB_PROCESS_STATUS.getSql(getJobSchema()), params, new JobStatusHandler());

            if (jobProcessStatusList.getFirst() != null) {
                return jobProcessStatusList.getFirst();
            }
        } catch (Exception ex) {
            logger.error("Failed to retrieve job process status for process {}", processId, ex);
        }
        return null;
    }

    /** {@inheritDoc} */
    public List<JobProcessStatus> getJobStatusesByConditions(List<Condition> conditions, JobUser jobUser, Timestamp start, Timestamp end) {
        // An empty list would render an invalid "IN ()", and means the same thing as no filter.
        List<Condition> conditionFilter = (conditions == null || conditions.isEmpty())
                ? List.of(Condition.values()) : conditions;
        var params = new MapSqlParameterSource()
                .addValue("conditions", conditionFilter.stream().map(Condition::name).toList())
                .addValue("startTime", (start == null) ? new Timestamp(0) : start)
                .addValue("endTime", (end == null) ? new Timestamp(System.currentTimeMillis()) : end);

        String sql = JobProcessQuery.GET_JOB_PROCESS_STATUS_BY_CONDITIONS.getSql(getJobSchema());
        System.out.println("*****\n" + sql + "\n*****");
        // Admins see every user's jobs, everyone else only sees their own.
        if (jobUser != null && !jobUser.isAdmin()) {
            sql += "\nAND userId = :userId";
            params.addValue("userId", jobUser.getId());
        }
        sql += "\nORDER BY processId DESC";

        try {
            return namedJdbcTemplate.query(sql, params, new JobStatusHandler());
        } catch (Exception ex) {
            logger.error("Failed to retrieve statuses by conditions!", ex);
        }
        return null;
    }

    private class JobStatusHandler implements RowMapper<JobProcessStatus> {
        @Override
        public JobProcessStatus mapRow(@Nonnull ResultSet rs, int rowNum) throws SQLException {
            var jsonMapper = new ObjectMapper();
            JobProcessStatus jps = new JobProcessStatus();
            jps.setProcessId(rs.getInt("processId"));
            jps.setJobProcess(getJobProcessFromResultSet(rs));
            jps.setStartTime(rs.getTimestamp("startTime"));
            jps.setCompleteTime(rs.getTimestamp("completeTime"));
            jps.setCondition(Condition.valueOf(rs.getString("condition")));
            jps.setCompleted(rs.getBoolean("completed"));
            jps.setCompletedRecords(rs.getInt("completedRecords"));
            try {
                jps.setMessages(List.of(jsonMapper.readValue(rs.getString("messages"), String[].class)));
            } catch (Exception ex) {
                logger.error("Failed to retrieve job status messages list!", ex);
            }
            return jps;
        }
    }

    private JobProcess getJobProcessFromResultSet(ResultSet rs) throws SQLException {
        var jobProcess = new JobProcess();
        jobProcess.setId(rs.getInt("id"));
        jobProcess.setRequestor(jobUserDao.getJobUserById(rs.getInt("userId")));
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

    private static class JobProcessIdHandler implements RowMapper<Integer> {
        @Override
        public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getInt("id");
        }
    }
}
