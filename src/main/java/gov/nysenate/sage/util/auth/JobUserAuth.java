package gov.nysenate.sage.util.auth;

import gov.nysenate.sage.dao.model.job.SqlJobUserDao;
import gov.nysenate.sage.model.job.JobUser;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JobUserAuth {
    private static final Logger logger = LoggerFactory.getLogger(JobUserAuth.class);
    private final SqlJobUserDao sqlJobUserDao;

    @Autowired
    public JobUserAuth(SqlJobUserDao sqlJobUserDao) {
        this.sqlJobUserDao = sqlJobUserDao;
    }

    /**
     * Retrieves the JobUser that matches the given email and password.
     * Utilizes bcrypt hashes to verify password.
     * @return  JobUser if found, null otherwise
     */
    public JobUser getJobUser(String email, String password) {
        JobUser jobUser = sqlJobUserDao.getJobUserByEmail(email);
        if (jobUser != null) {
            if (BCrypt.checkpw(password, jobUser.getPassword())) {
                logger.info("Job user {} verified credentials.", email);
                return jobUser;
            }
        }
        logger.info("User {} failed to verify credentials.", email);
        return null;
    }

    public JobUser getJobUser(String email) {
        return sqlJobUserDao.getJobUserByEmail(email);
    }

    public JobUser addActiveJobUser(String email, String password, String firstname, String lastname, boolean admin) {
        JobUser jobUser = new JobUser(email, BCrypt.hashpw(password, BCrypt.gensalt()), firstname, lastname, admin);
        jobUser.setActive(true);

        int status = sqlJobUserDao.addJobUser(jobUser);
        if (status == 1) {
            logger.info("Added new job user: {}", jobUser.getEmail());
            return sqlJobUserDao.getJobUserByEmail(email);
        }
        return null;
    }
}
