package gov.nysenate.sage.dao.model;

import gov.nysenate.sage.TestBase;
import gov.nysenate.sage.model.job.JobProcess;
import gov.nysenate.sage.model.job.JobProcessStatus;
import gov.nysenate.sage.model.job.JobUser;
import gov.nysenate.sage.util.FormatUtil;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class JobProcessDaoTest extends TestBase
{
    private JobProcessDao jpDao = new JobProcessDao();
    private JobUserDao jobUserDao = new JobUserDao();
    private JobUser jobUser = jobUserDao.getJobUserByEmail("ashislam858@gmail.com");

    @Test
    public void addProcessTest()
    {
        JobProcess jobProcess = new JobProcess();
        jobProcess.setRequestor(jobUser);
        jobProcess.setFileName("testfile.tsv");
        jobProcess.setRecordCount(1000);
        jobProcess.setFileType("GeocodeTest");

        int res = jpDao.addJobProcess(jobProcess);
        FormatUtil.printObject(res);
    }

    @Test
    public void getProcessByIdTest()
    {
        JobProcess jobProcess = jpDao.getJobProcessById(1);
        assertNotNull(jobProcess);
        assertEquals(1, jobProcess.getId());
        assertEquals("ashislam858@gmail.com", jobProcess.getRequestor().getEmail());
        assertEquals(1000, jobProcess.getRecordCount());
        FormatUtil.printObject(jobProcess);
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
        FormatUtil.printObject(jpDao.getJobProcessStatus(2));
    }

    @Test
    public void getStatusTest()
    {
        JobProcessStatus jsp = jpDao.getJobProcessStatus(76);
        FormatUtil.printObject(jsp);
    }

    @Test
    public void getJobProcessesByStatusTest()
    {
        FormatUtil.printObject(jpDao.getJobStatusesByCondition(JobProcessStatus.Condition.WAITING_FOR_CRON, null));
    }

    @Test
    public void getJobStatusesByConditions()
    {
        FormatUtil.printObject(jpDao.getJobStatusesByConditions(Arrays.asList(JobProcessStatus.Condition.CANCELLED,
                                         JobProcessStatus.Condition.WAITING_FOR_CRON)));
    }

    @Test
    public void getActiveJobStatuses()
    {
        FormatUtil.printObject(jpDao.getActiveJobStatuses());
    }

    @Test
    public void getInactiveJobStatuses()
    {
        FormatUtil.printObject(jpDao.getInactiveJobStatuses());
    }

    @Test
    public void getRecentlyCompletedJobStatuses()
    {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        Timestamp yesterday = new Timestamp(calendar.getTimeInMillis());

        FormatUtil.printObject(jpDao.getRecentlyCompletedJobStatuses(JobProcessStatus.Condition.COMPLETED, null, yesterday));
    }


}
