package gov.nysenate.sage.controller.job;

import gov.nysenate.sage.client.response.job.JobStatusResponse;
import gov.nysenate.sage.dao.model.JobProcessDao;
import gov.nysenate.sage.model.job.JobProcessStatus;
import gov.nysenate.sage.model.job.JobUser;
import gov.nysenate.sage.model.result.JobErrorResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;

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
public class JobStatusController extends BaseJobController
{
    private static Logger logger = LogManager.getLogger(JobStatusController.class);
    private static JobProcessDao jobProcessDao = new JobProcessDao();
    private static String TEMP_DIR = "/tmp";
    private static String LOCK_FILENAME = "batchJobProcess.lock";

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        doGet(request, response);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        Object statusResponse = new JobErrorResult("Failed to process request!");
        if (isAuthenticated(request)) {
            String pathInfo[] = request.getPathInfo().replaceFirst("^/", "").split("/");
            List<String> args = new ArrayList<>(Arrays.asList(pathInfo));
            String method = (args.size() > 0) ? args.get(0) : "";
            JobUser jobUser = getJobUser(request);

            boolean running = isProcessorRunning();

            if (method != null) {
                switch (method) {
                    case "process" : {
                        if (args.size() == 2) {
                            try {
                                int processId = Integer.parseInt(args.get(1));
                                statusResponse = new JobStatusResponse(getJobProcessStatusById(processId, jobUser), running);
                            }
                            catch (NumberFormatException ex) {
                                statusResponse = new JobStatusResponse(new JobErrorResult("Process id must be an integer!"));
                            }
                        }
                        else {
                            statusResponse = new JobStatusResponse(new JobErrorResult("Specify the process id!"));
                        }
                        break;
                    }
                    case "running" : {
                        statusResponse = new JobStatusResponse(getRunningJobProcesses(jobUser), running);
                        break;
                    }
                    case "active" : {
                        statusResponse = new JobStatusResponse(getActiveJobProcesses(jobUser), running);
                        break;
                    }
                    case "inactive" : {
                        statusResponse = new JobStatusResponse(getInactiveJobProcesses(jobUser), running);
                        break;
                    }
                    case "completed" : {
                        statusResponse = new JobStatusResponse(getRecentlyCompletedJobProcesses(jobUser), running);
                        break;
                    }
                    case "processor" : {
                        statusResponse = isProcessorRunning();
                        break;
                    }
                    case "all" : {
                        statusResponse = new JobStatusResponse(getAllJobProcesses(jobUser), running);
                        break;
                    }
                }
            }
        }
        else {
            setJobResponse(new JobStatusResponse(new JobErrorResult("You must be logged in to access job status.")), response);
        }
        setJobResponse(statusResponse, response);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {}

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