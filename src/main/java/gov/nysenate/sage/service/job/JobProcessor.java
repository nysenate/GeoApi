package gov.nysenate.sage.service.job;

import gov.nysenate.sage.model.job.JobProcessStatus;

import java.util.List;

public interface JobProcessor {
    /**
     * Processing the actual job process
     * @param args
     * @throws Exception
     */
    void run(String[] args) throws Exception;

    /**
     * Retrieves all job processes that are waiting to be picked up.
     * @return List<JobProcessStatus>
     */
    List<JobProcessStatus> getWaitingJobProcesses();

    /**
     * Retrieves jobs that are still running and need to be finished.
     * @return List<JobProcessStatus>
     */
    List<JobProcessStatus> getRunningJobProcesses();

    /**
     * Main routine for processing a JobProcess.
     * @param jobStatus
     */
    void processJob(JobProcessStatus jobStatus) throws Exception;

    /**
     * Sends an email to the JobProcess's submitter and the admin indicating that the job has completed successfully.
     * @param jobStatus
     * @throws Exception
     */
    void sendSuccessMail(JobProcessStatus jobStatus) throws Exception;

    /**
     * Sends an email to the JobProcess's submitter and the admin indicating that the job has encountered an error.
     * @param jobStatus
     * @throws Exception
     */
    void sendErrorMail(JobProcessStatus jobStatus, Exception ex);

    /**
     * Marks all running jobs as cancelled effectively removing them from the queue.
     */
    void cancelRunningJobs();




}
