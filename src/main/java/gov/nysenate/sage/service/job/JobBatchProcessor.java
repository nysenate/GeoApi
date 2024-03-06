package gov.nysenate.sage.service.job;

import gov.nysenate.sage.config.ApplicationConfig;
import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.dao.logger.district.SqlDistrictResultLogger;
import gov.nysenate.sage.dao.logger.geocode.SqlGeocodeResultLogger;
import gov.nysenate.sage.dao.model.job.SqlJobProcessDao;
import gov.nysenate.sage.model.api.BatchDistrictRequest;
import gov.nysenate.sage.model.api.BatchGeocodeRequest;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.job.*;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.service.address.AddressServiceProvider;
import gov.nysenate.sage.service.district.DistrictServiceProvider;
import gov.nysenate.sage.service.geo.GeocodeServiceProvider;
import gov.nysenate.sage.util.FileUtil;
import gov.nysenate.sage.util.FormatUtil;
import gov.nysenate.sage.util.Mailer;
import gov.nysenate.sage.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

import javax.annotation.PreDestroy;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedTransferQueue;

import static gov.nysenate.sage.model.job.JobProcessStatus.Condition.*;
import static gov.nysenate.sage.service.district.DistrictServiceProvider.DistrictStrategy;
import static gov.nysenate.sage.util.controller.ConstantUtil.DOWNLOAD_BASE_URL;

@Service
public class JobBatchProcessor implements JobProcessor {

    private static String BASE_URL;
    private static String UPLOAD_DIR;
    private static String DOWNLOAD_DIR;
    private static String DOWNLOAD_URL;
    private static Integer JOB_BATCH_SIZE;
    private static String TEMP_DIR = "/tmp";
    private static String LOCK_FILENAME = "batchJobProcess.lock";
    private static Boolean SEND_EMAILS = true;
    private static Boolean LOGGING_ENABLED = false;
    private static Integer LOGGING_THRESHOLD = 1000;

    public static Logger logger = LoggerFactory.getLogger(JobBatchProcessor.class);
    Marker fatal = MarkerFactory.getMarker("FATAL");
    private Environment env;
    private Mailer mailer;
    private AddressServiceProvider addressProvider;
    private GeocodeServiceProvider geocodeProvider;
    private DistrictServiceProvider districtProvider;
    private SqlJobProcessDao sqlJobProcessDao;

    private SqlGeocodeResultLogger sqlGeocodeResultLogger;
    private SqlDistrictResultLogger sqlDistrictResultLogger;
    private ApplicationConfig applicationConfig;

    private ThreadPoolTaskExecutor addressExecutor;
    private ThreadPoolTaskExecutor geocodeExecutor;
    private ThreadPoolTaskExecutor districtExecutor;

    @Autowired
    public JobBatchProcessor(Environment env, Mailer mailer, AddressServiceProvider addressServiceProvider,
                             GeocodeServiceProvider geocodeServiceProvider,
                             DistrictServiceProvider districtServiceProvider,
                             SqlJobProcessDao sqlJobProcessDao, SqlGeocodeResultLogger sqlGeocodeResultLogger,
                             SqlDistrictResultLogger sqlDistrictResultLogger, ApplicationConfig applicationConfig)
    {
        this.env = env;
        BASE_URL = env.getBaseUrl();
        UPLOAD_DIR = env.getJobUploadDir();
        DOWNLOAD_DIR = env.getJobDownloadDir();
        DOWNLOAD_URL = BASE_URL + DOWNLOAD_BASE_URL;
        JOB_BATCH_SIZE = env.getJobBatchSize();
        SEND_EMAILS = env.getJobSendEmail();
        LOGGING_ENABLED = env.isBatchDetailedLoggingEnabled();

        this.mailer = mailer;
        this.addressProvider = addressServiceProvider;
        this.geocodeProvider = geocodeServiceProvider;
        this.districtProvider = districtServiceProvider;
        this.sqlJobProcessDao = sqlJobProcessDao;

        this.sqlGeocodeResultLogger = sqlGeocodeResultLogger;
        this.sqlDistrictResultLogger = sqlDistrictResultLogger;
        this.applicationConfig = applicationConfig;

        this.addressExecutor = this.applicationConfig.getJobAddressValidationExecutor();
        this.geocodeExecutor = this.applicationConfig.getJobGeocodeExecutor();
        this.districtExecutor = this.applicationConfig.getJobDistrictAssignExecutor();
    }

    @PreDestroy
    public void closeProcessor() {
        this.mailer = null;
        this.addressProvider = null;
        this.geocodeProvider = null;
        this.districtProvider = null;
        this.sqlJobProcessDao = null;

        this.sqlGeocodeResultLogger = null;
        this.sqlDistrictResultLogger = null;
        this.applicationConfig = null;
    }


    @Scheduled(cron = "${job.process.cron}")
    public void jobCron() throws Exception {
        String[] args = new String[1];
        args[0] = "process";
        logger.info("Starting Job Processor");
        run(args);
    }

    /** Entry point for cron job */
    public void run(String[] args) throws Exception
    {
        /** Ensure another job process is not running */
        checkLockFile();

        if (args.length > 0) {


            switch (args[0]) {
                case "clean" : {
                    cancelRunningJobs();
                    break;
                }
                case "process" : {
                    List<JobProcessStatus> runningJobs = getRunningJobProcesses();
                    logger.info("Resuming " + runningJobs.size() + " jobs.");
                    for (JobProcessStatus runningJob : runningJobs) {
                        logger.info("Processing job process id " + runningJob.getProcessId());
                        processJob(runningJob);
                    }

                    List<JobProcessStatus> waitingJobs = getWaitingJobProcesses();
                    logger.info(waitingJobs.size() + " batch jobs have been queued for processing.");
                    for (JobProcessStatus waitingJob : waitingJobs){
                        logger.info("Processing job process id " + waitingJob.getProcessId());
                        processJob(waitingJob);
                    }
                    break;
                }
                default : {
                    logger.error("Unsupported argument. " + args[0] + " Exiting..");
                }
            }

            /** Clean up and exit */
            logger.info("Finishing processing, Exiting Data Processor");
            deleteLockFile();
        }
        else {
            logger.error("Usage: jobBatchProcessor [process | clean]");
            logger.error("Process: Iterates through all pending jobs and completes them.");
            logger.error("Clean:   Cancels all running jobs.");
        }
    }

    /**
     * If the lock file already exists, then fail. Otherwise, create it and arrange for it to be automatically
     * deleted on exit.
     */
    public static void checkLockFile() throws IOException
    {
        File lockFile = new File(TEMP_DIR, LOCK_FILENAME);
        boolean rc = lockFile.createNewFile();
        if (rc == true) {
            logger.debug("Created lock file at: " + lockFile.getAbsolutePath());
            lockFile.deleteOnExit();
        }
        else {
            logger.info("Exiting Data Processor");
            logger.debug("Lock file [" + lockFile.getAbsolutePath() + "] already exists; exiting immediately");
            //back out of api call?
        }
    }

    public static void deleteLockFile() throws IOException {
        File lockFile = new File(TEMP_DIR, LOCK_FILENAME);
        lockFile.deleteOnExit();
    }

    /**
     * Retrieves all job processes that are waiting to be picked up.
     * @return List<JobProcessStatus>
     */
    public List<JobProcessStatus> getWaitingJobProcesses()
    {
        return sqlJobProcessDao.getJobStatusesByCondition(WAITING_FOR_CRON, null);
    }

    /**
     * Retrieves jobs that are still running and need to be finished.
     * @return List<JobProcessStatus>
     */
    public List<JobProcessStatus> getRunningJobProcesses()
    {
        return sqlJobProcessDao.getJobStatusesByCondition(RUNNING, null);
    }

    /**
     * Main routine for processing a JobProcess.
     * @param jobStatus
     */
    public void processJob(JobProcessStatus jobStatus) throws Exception
    {
        JobProcess jobProcess = jobStatus.getJobProcess();
        String fileName = jobProcess.getFileName();

        CsvListReader jobReader = null;
        CsvListWriter jobWriter = null;

        try {
            /** Ensure directories for uploading and downloading exist*/
            ensureDirectoryExists(UPLOAD_DIR);
            ensureDirectoryExists(DOWNLOAD_DIR);

            /** Initialize file readers and writers */
            File uploadedFile = new File(UPLOAD_DIR + fileName);
            File targetFile = new File(DOWNLOAD_DIR, fileName);

            /** Determine the type of formatting (tab, comma, semi-colon) */
            CsvPreference preference = FileUtil.getCsvPreference(uploadedFile);

            FileReader fileReader = new FileReader(uploadedFile);
            jobReader = new CsvListReader(fileReader, preference);

            /** Target writer appends by default */
            FileWriter fileWriter = new FileWriter(targetFile, true);
            jobWriter = new CsvListWriter(fileWriter, preference);

            /** Retrieve the header (first line) */
            String[] header = jobReader.getHeader(true);

            /** Create the job file and analyze the header columns */
            JobFile jobFile = new JobFile();
            jobFile.processHeader(header);

            logger.info("--------------------------------------------------------------------");
            logger.info("Starting Batch Job");
            logger.info("Job Header: " + FormatUtil.toJsonString(header));

            /** Check if file can be skipped */
            if (!jobFile.requiresAny()) {
                skipFile(jobStatus);
            }
            else {
                final CellProcessor[] processors = jobFile.getProcessors().toArray(new CellProcessor[0]);

                /** If process is already running, seek to the last saved record */
                if (jobStatus.getCondition().equals(RUNNING)) {
                    int completedRecords = jobStatus.getCompletedRecords();
                    if (completedRecords > 0) {
                        logger.debug("Skipping ahead " + completedRecords + " records.");
                        for (int i = 0; i < completedRecords; i++) {
                            jobReader.read(processors);
                        }
                    }
                    else {
                        jobWriter.writeHeader(header);
                    }
                }
                /** Otherwise write the header and set the status to running */
                else {
                    jobWriter.writeHeader(header);
                    jobStatus.setCondition(RUNNING);
                    jobStatus.setStartTime(new Timestamp(new Date().getTime()));
                    sqlJobProcessDao.setJobProcessStatus(jobStatus);
                }

                /** Read records into a JobFile */
                logMemoryUsage();
                List<Object> row;
                while( (row = jobReader.read(processors)) != null ) {
                    jobFile.addRecord(new JobRecord(jobFile, row));
                }
                logMemoryUsage();
                logger.info(jobFile.getRecords().size() + " records");
                logger.info("--------------------------------------------------------------------");

                LinkedTransferQueue<Future<JobBatch>> jobResultsQueue = new LinkedTransferQueue<>();
                List<DistrictType> districtTypes = jobFile.getRequiredDistrictTypes();

                int recordCount = jobFile.recordCount();
                int batchCount =  (recordCount + JOB_BATCH_SIZE - 1) / JOB_BATCH_SIZE; // Allows us to round up
                logger.info("Dividing job into " + batchCount + " batches");

                for (int i = 0; i < batchCount; i++) {
                    int from = (i * JOB_BATCH_SIZE);
                    int to = (from + JOB_BATCH_SIZE < recordCount) ? (from + JOB_BATCH_SIZE) : recordCount;
                    ArrayList<JobRecord> batchRecords = new ArrayList<>(jobFile.getRecords().subList(from, to));
                    JobBatch jobBatch = new JobBatch(batchRecords, from, to);

                    Future<JobBatch> futureValidatedBatch = null;
                    if (jobFile.requiresAddressValidation()) {
                        futureValidatedBatch = addressExecutor.submit(new JobBatchProcessor.ValidateJobBatch(jobBatch, jobProcess,  addressProvider));
                    }

                    if (jobFile.requiresGeocode() || jobFile.requiresDistrictAssign()) {
                        Future<JobBatch> futureGeocodedBatch;
                        if (jobFile.requiresAddressValidation() && futureValidatedBatch != null) {
                            futureGeocodedBatch = geocodeExecutor.submit(new JobBatchProcessor.GeocodeJobBatch(futureValidatedBatch, jobProcess,geocodeProvider, sqlGeocodeResultLogger));
                        }
                        else {
                            futureGeocodedBatch = geocodeExecutor.submit(new JobBatchProcessor.GeocodeJobBatch(jobBatch, jobProcess, geocodeProvider, sqlGeocodeResultLogger));
                        }

                        if (jobFile.requiresDistrictAssign() && futureGeocodedBatch != null) {
                            Future<JobBatch> futureDistrictedBatch = districtExecutor.submit(new JobBatchProcessor.DistrictJobBatch(futureGeocodedBatch, districtTypes, jobProcess, districtProvider, sqlDistrictResultLogger));
                            jobResultsQueue.add(futureDistrictedBatch);
                        }
                        else {
                            jobResultsQueue.add(futureGeocodedBatch);
                        }
                    }
                    else {
                        jobResultsQueue.add(futureValidatedBatch);
                    }
                }

                boolean interrupted = false;
                int batchNum = 0;
                while (jobResultsQueue.peek() != null) {
                    try {
                        logger.info("Waiting on batch # " + batchNum);
                        JobBatch batch = jobResultsQueue.poll().get();
                        for (JobRecord record : batch.getJobRecords()) {
                            jobWriter.write(record.getRow(), processors);
                        }
                        jobWriter.flush(); // Ensure records have been written

                        /** Determine if this job process has been cancelled by the user */
                        jobStatus = sqlJobProcessDao.getJobProcessStatus(jobProcess.getId());
                        if (jobStatus.getCondition().equals(CANCELLED)) {
                            logger.warn("Job process has been cancelled by the user!");
                            interrupted = true;
                            break;
                        }

                        jobStatus.setCompletedRecords(jobStatus.getCompletedRecords() + batch.getJobRecords().size());
                        sqlJobProcessDao.setJobProcessStatus(jobStatus);
                        logger.info("Wrote results of batch # " + batchNum);
                        batchNum++;
                    }
                    catch (Exception e) {
                        logger.error("" + e);
                        e.getCause().printStackTrace();
                    }
                }

                if (!interrupted) {
                    successfulProcessHandling(jobStatus);
                }

                if (SEND_EMAILS) {
                    logger.info("--------------------------------------------------------------------");
                    logger.info("Sending email confirmation                                         |");
                    logger.info("--------------------------------------------------------------------");

                    try {
                        sendSuccessMail(jobStatus);
                    }
                    catch (Exception ex) {
                        logger.error("Failed to send completion email!", ex);
                    }

                    logger.info("--------------------------------------------------------------------");
                    logger.info("Completed batch processing for job file!                           |");
                    logger.info("--------------------------------------------------------------------");
                }

                if (LOGGING_ENABLED) {
                    try {
                        logger.info("Flushing log cache...");
                        logMemoryUsage();
                        sqlGeocodeResultLogger.flushBatchRequestsCache();
                        sqlDistrictResultLogger.flushBatchRequestsCache();
                        logMemoryUsage();
                    }
                    catch (Exception ex) {
                        logger.error("Failed to flush log buffer! Logged data will be discarded.", ex);
                    }
                }
            }
        }
        catch (FileNotFoundException ex) {
            handleErrors("Job process " + jobProcess.getId() + "'s file could not be found!",
                    "Could not open file!", ex, jobStatus, SKIPPED);
        }
        catch (IOException ex){
            handleErrors("IOException exception occurred!", "IO Error! ", ex,
                    jobStatus, FAILED);
        }
        catch (InterruptedException ex) {
            handleErrors("Interrupted exception occurred!", "Job Interrupted! ", ex,
                    jobStatus, FAILED);
        }
        catch (ExecutionException ex) {
            handleErrors("Execution exception occurred!", "Execution Error! ", ex,
                    jobStatus, FAILED);
        }
        catch (Exception ex) {
            handleErrors("Unknown exception occurred!", "Fatal Error! ", ex,
                    jobStatus, FAILED);
        }
        finally {
            try {
                jobReader.close();
                jobWriter.close();
            }
            catch (NullPointerException e) {}

            logger.info("Closed resources.");
        }

        return;
    }

    private void ensureDirectoryExists(String dir)  {
        try {
            if ( Files.notExists(Paths.get(dir)) ) {
                Files.createDirectory(Paths.get(dir));
            }
        }
        catch (IOException e) {
            logger.warn("Unable to create directory " + dir);
        }

    }

    private void successfulProcessHandling(JobProcessStatus jobStatus) {
        jobStatus.setCompleted(true);
        jobStatus.setCompleteTime(new Timestamp(new Date().getTime()));
        jobStatus.setCondition(COMPLETED);
        sqlJobProcessDao.setJobProcessStatus(jobStatus);
    }

    private void handleErrors(String loggerMessage , String jobStatusMessage, Exception ex, JobProcessStatus jobStatus,
                              JobProcessStatus.Condition condition) {
        logger.error(fatal, loggerMessage, ex);
        setJobStatusError(jobStatus, condition, jobStatusMessage + ex.getMessage());
        if (SEND_EMAILS) sendErrorMail(jobStatus, ex);
    }

    private void skipFile(JobProcessStatus jobStatus) {
        logger.warn("Warning: Skipping job file - No usps, geocode, or dist assign columns!");
        jobStatus.setCondition(SKIPPED);
        jobStatus.setCompleteTime(new Timestamp(new Date().getTime()));
        sqlJobProcessDao.setJobProcessStatus(jobStatus);
    }

    /**
     * Marks a JobProcessStatus with the given condition and message.
     * @param jobStatus JobProcessStatus to modify.
     * @param condition The condition to write.
     * @param message   The message to write.
     */
    private void setJobStatusError(JobProcessStatus jobStatus, JobProcessStatus.Condition condition, String message)
    {
        if (jobStatus != null) {
            jobStatus.setCondition(condition);
            jobStatus.setMessages(Arrays.asList(message));
            sqlJobProcessDao.setJobProcessStatus(jobStatus);
        }
    }

    /**
     * A callable for the executor to perform address validation for a JobBatch.
     */
    public JobBatch validateJobBatch(JobBatch jobBatch) throws Exception
    {
        List<AddressResult> addressResults = addressProvider.validate(jobBatch.getAddresses(), null, false);
        if (addressResults.size() == jobBatch.getAddresses().size()) {
            for (int i = 0; i < addressResults.size(); i++) {
                jobBatch.setAddressResult(i, addressResults.get(i));
            }
        }
        return jobBatch;
    }

    public static class ValidateJobBatch implements Callable<JobBatch>
    {
        private JobBatch jobBatch;
        private JobProcess jobProcess;
        private  AddressServiceProvider addressProvider;

        public ValidateJobBatch(JobBatch jobBatch, JobProcess jobProcess, AddressServiceProvider addressProvider) {
            this.jobBatch = jobBatch;
            this.jobProcess = jobProcess;
            this.addressProvider = addressProvider;
        }

        @Override
        public JobBatch call() throws Exception
        {
            List<AddressResult> addressResults = addressProvider.validate(jobBatch.getAddresses(), null, false);
            if (addressResults.size() == jobBatch.getAddresses().size()) {
                for (int i = 0; i < addressResults.size(); i++) {
                    jobBatch.setAddressResult(i, addressResults.get(i));
                }
            }
            return this.jobBatch;
        }
    }

    /**
     * A callable for the executor to perform geocoding for a JobBatch.
     */
    public static class GeocodeJobBatch implements Callable<JobBatch>
    {
        private JobBatch jobBatch;
        private Future<JobBatch> futureJobBatch;
        private JobProcess jobProcess;

        private GeocodeServiceProvider geocodeServiceProvider;
        private SqlGeocodeResultLogger sqlGeocodeResultLogger;

        public GeocodeJobBatch(JobBatch jobBatch, JobProcess jobProcess,
                               GeocodeServiceProvider geocodeServiceProvider,
                               SqlGeocodeResultLogger sqlGeocodeResultLogger) {
            this.jobBatch = jobBatch;
            this.jobProcess = jobProcess;
            this.geocodeServiceProvider = geocodeServiceProvider;
            this.sqlGeocodeResultLogger = sqlGeocodeResultLogger;
        }

        public GeocodeJobBatch(Future<JobBatch> futureValidatedJobBatch, JobProcess jobProcess,
                               GeocodeServiceProvider geocodeServiceProvider,
                               SqlGeocodeResultLogger sqlGeocodeResultLogger) {
            this.futureJobBatch = futureValidatedJobBatch;
            this.jobProcess = jobProcess;

            this.geocodeServiceProvider = geocodeServiceProvider;
            this.sqlGeocodeResultLogger = sqlGeocodeResultLogger;
        }

        @Override
        public JobBatch call() throws Exception
        {
            if (jobBatch == null && futureJobBatch != null) {
                this.jobBatch = futureJobBatch.get();
            }
            logger.info("Geocoding for records " + jobBatch.getFromRecord() + "-" + jobBatch.getToRecord());

            BatchGeocodeRequest batchGeoRequest = new BatchGeocodeRequest();
            batchGeoRequest.setJobProcess(this.jobProcess);
            batchGeoRequest.setAddresses(this.jobBatch.getAddresses(true));
            batchGeoRequest.setUseCache(true);
            batchGeoRequest.setUseFallback(true);
            batchGeoRequest.setRequestTime(TimeUtil.currentTimestamp());

            List<GeocodeResult> geocodeResults = geocodeServiceProvider.geocode(batchGeoRequest);
            if (geocodeResults.size() == jobBatch.getJobRecords().size()) {
                for (int i = 0; i < geocodeResults.size(); i++) {
                    jobBatch.setGeocodeResult(i, geocodeResults.get(i));
                }
            }

            if (LOGGING_ENABLED) {
                sqlGeocodeResultLogger.logBatchGeocodeResults(batchGeoRequest, geocodeResults, false);
                if (sqlGeocodeResultLogger.getLogCacheSize() > LOGGING_THRESHOLD) {
                    sqlGeocodeResultLogger.flushBatchRequestsCache();
                    logMemoryUsage();
                }
            }

            return this.jobBatch;
        }
    }

    /**
     * A callable for the executor to perform district assignment for a JobBatch.
     */
    public static class DistrictJobBatch implements Callable<JobBatch>
    {
        private JobProcess jobProcess;
        private Future<JobBatch> futureJobBatch;
        private List<DistrictType> districtTypes;
        private DistrictStrategy districtStrategy = DistrictStrategy.shapeFallback;

        private DistrictServiceProvider districtServiceProvider;
        private SqlDistrictResultLogger sqlDistrictResultLogger;

        public DistrictJobBatch(Future<JobBatch> futureJobBatch, List<DistrictType> types, JobProcess jobProcess,
                                DistrictServiceProvider districtServiceProvider, SqlDistrictResultLogger sqlDistrictResultLogger)
                throws InterruptedException, ExecutionException
        {
            this.jobProcess = jobProcess;
            this.futureJobBatch = futureJobBatch;
            this.districtTypes = types;
            this.districtServiceProvider = districtServiceProvider;
            this.sqlDistrictResultLogger = sqlDistrictResultLogger;
        }

        @Override
        public JobBatch call() throws Exception
        {
            JobBatch jobBatch = futureJobBatch.get();
            logger.info("District assignment for records " + jobBatch.getFromRecord() + "-" + jobBatch.getToRecord());

            BatchDistrictRequest batchDistRequest = new BatchDistrictRequest();
            batchDistRequest.setJobProcess(this.jobProcess);
            batchDistRequest.setDistrictTypes(this.districtTypes);
            batchDistRequest.setGeocodedAddresses(jobBatch.getGeocodedAddresses());
            batchDistRequest.setRequestTime(TimeUtil.currentTimestamp());
            batchDistRequest.setDistrictStrategy(this.districtStrategy);

            List<DistrictResult> districtResults = districtServiceProvider.assignDistricts(batchDistRequest);
            for (int i = 0; i < districtResults.size(); i++) {
                jobBatch.setDistrictResult(i, districtResults.get(i));
            }

            if (LOGGING_ENABLED) {
                sqlDistrictResultLogger.logBatchDistrictResults(batchDistRequest, districtResults, false);
                if (sqlDistrictResultLogger.getLogCacheSize() > LOGGING_THRESHOLD) {
                    sqlDistrictResultLogger.flushBatchRequestsCache();
                    logMemoryUsage();
                }
            }
            return jobBatch;
        }
    }

    /**
     * Sends an email to the JobProcess's submitter and the admin indicating that the job has completed successfully.
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
                DOWNLOAD_URL + jobProcess.getFileName());

        String adminMessage = String.format("Request by %s on %s has been completed and can be downloaded <a href='%s'>here</a>." +
                        "<br/>This is an automated message.", jobUser.getEmail(),
                jobProcess.getRequestTime().toString(), DOWNLOAD_URL + jobProcess.getFileName());

        logger.info("Sending email to " + jobUser.getEmail());
        mailer.sendMail(jobUser.getEmail(), subject, message);
        logger.info("Sending email to " + mailer.getAdminEmail());
        mailer.sendMail(mailer.getAdminEmail(), subject, adminMessage);
    }

    /**
     * Sends an email to the JobProcess's submitter and the admin indicating that the job has encountered an error.
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
        catch (Exception ex2) { logger.error(fatal, "Failed to send error email.. sheesh", ex2); }
    }

    /**
     * Marks all running jobs as cancelled effectively removing them from the queue.
     */
    public void cancelRunningJobs()
    {
        logger.info("Cancelling all running jobs!");
        List<JobProcessStatus> jobStatuses = sqlJobProcessDao.getJobStatusesByCondition(RUNNING, null);
        for (JobProcessStatus jobStatus : jobStatuses) {
            jobStatus.setCondition(CANCELLED);
            jobStatus.setMessages(Arrays.asList("Cancelled during cleanup."));
            jobStatus.setCompleteTime(TimeUtil.currentTimestamp());
            sqlJobProcessDao.setJobProcessStatus(jobStatus);
        }
    }

    /**
     * Prints out memory stats.
     */
    private static void logMemoryUsage()
    {
        logger.info("[RUNTIME STATS]: Free Memory - " + Runtime.getRuntime().freeMemory() + " bytes.");
        logger.info("[RUNTIME STATS]: Total Memory - " + Runtime.getRuntime().totalMemory() + " bytes.");
    }
}