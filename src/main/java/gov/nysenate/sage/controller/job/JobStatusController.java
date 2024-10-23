package gov.nysenate.sage.controller.job;

import gov.nysenate.sage.client.response.job.JobStatusResponse;
import gov.nysenate.sage.dao.model.job.SqlJobProcessDao;
import gov.nysenate.sage.model.job.JobProcessStatus;
import gov.nysenate.sage.model.job.JobUser;
import gov.nysenate.sage.model.result.JobErrorResult;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import static gov.nysenate.sage.util.controller.JobControllerUtil.getJobUser;
import static gov.nysenate.sage.util.controller.JobControllerUtil.setJobResponse;

/**
 * This controller provides an API for accessing the status of a batch job request.
 */
@Controller
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
     * Get Job Process Api
     * ---------------------
     * Get process information for a given process
     * Usage:
     * (GET)    /job/status/process/{Process Id}
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param processId int
     *
     */
    @RequestMapping(value = "/process/{processId}", method = RequestMethod.GET)
    public void jobProcess(HttpServletRequest request, HttpServletResponse response,
                                     @PathVariable int processId) {
        Object statusResponse = new JobErrorResult("Failed to process request!");
        Subject subject = SecurityUtils.getSubject();
        if (subject.hasRole("JOB_USER")) {
            boolean running = isProcessorRunning();
            statusResponse = new JobStatusResponse(sqlJobProcessDao.getJobProcessStatus(processId), running);
        }
        setJobResponse(statusResponse, response);
    }

    /**
     * Running Job Processes Api
     * ---------------------
     * Get all running jobs
     * Usage:
     * (GET)    /job/status/running
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     *
     */
    @RequestMapping(value = "/running", method = RequestMethod.GET)
    public void jobRunning(HttpServletRequest request, HttpServletResponse response) {
        Object statusResponse = new JobErrorResult("Failed to process request!");
        Subject subject = SecurityUtils.getSubject();
        if (subject.hasRole("JOB_USER")) {
            JobUser jobUser = getJobUser(request);
            statusResponse = new JobStatusResponse(
                    sqlJobProcessDao.getJobStatusesByCondition(JobProcessStatus.Condition.RUNNING, jobUser),
                    isProcessorRunning());
        }
        setJobResponse(statusResponse, response);
    }

    /**
     * Active Job Processes Api
     * ---------------------
     * Get all active jobs
     * Usage:
     * (GET)    /job/status/active
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     *
     */
    @RequestMapping(value = "/active", method = RequestMethod.GET)
    public void jobActive(HttpServletRequest request, HttpServletResponse response) {
        Object statusResponse = new JobErrorResult("Failed to process request!");
        Subject subject = SecurityUtils.getSubject();
        if (subject.hasRole("JOB_USER")) {
            JobUser jobUser = getJobUser(request);
            boolean running = isProcessorRunning();
            statusResponse = new JobStatusResponse(sqlJobProcessDao.getActiveJobStatuses(jobUser), running);
        }
        setJobResponse(statusResponse, response);
    }

    /**
     * Inactive Job Processes Api
     * ---------------------
     * Get all inactive jobs
     * Usage:
     * (GET)    /job/status/inactive
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     *
     */
    @RequestMapping(value = "/inactive", method = RequestMethod.GET)
    public void jobInactive(HttpServletRequest request, HttpServletResponse response) {
        Object statusResponse = new JobErrorResult("Failed to process request!");
        Subject subject = SecurityUtils.getSubject();
        if (subject.hasRole("JOB_USER")) {
            JobUser jobUser = getJobUser(request);
            boolean running = isProcessorRunning();
            statusResponse = new JobStatusResponse(sqlJobProcessDao.getInactiveJobStatuses(jobUser), running);
        }
        setJobResponse(statusResponse, response);
    }

    /**
     * Completed Job Processes Api
     * ---------------------
     * Get processes that completed successfully within the past day
     * Usage:
     * (GET)    /job/status/completed
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     *
     */
    @RequestMapping(value = "/completed", method = RequestMethod.GET)
    public void jobCompleted(HttpServletRequest request, HttpServletResponse response) {
        Object statusResponse = new JobErrorResult("Failed to process request!");
        Subject subject = SecurityUtils.getSubject();
        if (subject.hasRole("JOB_USER")) {
            JobUser jobUser = getJobUser(request);
            boolean running = isProcessorRunning();
            statusResponse = new JobStatusResponse(getRecentlyCompletedJobProcesses(jobUser), running);
        }
        setJobResponse(statusResponse, response);
    }

    /**
     * Processor Api
     * ---------------------
     * Get the current status of the processor (Is it running or not)
     * Usage:
     * (GET)    /job/status/processor
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     *
     */
    @RequestMapping(value = "/processor", method = RequestMethod.GET)
    public void jobProcessor(HttpServletRequest request, HttpServletResponse response) {
        Object statusResponse = new JobErrorResult("Failed to process request!");
        Subject subject = SecurityUtils.getSubject();
        if (subject.hasRole("JOB_USER")) {
            statusResponse = isProcessorRunning();
        }
        setJobResponse(statusResponse, response);
    }

    /**
     * All Job Processes Api
     * ---------------------
     * Get all processes (basically a job history)
     * Usage:
     * (GET)    /job/status/all
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     *
     */
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public void jobAll(HttpServletRequest request, HttpServletResponse response) {
        Object statusResponse = new JobErrorResult("Failed to process request!");
        Subject subject = SecurityUtils.getSubject();
        if (subject.hasRole("JOB_USER")) {
            JobUser jobUser = getJobUser(request);
            boolean running = isProcessorRunning();
            List<JobProcessStatus> statuses = sqlJobProcessDao.getJobStatusesByConditions(
                    List.of(JobProcessStatus.Condition.values()),
                    jobUser, null, null);
            statusResponse = new JobStatusResponse(statuses, running);
        }
        setJobResponse(statusResponse, response);
    }

    private List<JobProcessStatus> getRecentlyCompletedJobProcesses(JobUser jobUser) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        Timestamp yesterday = new Timestamp(calendar.getTimeInMillis());

        return sqlJobProcessDao.getRecentlyCompletedJobStatuses(JobProcessStatus.Condition.COMPLETED, jobUser, yesterday);
    }

    private boolean isProcessorRunning() {
        return new File(TEMP_DIR, LOCK_FILENAME).exists();
    }
}