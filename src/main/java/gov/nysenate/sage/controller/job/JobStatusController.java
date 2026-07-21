package gov.nysenate.sage.controller.job;

import gov.nysenate.sage.client.response.job.JobStatusResponse;
import gov.nysenate.sage.dao.model.job.SqlJobProcessDao;
import gov.nysenate.sage.model.job.JobProcessStatus;
import gov.nysenate.sage.model.job.JobUser;
import gov.nysenate.sage.model.result.JobErrorResult;
import gov.nysenate.sage.service.job.JobBatchProcessor;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static gov.nysenate.sage.util.controller.JobControllerUtil.getJobUser;

/**
 * This controller provides an API for accessing the status of a batch job request.
 */
@RestController
@RequestMapping(value = "/job/status")
public class JobStatusController {
    private final SqlJobProcessDao sqlJobProcessDao;
    private final JobBatchProcessor jobBatchProcessor;

    @Autowired
    public JobStatusController(SqlJobProcessDao sqlJobProcessDao, JobBatchProcessor jobBatchProcessor) {
        this.sqlJobProcessDao = sqlJobProcessDao;
        this.jobBatchProcessor = jobBatchProcessor;
    }

    /**
     * Running Job Processes Api
     * ---------------------
     * Get all running jobs
     * Usage:
     * (GET)    /job/status/running
     *
     */
    @GetMapping(value = "/running")
    public Object jobRunning(HttpServletRequest request) {
        return getResponse(request, JobProcessStatus.Condition.RUNNING);
    }

    /**
     * Active Job Processes Api
     * ---------------------
     * Get all active jobs
     * Usage:
     * (GET)    /job/status/active
     *
     */
    @GetMapping(value = "/active")
    public Object jobActive(HttpServletRequest request) {
        return getResponse(request, JobProcessStatus.Condition.WAITING_FOR_CRON, JobProcessStatus.Condition.RUNNING);
    }

    /**
     * Job History Api
     * ---------------------
     * Get all processes (basically a job history), optionally filtered by the
     * year they were requested in and by their status condition.
     * Usage:
     * (GET)    /job/status/history
     *
     * @param year      Optional request year to filter by, e.g. 2026. If absent, all years are included.
     * @param condition Optional Condition to filter by, e.g. COMPLETED. If absent, all conditions are included.
     *
     */
    @GetMapping(value = "/history")
    public Object jobHistory(HttpServletRequest request,
                             @RequestParam(required = false) Integer year,
                             @RequestParam(required = false) JobProcessStatus.Condition condition) {
        Timestamp start = (year == null) ? null : Timestamp.valueOf(LocalDate.of(year, 1, 1).atStartOfDay());
        Timestamp end = (year == null) ? null : Timestamp.valueOf(LocalDate.of(year + 1, 1, 1).atStartOfDay());
        return getResponse(request, start, end, condition);
    }

    /**
     * Recently Completed Job Processes Api
     * ---------------------
     * Get processes that completed successfully within the past day
     * Usage:
     * (GET)    /job/status/recentlyCompleted
     *
     */
    @GetMapping(value = "/recentlyCompleted")
    public Object jobRecentlyCompleted(HttpServletRequest request) {
        Timestamp end = Timestamp.valueOf(LocalDateTime.now());
        Timestamp start = Timestamp.valueOf(LocalDateTime.now().minusDays(1));
        return getResponse(request, start, end, JobProcessStatus.Condition.COMPLETED);
    }

    /**
     * Processor Api
     * ---------------------
     * Get the current status of the processor (Is it running or not)
     * Usage:
     * (GET)    /job/status/processor
     *
     */
    @GetMapping(value = "/processor")
    public Object jobProcessor() {
        return getResponse(jobBatchProcessor::isRunning);
    }

    private static Object getResponse(Supplier<Object> responseGetter) {
        if (!SecurityUtils.getSubject().hasRole("JOB_USER")) {
            return new JobErrorResult("Failed to process request!");
        }
        return responseGetter.get();
    }

    private Object getResponse(HttpServletRequest request, JobProcessStatus.Condition... conditions) {
        return getResponse(request, null, null, conditions);
    }

    private Object getResponse(HttpServletRequest request, Timestamp start, Timestamp end, JobProcessStatus.Condition... conditions) {
        return getResponse(() -> {
            JobUser jobUser = getJobUser(request);
            List<JobProcessStatus.Condition> conditionsList = Arrays.stream(conditions).filter(Objects::nonNull).toList();
            List<JobProcessStatus> processStatuses = sqlJobProcessDao.getJobStatusesByConditions(conditionsList, jobUser, start, end);
            return new JobStatusResponse(processStatuses, jobBatchProcessor.isRunning());
        });
    }
}
