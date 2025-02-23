package gov.nysenate.sage.dao.model.job;

import gov.nysenate.sage.model.job.JobProcess;
import gov.nysenate.sage.model.job.JobProcessStatus;
import gov.nysenate.sage.model.job.JobUser;

import java.sql.Timestamp;
import java.util.List;

public interface JobProcessDao {
    /**
     * Adds a new job process to the database queue.
     * @param p JobProcess to insert
     * @return The id of the inserted job process, or -1 on failure
     */
    int addJobProcess(JobProcess p);

    /**
     * Update or insert a JobProcessStatus. If a job status entry already exists, the record will be updated with the
     * new information. Otherwise a new row will be created.
     * @param jps JobProcessStatus
     * @return int (rows affected) or -1 if failed.
     */
    int setJobProcessStatus(JobProcessStatus jps);

    /**
     * Retrieve JobProcessStatus by processId.
     * @param processId int
     * @return JobProcessStatus
     */
    JobProcessStatus getJobProcessStatus(int processId);

    /**
     * Retrieves a List of JobProcessStatus matching the given Condition with no filtering on requestTime.
     * Delegates to getJobStatusesByConditions(), see method for details.
     * @return List<JobProcessStatus>
     */
    default List<JobProcessStatus> getJobStatusesByCondition(JobProcessStatus.Condition condition, JobUser jobUser) {
        return getJobStatusesByConditions(List.of(condition), jobUser, null, null);
    }

    default List<JobProcessStatus> getJobStatusesByConditions(List<JobProcessStatus.Condition> conditions, JobUser jobUser) {
        return getJobStatusesByConditions(conditions, jobUser, null, null);
    }


    /**
     * Retrieves a List of JobProcessStatus matching the given Condition types.
     * @param conditions List of Condition objects to filter results by.
     * @param jobUser JobUser to retrieve results for. If null or admin user, all results returned.
     * @param start Start requestTime to filter results by. If null, start will be defaulted to 0 UTC.
     * @param end End requestTime to filter results by. If null, end will be defaulted to current time.
     * @return List<JobProcessStatus>
     */
    List<JobProcessStatus> getJobStatusesByConditions(List<JobProcessStatus.Condition> conditions, JobUser jobUser, Timestamp start, Timestamp end);

    /**
     * Gets completed job statuses that finished on or after the 'afterThis' timestamp.
     * This method is different from getJobStatusesByCondition because the completeTime is filtered on
     * as opposed to the requestTime.
     * @param condition Condition to filter by. If null, no filtering will occur on condition.
     * @param jobUser JobUser to retrieve results for. If null or admin user, all results returned.
     * @return List<JobProcessStatus>
     */
    List<JobProcessStatus> getRecentlyCompletedJobStatuses(JobProcessStatus.Condition condition, JobUser jobUser);

    /**
     *  Gets a list of active job processes for a given job user
     * @param jobUser
     * @return
     */
    default List<JobProcessStatus> getActiveJobStatuses(JobUser jobUser) {
        return getJobStatusesByConditions(JobProcessStatus.Condition.getActiveConditions(), jobUser, null, null);
    }
}
