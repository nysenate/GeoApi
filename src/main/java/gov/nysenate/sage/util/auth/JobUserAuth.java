package gov.nysenate.sage.util.auth;

import gov.nysenate.sage.dao.model.JobUserDao;
import gov.nysenate.sage.model.job.JobUser;
import org.apache.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;

public class JobUserAuth
{
    private Logger logger = Logger.getLogger(JobUserAuth.class);
    private JobUserDao jobUserDao;

    public JobUserAuth()
    {
        jobUserDao = new JobUserDao();
    }

    /**
     * Retrieves the JobUser that matches the given email and password.
     * Utilizes bcrypt hashes to verify password.
     * @param email
     * @param password
     * @return  JobUser if found, null otherwise
     */
    public JobUser getJobUser(String email, String password)
    {
        JobUser jobUser = jobUserDao.getJobUserByEmail(email);
        if (jobUser != null) {
            /** Simple password validation */
            if (BCrypt.checkpw(password, jobUser.getPassword())) {
                logger.info("Job user " + email + " verified credentials.");
                return jobUser;
            }
        }
        logger.info("User " + email + " failed to verify credentials.");
        return null;
    }

    public JobUser addActiveJobUser(String email, String password, String firstname, String lastname)
    {
        JobUser jobUser = new JobUser(email, BCrypt.hashpw(password, BCrypt.gensalt()), firstname, lastname);
        jobUser.setActive(true);

        int status = jobUserDao.addJobUser(jobUser);
        if (status == 1) {
            logger.info("Added new job user: " + jobUser.getEmail());
            return jobUserDao.getJobUserByEmail(email);
        }
        return null;
    }
}
