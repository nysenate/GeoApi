package gov.nysenate.sage.service.job;

public interface JobProcessor {
    /**
     * Processing the actual job process
     */
    void run() throws Exception;

    /**
     * Marks all running jobs as cancelled effectively removing them from the queue.
     */
    void cancelRunningJobs();
}
