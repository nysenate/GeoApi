package gov.nysenate.sage.controller.job;

import gov.nysenate.sage.client.response.job.JobStatusResponse;
import gov.nysenate.sage.dao.model.job.SqlJobProcessDao;
import gov.nysenate.sage.model.job.JobProcessStatus;
import gov.nysenate.sage.model.job.JobUser;
import gov.nysenate.sage.model.result.JobErrorResult;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.subject.WebSubject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static gov.nysenate.sage.util.controller.JobControllerUtil.getJobUser;
import static gov.nysenate.sage.util.controller.JobControllerUtil.setJobResponse;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * This controller provides an API for accessing the status of a batch job request.
 */
@RestController
@RequestMapping(value = "/job/status", produces = APPLICATION_JSON_VALUE)
public class JobStatusController
{
    private static Logger logger = LoggerFactory.getLogger(JobStatusController.class);
    private static SqlJobProcessDao sqlJobProcessDao;
    private static String TEMP_DIR = "/tmp";
    private static String LOCK_FILENAME = "batchJobProcess.lock";

    @Autowired
    @Qualifier("securityManager")
    protected DefaultWebSecurityManager securityManager;

    @Autowired
    public JobStatusController(SqlJobProcessDao sqlJobProcessDao) {
        this.sqlJobProcessDao = sqlJobProcessDao;
    }

    /**
     * Get Job Process Api
     * ---------------------
     *
     * Get process information for a given process
     *
     * Usage:
     * (GET)    /job/status/process/{Process Id}
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param processId int
     *
     */
    @RequestMapping(value = "/process/{processId}", method = RequestMethod.GET)
    public Object jobProcess(HttpServletRequest request, HttpServletResponse response,
                                     @PathVariable int processId) {
        Object statusResponse = new JobErrorResult("Failed to process request!");
        WebSubject webSubject = createSubject(request, response);
        if (webSubject.hasRole("JOB_USER")) {
            JobUser jobUser = getJobUser(request);
            boolean running = isProcessorRunning();
            statusResponse = new JobStatusResponse(getJobProcessStatusById(processId, jobUser), running);
        }
        return statusResponse;
    }

    /**
     * Running Job Processes Api
     * ---------------------
     *
     * Get all running jobs
     *
     * Usage:
     * (GET)    /job/status/running
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     *
     */
    @RequestMapping(value = "/running", method = RequestMethod.GET)
    public Object jobRunning(HttpServletRequest request, HttpServletResponse response) {
        Object statusResponse = new JobErrorResult("Failed to process request!");
        WebSubject webSubject = createSubject(request, response);
        if (webSubject.hasRole("JOB_USER")) {
            JobUser jobUser = getJobUser(request);
            boolean running = isProcessorRunning();
            statusResponse = new JobStatusResponse(getRunningJobProcesses(jobUser), running);
        }
        return statusResponse;
    }

    /**
     * Active Job Processes Api
     * ---------------------
     *
     * Get all active jobs
     *
     * Usage:
     * (GET)    /job/status/active
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     *
     */
    @RequestMapping(value = "/active", method = RequestMethod.GET)
    public Object jobActive(HttpServletRequest request, HttpServletResponse response) {
        Object statusResponse = new JobErrorResult("Failed to process request!");
        WebSubject webSubject = createSubject(request, response);
        if (webSubject.hasRole("JOB_USER")) {
            JobUser jobUser = getJobUser(request);
            boolean running = isProcessorRunning();
            statusResponse = new JobStatusResponse(getActiveJobProcesses(jobUser), running);
        }
        return statusResponse;
    }

    /**
     * Inactive Job Processes Api
     * ---------------------
     *
     * Get all inactive jobs
     *
     * Usage:
     * (GET)    /job/status/inactive
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     *
     */
    @RequestMapping(value = "/inactive", method = RequestMethod.GET)
    public Object jobInactive(HttpServletRequest request, HttpServletResponse response) {
        Object statusResponse = new JobErrorResult("Failed to process request!");
        WebSubject webSubject = createSubject(request, response);
        if (webSubject.hasRole("JOB_USER")) {
            JobUser jobUser = getJobUser(request);
            boolean running = isProcessorRunning();
            statusResponse = new JobStatusResponse(getInactiveJobProcesses(jobUser), running);
        }
        return statusResponse;
    }

    /**
     * Completed Job Processes Api
     * ---------------------
     *
     * Get processes that completed successfully within the past day
     *
     * Usage:
     * (GET)    /job/status/completed
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     *
     */
    @RequestMapping(value = "/completed", method = RequestMethod.GET)
    public Object jobCompleted(HttpServletRequest request, HttpServletResponse response) {
        Object statusResponse = new JobErrorResult("Failed to process request!");
        WebSubject webSubject = createSubject(request, response);
        if (webSubject.hasRole("JOB_USER")) {
            JobUser jobUser = getJobUser(request);
            boolean running = isProcessorRunning();
            statusResponse = new JobStatusResponse(getRecentlyCompletedJobProcesses(jobUser), running);
        }
        return statusResponse;
    }

    /**
     * Processor Api
     * ---------------------
     *
     * Get the current status of the processor (Is it running or not)
     *
     * Usage:
     * (GET)    /job/status/processor
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     *
     */
    @RequestMapping(value = "/processor", method = RequestMethod.GET)
    public Object jobProcessor(HttpServletRequest request, HttpServletResponse response) {
        Object statusResponse = new JobErrorResult("Failed to process request!");
        WebSubject webSubject = createSubject(request, response);
        if (webSubject.hasRole("JOB_USER")) {
            statusResponse = isProcessorRunning();
        }
        return statusResponse;
    }

    /**
     * All Job Processes Api
     * ---------------------
     *
     * Get all processes (basically a job history)
     *
     * Usage:
     * (GET)    /job/status/all
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     *
     */
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public Object jobAll(HttpServletRequest request, HttpServletResponse response) {
        Object statusResponse = new JobErrorResult("Failed to process request!");
        WebSubject webSubject = createSubject(request, response);
        if (webSubject.hasRole("JOB_USER")) {
            JobUser jobUser = getJobUser(request);
            boolean running = isProcessorRunning();
            statusResponse = new JobStatusResponse(getAllJobProcesses(jobUser), running);
        }
        return statusResponse;
    }

    private JobProcessStatus getJobProcessStatusById(int processId, JobUser jobUser)
    {
        JobProcessStatus jobProcessStatus = sqlJobProcessDao.getJobProcessStatus(processId);
        return jobProcessStatus;
    }

    private List<JobProcessStatus> getRunningJobProcesses(JobUser jobUser)
    {
        return sqlJobProcessDao.getJobStatusesByCondition(JobProcessStatus.Condition.RUNNING, jobUser);
    }

    private List<JobProcessStatus> getActiveJobProcesses(JobUser jobUser)
    {
        return sqlJobProcessDao.getActiveJobStatuses(jobUser);
    }

    private List<JobProcessStatus> getInactiveJobProcesses(JobUser jobUser)
    {
        return sqlJobProcessDao.getInactiveJobStatuses(jobUser);
    }

    private List<JobProcessStatus> getAllJobProcesses(JobUser jobUser)
    {
        return sqlJobProcessDao.getJobStatusesByConditions(Arrays.asList(JobProcessStatus.Condition.values()), jobUser, null, null);
    }

    private List<JobProcessStatus> getRecentlyCompletedJobProcesses(JobUser jobUser)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        Timestamp yesterday = new Timestamp(calendar.getTimeInMillis());

        return sqlJobProcessDao.getRecentlyCompletedJobStatuses(JobProcessStatus.Condition.COMPLETED, jobUser, yesterday);
    }

    private boolean isProcessorRunning()
    {
        String tempDir = System.getProperty("java.io.tmpdir", "/tmp");
        File lockFile = new File(TEMP_DIR, LOCK_FILENAME);
        return lockFile.exists();
    }

    protected WebSubject createSubject(ServletRequest request, ServletResponse response) {
        return new WebSubject.Builder(securityManager, request, response).buildWebSubject();
    }
}