package gov.nysenate.sage.dao.model;

import gov.nysenate.sage.TestBase;
import gov.nysenate.sage.model.job.JobProcess;
import gov.nysenate.sage.model.job.JobProcessStatus;
import gov.nysenate.sage.model.job.JobUser;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class JobProcessDaoTest extends TestBase
{
    private JobProcessDao jpDao = new JobProcessDao();
    private JobUserDao jobUserDao = new JobUserDao();
    private JobUser jobUser = jobUserDao.getJobUserByEmail("zalewski@nysenate.gov");

    @Test
    public void addProcessTest()
    {
        JobProcess jobProcess = new JobProcess();
        jobProcess.setRequestor(jobUser);
        jobProcess.setFileName("testfile.tsv");
        jobProcess.setRecordCount(1000);
        jobProcess.setFileType("GeocodeTest");

        int res = jpDao.addJobProcess(jobProcess);
        assertNotNull(res);
    }

    @Test
    public void getProcessByIdTest()
    {
        JobProcess jobProcess = jpDao.getJobProcessById(1);
        assertNotNull(jobProcess);
        assertEquals(1, jobProcess.getId());
        assertEquals("neison@nysenate.gov", jobProcess.getRequestor().getEmail());
        assertEquals(19, jobProcess.getRecordCount());
    }

    @Test
    public void setProcessStatusTest()
    {
        JobProcessStatus jsp = new JobProcessStatus();
        jsp.setCompleted(false);
        jsp.setCompletedRecords(1600);
        jsp.setProcessId(2);
        jsp.setCondition(JobProcessStatus.Condition.CANCELLED);
        jsp.setMessages(Arrays.asList("Missed geocode here", "Also another error here!"));
        jsp.setStartTime(new Timestamp(new Date().getTime()));

        jpDao.setJobProcessStatus(jsp);
        jpDao.getJobProcessStatus(2);
    }

    @Test
    public void getStatusTest()
    {
        JobProcessStatus jsp = jpDao.getJobProcessStatus(76);
        assertNotNull(jsp);
    }

    @Test
    public void getJobProcessesByStatusTest()
    {
        jpDao.getJobStatusesByCondition(JobProcessStatus.Condition.WAITING_FOR_CRON, null);
    }

    @Test
    public void getJobStatusesByConditions()
    {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        assertNotNull(jpDao.getJobStatusesByConditions(new ArrayList<JobProcessStatus.Condition>(), jobUser,
                new Timestamp(cal.getTimeInMillis()), null));
    }

    @Test
    public void getActiveJobStatuses()
    {
        assertNotNull(jpDao.getActiveJobStatuses(jobUser));
    }

    @Test
    public void getInactiveJobStatuses()
    {
        assertNotNull(jpDao.getInactiveJobStatuses(jobUser));
    }

    @Test
    public void getRecentlyCompletedJobStatuses()
    {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        Timestamp yesterday = new Timestamp(calendar.getTimeInMillis());
        assertNotNull(jpDao.getRecentlyCompletedJobStatuses(JobProcessStatus.Condition.COMPLETED,
                null, yesterday));

    }


}
