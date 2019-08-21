package gov.nysenate.sage.service.job;

import gov.nysenate.sage.model.job.JobBatch;
import gov.nysenate.sage.model.job.JobProcessStatus;

import java.io.IOException;
import java.util.List;

public interface JobProcessor {

    /**
     * Processing the actual job process
     * @param args
     * @throws Exception
     */
    public void run(String[] args) throws Exception;

    /**
     * Kicks off the job processor at the cron specified in app properties
     * @throws Exception
     */
    public void jobCron() throws Exception;

    /**
     * Retrieves all job processes that are waiting to be picked up.
     * @return List<JobProcessStatus>
     */
    public List<JobProcessStatus> getWaitingJobProcesses();

    /**
     * Retrieves jobs that are still running and need to be finished.
     * @return List<JobProcessStatus>
     */
    public List<JobProcessStatus> getRunningJobProcesses();

    /**
     * Main routine for processing a JobProcess.
     * @param jobStatus
     */
    public void processJob(JobProcessStatus jobStatus) throws Exception;

    /**
     * A callable for the executor to perform address validation for a JobBatch.
     */
    public JobBatch validateJobBatch(JobBatch jobBatch) throws Exception;

    /**
     * Sends an email to the JobProcess's submitter and the admin indicating that the job has completed successfully.
     * @param jobStatus
     * @throws Exception
     */
    public void sendSuccessMail(JobProcessStatus jobStatus) throws Exception;

    /**
     * Sends an email to the JobProcess's submitter and the admin indicating that the job has encountered an error.
     * @param jobStatus
     * @throws Exception
     */
    public void sendErrorMail(JobProcessStatus jobStatus, Exception ex);

    /**
     * Marks all running jobs as cancelled effectively removing them from the queue.
     */
    public void cancelRunningJobs();




}
