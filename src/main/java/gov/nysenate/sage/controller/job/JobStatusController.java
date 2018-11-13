package gov.nysenate.sage.controller.job;

import gov.nysenate.sage.client.response.job.JobStatusResponse;
import gov.nysenate.sage.dao.model.JobProcessDao;
import gov.nysenate.sage.model.job.JobProcessStatus;
import gov.nysenate.sage.model.job.JobUser;
import gov.nysenate.sage.model.result.JobErrorResult;
import gov.nysenate.sage.util.controller.ConstantUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static gov.nysenate.sage.util.controller.JobControllerUtil.getJobUser;
import static gov.nysenate.sage.util.controller.JobControllerUtil.isAuthenticated;
import static gov.nysenate.sage.util.controller.JobControllerUtil.setJobResponse;

/**
 * This controller provides an API for accessing the status of a batch job request.
 * The available calls are as follows:
 *
 *    /job/status/process/{Process Id}  - Get process information for a given process
 *    /job/status/active                - Get all active processes
 *    /job/status/completed             - Get processes that completed successfully within the past day
 *    /job/status/inactive              - Get all inactive processes
 *    /job/status/all                   - Get all processes (basically a job history)
 */
@Controller
@RequestMapping(value = ConstantUtil.REST_PATH + "job/status")
public class JobStatusController
{
    private static Logger logger = LoggerFactory.getLogger(JobStatusController.class);
    private static JobProcessDao jobProcessDao;
    private static String TEMP_DIR = "/tmp";
    private static String LOCK_FILENAME = "batchJobProcess.lock";

    @Autowired
    public JobStatusController(JobProcessDao jobProcessDao) {
        this.jobProcessDao = jobProcessDao;
    }

    @RequestMapping(value = "/process/{processId}", method = RequestMethod.GET)
    public void jobProcess(HttpServletRequest request, HttpServletResponse response,
                                     @PathVariable int processId) {
        Object statusResponse = new JobErrorResult("Failed to process request!");
        if (isAuthenticated(request)) {
            JobUser jobUser = getJobUser(request);
            boolean running = isProcessorRunning();
            statusResponse = new JobStatusResponse(getJobProcessStatusById(processId, jobUser), running);
        }
        setJobResponse(statusResponse, response);
    }

    @RequestMapping(value = "/running", method = RequestMethod.GET)
    public void jobRunning(HttpServletRequest request, HttpServletResponse response) {
        Object statusResponse = new JobErrorResult("Failed to process request!");
        if (isAuthenticated(request)) {
            JobUser jobUser = getJobUser(request);
            boolean running = isProcessorRunning();
            statusResponse = new JobStatusResponse(getRunningJobProcesses(jobUser), running);
        }
        setJobResponse(statusResponse, response);
    }

    @RequestMapping(value = "/active", method = RequestMethod.GET)
    public void jobActive(HttpServletRequest request, HttpServletResponse response) {
        Object statusResponse = new JobErrorResult("Failed to process request!");
        if (isAuthenticated(request)) {
            JobUser jobUser = getJobUser(request);
            boolean running = isProcessorRunning();
            statusResponse = new JobStatusResponse(getActiveJobProcesses(jobUser), running);
        }
        setJobResponse(statusResponse, response);
    }

    @RequestMapping(value = "/inactive", method = RequestMethod.GET)
    public void jobInactive(HttpServletRequest request, HttpServletResponse response) {
        Object statusResponse = new JobErrorResult("Failed to process request!");
        if (isAuthenticated(request)) {
            JobUser jobUser = getJobUser(request);
            boolean running = isProcessorRunning();
            statusResponse = new JobStatusResponse(getInactiveJobProcesses(jobUser), running);
        }
        setJobResponse(statusResponse, response);
    }

    @RequestMapping(value = "/completed", method = RequestMethod.GET)
    public void jobCompleted(HttpServletRequest request, HttpServletResponse response) {
        Object statusResponse = new JobErrorResult("Failed to process request!");
        if (isAuthenticated(request)) {
            JobUser jobUser = getJobUser(request);
            boolean running = isProcessorRunning();
            statusResponse = new JobStatusResponse(getRecentlyCompletedJobProcesses(jobUser), running);
        }
        setJobResponse(statusResponse, response);
    }

    @RequestMapping(value = "/processor", method = RequestMethod.GET)
    public void jobProcessor(HttpServletRequest request, HttpServletResponse response) {
        Object statusResponse = new JobErrorResult("Failed to process request!");
        if (isAuthenticated(request)) {
            statusResponse = isProcessorRunning();
        }
        setJobResponse(statusResponse, response);
    }

    @RequestMapping(value = "/All", method = RequestMethod.GET)
    public void jobAll(HttpServletRequest request, HttpServletResponse response) {
        Object statusResponse = new JobErrorResult("Failed to process request!");
        if (isAuthenticated(request)) {
            JobUser jobUser = getJobUser(request);
            boolean running = isProcessorRunning();
            statusResponse = new JobStatusResponse(getAllJobProcesses(jobUser), running);
        }
        setJobResponse(statusResponse, response);
    }

    private JobProcessStatus getJobProcessStatusById(int processId, JobUser jobUser)
    {
        JobProcessStatus jobProcessStatus = jobProcessDao.getJobProcessStatus(processId);
        return jobProcessStatus;
    }

    private List<JobProcessStatus> getRunningJobProcesses(JobUser jobUser)
    {
        return jobProcessDao.getJobStatusesByCondition(JobProcessStatus.Condition.RUNNING, jobUser);
    }

    private List<JobProcessStatus> getActiveJobProcesses(JobUser jobUser)
    {
        return jobProcessDao.getActiveJobStatuses(jobUser);
    }

    private List<JobProcessStatus> getInactiveJobProcesses(JobUser jobUser)
    {
        return jobProcessDao.getInactiveJobStatuses(jobUser);
    }

    private List<JobProcessStatus> getAllJobProcesses(JobUser jobUser)
    {
        return jobProcessDao.getJobStatusesByConditions(Arrays.asList(JobProcessStatus.Condition.values()), jobUser, null, null);
    }

    private List<JobProcessStatus> getRecentlyCompletedJobProcesses(JobUser jobUser)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        Timestamp yesterday = new Timestamp(calendar.getTimeInMillis());

        return jobProcessDao.getRecentlyCompletedJobStatuses(JobProcessStatus.Condition.COMPLETED, jobUser, yesterday);
    }

    private boolean isProcessorRunning()
    {
        String tempDir = System.getProperty("java.io.tmpdir", "/tmp");
        File lockFile = new File(TEMP_DIR, LOCK_FILENAME);
        return lockFile.exists();
    }
}