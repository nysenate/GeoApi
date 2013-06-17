package gov.nysenate.sage.controller.job;

import gov.nysenate.sage.client.response.job.JobStatusResponse;
import gov.nysenate.sage.dao.model.JobProcessDao;
import gov.nysenate.sage.model.job.JobProcessStatus;
import gov.nysenate.sage.model.result.JobErrorResult;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
public class JobStatusController extends BaseJobController
{
    private static JobProcessDao jobProcessDao = new JobProcessDao();

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

            if (method != null) {
                switch (method) {
                    case "process" : {
                        if (args.size() == 2) {
                            try {
                                int processId = Integer.parseInt(args.get(1));
                                statusResponse = new JobStatusResponse(getJobProcessStatusById(processId));
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
                        statusResponse = new JobStatusResponse(getRunningJobProcesses());
                        break;
                    }
                    case "active" : {
                        statusResponse = new JobStatusResponse(getActiveJobProcesses());
                        break;
                    }
                    case "inactive" : {
                        statusResponse = new JobStatusResponse(getInactiveJobProcesses());
                        break;
                    }
                    case "completed" : {
                        statusResponse = new JobStatusResponse(getRecentlyCompletedJobProcesses());
                        break;
                    }
                    case "all" : {
                        statusResponse = new JobStatusResponse(getAllJobProcesses());
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

    private JobProcessStatus getJobProcessStatusById(int processId)
    {
        JobProcessStatus jobProcessStatus = jobProcessDao.getJobProcessStatus(processId);
        return jobProcessStatus;
    }

    private List<JobProcessStatus> getRunningJobProcesses()
    {
        return jobProcessDao.getJobStatusesByCondition(JobProcessStatus.Condition.RUNNING, null);
    }

    private List<JobProcessStatus> getActiveJobProcesses()
    {
        return jobProcessDao.getActiveJobStatuses();
    }

    private List<JobProcessStatus> getInactiveJobProcesses()
    {
        return jobProcessDao.getInactiveJobStatuses();
    }

    private List<JobProcessStatus> getAllJobProcesses()
    {
        return jobProcessDao.getJobStatusesByConditions(Arrays.asList(JobProcessStatus.Condition.values()));
    }

    private List<JobProcessStatus> getRecentlyCompletedJobProcesses()
    {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        Timestamp yesterday = new Timestamp(calendar.getTimeInMillis());

        return jobProcessDao.getRecentlyCompletedJobStatuses(JobProcessStatus.Condition.COMPLETED, null, yesterday);
    }
}