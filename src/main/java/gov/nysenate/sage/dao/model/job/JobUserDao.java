package gov.nysenate.sage.dao.model.job;

import gov.nysenate.sage.model.job.JobUser;

import java.util.List;

public interface JobUserDao {
    /**
     * Get a list of all job users
     */
    List<JobUser> getJobUsers();

    /**
     * Get a single job user by their id
     */
    JobUser getJobUserById(int id);

    /**
     * Get a job user by their email address
     */
    JobUser getJobUserByEmail(String email);

    /**
     * Add a job user who can use SAGE batch services
     */
    int addJobUser(JobUser jobUser);

    /**
     * Remove a job user. They will no longer be able to use sage batch services
     */
    int removeJobUser(JobUser jobUser);
}
