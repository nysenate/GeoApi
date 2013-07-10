package gov.nysenate.sage.scripts;

import gov.nysenate.sage.dao.model.JobProcessDao;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.job.*;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.service.district.DistrictServiceProvider;
import gov.nysenate.sage.service.geo.GeocodeServiceProvider;
import gov.nysenate.sage.util.Config;
import gov.nysenate.sage.util.FormatUtil;
import gov.nysenate.sage.util.JobFileUtil;
import gov.nysenate.sage.util.Mailer;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.CsvListWriter;
import org.supercsv.io.ICsvListReader;
import org.supercsv.io.ICsvListWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

import static gov.nysenate.sage.model.job.JobProcessStatus.Condition.*;

/**
 * Performs batch processing of uploaded jobs.
 */
public class ProcessBatchJobs
{
    private static Config config;
    private static String UPLOAD_DIR;
    private static String DOWNLOAD_DIR;
    private static String USER_DOWNLOAD_DIR;
    private static Integer GEOCODE_THREAD_COUNT;
    private static Integer DISTRICT_THREAD_COUNT;
    private static Integer JOB_BATCH_SIZE;
    private static String LOCK_FILENAME = "batchJobProcess.lock";
    private static Boolean SEND_EMAILS = false;

    public static Logger logger = Logger.getLogger(ProcessBatchJobs.class);
    public static Mailer mailer;
    public static GeocodeServiceProvider geocodeProvider;
    public static DistrictServiceProvider districtProvider;
    public static JobProcessDao jobProcessDao;

    public ProcessBatchJobs()
    {
        config = ApplicationFactory.getConfig();
        UPLOAD_DIR = config.getValue("job.upload.dir");
        DOWNLOAD_DIR = config.getValue("job.download.dir");
        USER_DOWNLOAD_DIR = config.getValue("job.user.download.dir");
        GEOCODE_THREAD_COUNT = Integer.parseInt(config.getValue("job.threads.geocode", "3"));
        DISTRICT_THREAD_COUNT = Integer.parseInt(config.getValue("job.threads.distassign", "3"));
        JOB_BATCH_SIZE = Integer.parseInt(config.getValue("job.batch.size", "95"));

        mailer = new Mailer();
        geocodeProvider = ApplicationFactory.getGeocodeServiceProvider();
        districtProvider = ApplicationFactory.getDistrictServiceProvider();
        jobProcessDao = new JobProcessDao();
    }

    /** Entry point for cron job */
    public static void main(String[] args) throws Exception
    {
        /** Ensure another job process is not running */
        checkLockFile();

        /** Bootstrap the application */
        ApplicationFactory.bootstrap();
        ProcessBatchJobs processBatchJobs = new ProcessBatchJobs();

        if (args.length > 1) {
            switch (args[1]) {
                case "clean" : {
                    processBatchJobs.cancelRunningJobs();
                    break;
                }
                case "process" : {
                    List<JobProcessStatus> jobs = processBatchJobs.getWaitingJobProcesses();
                    logger.info(jobs.size() + " batch jobs have been queued for processing.");
                    for (JobProcessStatus job : jobs){
                        logger.info("Processing job process id " + job.getProcessId());
                        processBatchJobs.processJob(job);
                    }
                    break;
                }
                default : {
                    logger.error("Unsupported argument. " + args[1] + " Exiting..");
                }
            }
        }

        /** Clean up and exit */
        logger.info("Wrapping things up..");
        ApplicationFactory.close();
        System.exit(0);
    }

    /**
     * If the lock file already exists, then fail. Otherwise, create it and arrange for it to be automatically
     * deleted on exit.
    */
    public static void checkLockFile() throws IOException
    {
        String tempDir = System.getProperty("java.io.tmpdir", "/tmp");
        File lockFile = new File(tempDir, LOCK_FILENAME);
        boolean rc = lockFile.createNewFile();
        if (rc == true) {
            lockFile.deleteOnExit();
        }
        else {
            logger.error("Lock file [" + lockFile.getAbsolutePath() + "] already exists; exiting immediately");
            System.exit(1);
        }
    }

    /**
     * Retrieves all job processes that are waiting to be picked up.
     * @return List<JobProcessStatus>
     */
    public List<JobProcessStatus> getWaitingJobProcesses()
    {
        JobProcessDao jobProcessDao = new JobProcessDao();
        List<JobProcessStatus> jobs = jobProcessDao.getJobStatusesByCondition(WAITING_FOR_CRON, null);
        return jobs;
    }

    /**
     *
     * @param jobStatus
     */
    public void processJob(JobProcessStatus jobStatus)
    {
        JobProcess jobProcess = jobStatus.getJobProcess();
        String fileName = jobProcess.getFileName();

        ExecutorService geocodeExecutor = Executors.newFixedThreadPool(GEOCODE_THREAD_COUNT);
        ExecutorService districtExecutor = Executors.newFixedThreadPool(DISTRICT_THREAD_COUNT);
        ICsvListReader jobReader = null;
        ICsvListWriter jobWriter = null;

        try {
            /** Initialize file readers and writers */
            File uploadedFile = new File(UPLOAD_DIR + fileName);
            File targetFile = new File(DOWNLOAD_DIR, fileName);

            /** Determine the type of formatting (tab, comma, semi-colon) */
            CsvPreference preference = JobFileUtil.getCsvPreference(uploadedFile);

            FileReader fileReader = new FileReader(uploadedFile);
            jobReader = new CsvListReader(fileReader, preference);

            FileWriter fileWriter = new FileWriter(targetFile);
            jobWriter = new CsvListWriter(fileWriter, preference);

            String[] header = jobReader.getHeader(true);
            jobWriter.writeHeader(header);

            /** Create the job file and analyze the header columns */
            JobFile jobFile = new JobFile();
            header = jobFile.processHeader(header);

            logger.info("--------------------------------------------------------------------");
            logger.info("Starting Batch Job");
            logger.info("Job Header: " + FormatUtil.toJsonString(header));

            /** Set the job status to running and record the start time */
            jobStatus.setCondition(RUNNING);
            jobStatus.setStartTime(new Timestamp(new Date().getTime()));
            jobProcessDao.setJobProcessStatus(jobStatus);

            /** Check if file can be skipped */
            if (!jobFile.requiresGeocode() && !jobFile.requiresDistrictAssign()) {
                logger.warn("Warning: Skipping job file - No geocode or dist assign columns!");
                jobStatus.setCondition(SKIPPED);
                jobStatus.setCompleteTime(new Timestamp(new Date().getTime()));
                jobProcessDao.setJobProcessStatus(jobStatus);
            }
            else {
                /** Read records into a JobFile */
                final CellProcessor[] processors = jobFile.getProcessors().toArray(new CellProcessor[0]);
                List<Object> row;
                while( (row = jobReader.read(processors)) != null ) {
                    jobFile.addRecord(new JobRecord(jobFile, row));
                }
                logger.info(jobFile.getRecords().size() + " records");
                logger.info("--------------------------------------------------------------------");

                ArrayList<Future<JobBatch>> jobResults = new ArrayList<>();
                List<DistrictType> districtTypes = jobFile.getRequiredDistrictTypes();

                int recordCount = jobFile.recordCount();
                int batchCount =  (recordCount + JOB_BATCH_SIZE - 1) / JOB_BATCH_SIZE; // Allows us to round up
                logger.info("Dividing job into " + batchCount + " batches");

                for (int i = 0; i < batchCount; i++) {
                    int from = (i * JOB_BATCH_SIZE);
                    int to = (from + JOB_BATCH_SIZE < recordCount) ? (from + JOB_BATCH_SIZE) : recordCount;
                    ArrayList<JobRecord> batchRecords = new ArrayList<>(jobFile.getRecords().subList(from, to));
                    JobBatch jobBatch = new JobBatch(batchRecords, from, to);

                    Future<JobBatch> futureGeocodedJobBatch = geocodeExecutor.submit(new GeocodeJobBatch(jobBatch));
                    if (jobFile.requiresDistrictAssign()) {
                         Future<JobBatch> futureDistrictedJobBatch = districtExecutor.submit(new DistrictJobBatch(futureGeocodedJobBatch, districtTypes));
                         jobResults.add(futureDistrictedJobBatch);
                    }
                    else {
                        jobResults.add(futureGeocodedJobBatch);
                    }
                }

                boolean interrupted = false;
                for (int i = 0; i < batchCount; i++) {
                    try {
                        logger.info("Waiting on batch # " + i);
                        JobBatch batch = jobResults.get(i).get();
                        for (JobRecord record : batch.getJobRecords()) {
                            jobWriter.write(record.getRow(), processors);
                        }
                        jobWriter.flush(); // Ensure records have been written

                        /** Determine if this job process has been cancelled by the user */
                        jobStatus = jobProcessDao.getJobProcessStatus(jobProcess.getId());
                        logger.debug("Latest Status: " + FormatUtil.toJsonString(jobStatus));
                        if (jobStatus.getCondition().equals(CANCELLED)) {
                            logger.warn("Job process has been cancelled by the user!");
                            interrupted = true;
                            break;
                        }

                        jobStatus.setCompletedRecords(jobStatus.getCompletedRecords() + batch.getJobRecords().size());
                        jobProcessDao.setJobProcessStatus(jobStatus);
                        logger.info("Wrote results of batch # " + i);
                    }
                    catch (Exception e) {
                        logger.error(e);
                        e.getCause().printStackTrace();
                    }
                }

                if (!interrupted) {
                    jobStatus.setCompleted(true);
                    jobStatus.setCompleteTime(new Timestamp(new Date().getTime()));
                    jobStatus.setCondition(COMPLETED);
                    jobProcessDao.setJobProcessStatus(jobStatus);
                }

                if (SEND_EMAILS) {
                    logger.info("--------------------------------------------------------------------");
                    logger.info("Sending email confirmation                                         |");
                    logger.info("--------------------------------------------------------------------");

                    sendSuccessMail(jobStatus);

                    logger.info("--------------------------------------------------------------------");
                    logger.info("Completed batch processing for job file!                           |");
                    logger.info("--------------------------------------------------------------------");
                }
            }
        }
        catch (FileNotFoundException ex) {
            logger.error("Job process " + jobProcess.getId() + "'s file could not be found!");
            setJobStatusError(jobStatus, SKIPPED, "Could not open file!");
            if (SEND_EMAILS) sendErrorMail(jobStatus, ex);
        }
        catch (IOException ex){
            logger.error(ex);
            setJobStatusError(jobStatus, SKIPPED, "IO Error! " + ex.getMessage());
            if (SEND_EMAILS) sendErrorMail(jobStatus, ex);
        }
        catch (InterruptedException ex) {
            logger.error(ex);
            setJobStatusError(jobStatus, SKIPPED, "Job Interrupted! " + ex.getMessage());
            if (SEND_EMAILS) sendErrorMail(jobStatus, ex);
        }
        catch (ExecutionException ex) {
            logger.error(ex);
            setJobStatusError(jobStatus, SKIPPED, "Execution Error! " + ex.getMessage());
            if (SEND_EMAILS) sendErrorMail(jobStatus, ex);
        }
        catch (Exception ex) {
            logger.fatal("Unknown exception occurred!", ex);
            setJobStatusError(jobStatus, SKIPPED, "Fatal Error! " + ex.getMessage());
            if (SEND_EMAILS) sendErrorMail(jobStatus, ex);
        }
        finally {
            IOUtils.closeQuietly(jobReader);
            IOUtils.closeQuietly(jobWriter);
            geocodeExecutor.shutdownNow();
            districtExecutor.shutdownNow();
            logger.info("Closed resources.");
        }
        return;
    }

    private void setJobStatusError(JobProcessStatus jobStatus, JobProcessStatus.Condition condition, String message)
    {
        if (jobStatus != null) {
            jobStatus.setCondition(condition);
            jobStatus.setMessages(Arrays.asList(message));
            jobProcessDao.setJobProcessStatus(jobStatus);
        }
    }

    public static class GeocodeJobBatch implements Callable<JobBatch>
    {
        private JobBatch jobBatch;
        public GeocodeJobBatch(JobBatch jobBatch) {
            this.jobBatch = jobBatch;
        }

        @Override
        public JobBatch call() throws Exception
        {
            List<GeocodeResult> geocodeResults = geocodeProvider.geocode(jobBatch.getAddresses());
            if (geocodeResults.size() == jobBatch.getJobRecords().size()) {
                for (int i = 0; i < geocodeResults.size(); i++) {
                    jobBatch.setGeocodeResult(i, geocodeResults.get(i));
                }
            }
            return this.jobBatch;
        }
    }

    public static class DistrictJobBatch implements Callable<JobBatch>
    {
        private Future<JobBatch> futureJobBatch;
        private List<DistrictType> districtTypes;
        public DistrictJobBatch(Future<JobBatch> futureJobBatch, List<DistrictType> types) throws InterruptedException,
                                                                                                  ExecutionException
        {
            this.futureJobBatch = futureJobBatch;
            this.districtTypes = types;
        }

        @Override
        public JobBatch call() throws Exception
        {
            JobBatch jobBatch = futureJobBatch.get();
            logger.info("District assignment for records " + jobBatch.getFromRecord() + "-" + jobBatch.getToRecord());
            List<DistrictResult> districtResults = districtProvider.assignDistricts(jobBatch.getGeocodedAddresses(),
                                                                                    this.districtTypes);
            for (int i = 0; i < districtResults.size(); i++) {
                jobBatch.setDistrictResult(i, districtResults.get(i));
            }
            return jobBatch;
        }
    }

    /**
     *
     * @param jobStatus
     * @throws Exception
     */
    public void sendSuccessMail(JobProcessStatus jobStatus) throws Exception
    {
        JobProcess jobProcess = jobStatus.getJobProcess();
        JobUser jobUser = jobProcess.getRequestor();
        String subject = "SAGE Batch Job #" + jobProcess.getId() + " Completed";

        String message = String.format("Your request on %s has been completed and can be downloaded <a href='%s'>here</a>." +
                                       "<br/>This is an automated message.", jobProcess.getRequestTime().toString(),
                                        USER_DOWNLOAD_DIR + jobProcess.getFileName());

        String adminMessage = String.format("Request by %s on %s has been completed and can be downloaded <a href='%s'>here</a>." +
                                        "<br/>This is an automated message.", jobUser.getEmail(),
                                        jobProcess.getRequestTime().toString(), USER_DOWNLOAD_DIR + jobProcess.getFileName());

        logger.info("Sending email to " + jobUser.getEmail());
        mailer.sendMail(jobUser.getEmail(), subject, message);
        logger.info("Sending email to " + mailer.getAdminEmail());
        mailer.sendMail(mailer.getAdminEmail(), subject, adminMessage);
    }

    /**
     *
     * @param jobStatus
     * @throws Exception
     */
    public void sendErrorMail(JobProcessStatus jobStatus, Exception ex)
    {
        JobProcess jobProcess = jobStatus.getJobProcess();
        JobUser jobUser = jobProcess.getRequestor();
        String subject = "SAGE Batch Job #" + jobProcess.getId() + " Failed";

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);

        String message = String.format("Your request on %s has encountered a fatal error and has been been skipped. The administrator has been notified." +
                "<br/>This is an automated message.", jobProcess.getRequestTime().toString());

        String adminMessage = String.format("Request by %s on %s has encountered a fatal error during processing:" +
                "<br/><br/>Exception:<br/><pre>%s</pre><br/>Request:<br/><pre>%s</pre><br/>This is an automated message.",
                jobUser.getEmail(), jobProcess.getRequestTime().toString(), sw.toString(), FormatUtil.toJsonString(jobStatus));

        try {
            logger.info("Sending email to " + jobUser.getEmail());
            mailer.sendMail(jobUser.getEmail(), subject, message);
            logger.info("Sending email to " + mailer.getAdminEmail());
            mailer.sendMail(mailer.getAdminEmail(), subject, adminMessage);
        }
        catch (Exception ex2) { logger.fatal("Failed to send error email.. sheesh", ex2); }
    }

    /**
     *
     */
    public void cancelRunningJobs()
    {
        logger.info("Cancelling all running jobs!");
        List<JobProcessStatus> jobStatuses = jobProcessDao.getJobStatusesByCondition(RUNNING, null);
        for (JobProcessStatus jobStatus : jobStatuses) {
            jobStatus.setCondition(CANCELLED);
            jobStatus.setMessages(Arrays.asList("Cancelled during cleanup."));
            jobStatus.setCompleteTime(new Timestamp(new Date().getTime()));
            jobProcessDao.setJobProcessStatus(jobStatus);
        }
    }
}