package gov.nysenate.sage.controller.job;

import gov.nysenate.sage.client.response.job.JobStatusResponse;
import gov.nysenate.sage.dao.model.job.SqlJobProcessDao;
import gov.nysenate.sage.model.job.JobProcessStatus;
import gov.nysenate.sage.model.job.JobUser;
import gov.nysenate.sage.model.result.JobErrorResult;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static gov.nysenate.sage.util.controller.JobControllerUtil.getJobUser;

/**
 * This controller provides an API for accessing the status of a batch job request.
 */
@RestController
@RequestMapping(value = "/job/status")
public class JobStatusController {
    private static final String TEMP_DIR = "/tmp";
    private static final String LOCK_FILENAME = "batchJobProcess.lock";
    private final SqlJobProcessDao sqlJobProcessDao;

    @Autowired
    public JobStatusController(SqlJobProcessDao sqlJobProcessDao) {
        this.sqlJobProcessDao = sqlJobProcessDao;
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
        return getResponse(request, jobUser ->
                sqlJobProcessDao.getJobStatusesByCondition(JobProcessStatus.Condition.RUNNING, jobUser));
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
        return getResponse(request, sqlJobProcessDao::getActiveJobStatuses);
    }

    /**
     * Completed Job Processes Api
     * ---------------------
     * Get processes that completed successfully within the past day
     * Usage:
     * (GET)    /job/status/completed
     *
     */
    @GetMapping(value = "/completed")
    public Object jobCompleted(HttpServletRequest request) {
        return getResponse(request, jobUser ->
                sqlJobProcessDao.getRecentlyCompletedJobStatuses(JobProcessStatus.Condition.COMPLETED, jobUser));
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
        return getResponse(this::isProcessorRunning);
    }

    /**
     * All Job Processes Api
     * ---------------------
     * Get all processes (basically a job history)
     * Usage:
     * (GET)    /job/status/all
     *
     * @param request HttpServletRequest
     *
     */
    @GetMapping(value = "/all")
    public Object jobAll(HttpServletRequest request) {
        return getResponse(request, jobUser -> sqlJobProcessDao.getJobStatusesByConditions(
                List.of(JobProcessStatus.Condition.values()), jobUser
        ));
    }

    private static Object getResponse(Supplier<Object> responseGetter) {
        if (!SecurityUtils.getSubject().hasRole("JOB_USER")) {
            return new JobErrorResult("Failed to process request!");
        }
        return responseGetter.get();
    }

    private Object getResponse(HttpServletRequest request, Function<JobUser, List<JobProcessStatus>> jpsSupplier) {
        return getResponse(() -> {
            JobUser jobUser = getJobUser(request);
            return new JobStatusResponse(jpsSupplier.apply(jobUser), isProcessorRunning());
        });
    }

    private boolean isProcessorRunning() {
        return new File(TEMP_DIR, LOCK_FILENAME).exists();
    }
}