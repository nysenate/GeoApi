package gov.nysenate.sage.service.job;

import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.dao.logger.DistrictResultLogger;
import gov.nysenate.sage.dao.logger.GeocodeResultLogger;
import gov.nysenate.sage.dao.model.JobProcessDao;
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
import gov.nysenate.sage.util.*;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
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
import static gov.nysenate.sage.service.district.DistrictServiceProvider.DistrictStrategy;
import static gov.nysenate.sage.util.controller.ConstantUtil.DOWNLOAD_BASE_URL;

@Service
public class JobBatchProcessor {

    private static String BASE_URL;
    private static String UPLOAD_DIR;
    private static String DOWNLOAD_DIR;
    private static String DOWNLOAD_URL;
    private static Integer ADDRESS_THREAD_COUNT;
    private static Integer GEOCODE_THREAD_COUNT;
    private static Integer DISTRICT_THREAD_COUNT;
    private static Integer JOB_BATCH_SIZE;
    private static String TEMP_DIR = "/tmp";
    private static String LOCK_FILENAME = "batchJobProcess.lock";
    private static Boolean SEND_EMAILS = true;
    private static Boolean LOGGING_ENABLED = false;
    private static Integer LOGGING_THRESHOLD = 1000;

    public static Logger logger = LoggerFactory.getLogger(JobBatchProcessor.class);
    Marker fatal = MarkerFactory.getMarker("FATAL");
    public Environment env;
    public Mailer mailer;
    public AddressServiceProvider addressProvider;
    public GeocodeServiceProvider geocodeProvider;
    public DistrictServiceProvider districtProvider;
    public JobProcessDao jobProcessDao;

    public GeocodeResultLogger geocodeResultLogger;
    public DistrictResultLogger districtResultLogger;



    @Autowired
    public JobBatchProcessor(Environment env, Mailer mailer, AddressServiceProvider addressServiceProvider,
                             GeocodeServiceProvider geocodeServiceProvider,
                             DistrictServiceProvider districtServiceProvider,
                             JobProcessDao jobProcessDao, GeocodeResultLogger geocodeResultLogger,
                             DistrictResultLogger districtResultLogger)
    {
        this.env = env;
        BASE_URL = env.getBaseUrl();
        UPLOAD_DIR = env.getJobUploadDir();
        DOWNLOAD_DIR = env.getJobDownloadDir();
        DOWNLOAD_URL = BASE_URL + DOWNLOAD_BASE_URL;
        ADDRESS_THREAD_COUNT = env.getJobThreadsValidate();
        GEOCODE_THREAD_COUNT = env.getJobThreadsGeocode();
        DISTRICT_THREAD_COUNT = env.getJobThreadsDistassign();
        JOB_BATCH_SIZE = env.getJobBatchSize();
        SEND_EMAILS = env.getJobSendEmail();
        LOGGING_ENABLED = env.isBatchDetailedLoggingEnabled();

        this.mailer = mailer;
        this.addressProvider = addressServiceProvider;
        this.geocodeProvider = geocodeServiceProvider;
        this.districtProvider = districtServiceProvider;
        this.jobProcessDao = jobProcessDao;

        this.geocodeResultLogger = geocodeResultLogger;
        this.districtResultLogger = districtResultLogger;
    }

    /** Entry point for cron job */
    public void run(String[] args) throws Exception
    {
        /** Ensure another job process is not running */
        if (checkLockFile()) {
            return;
        }

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
            logger.info("Wrapping things up..");
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
    public static boolean checkLockFile() throws IOException
    {
        boolean locked = false;
        File lockFile = new File(TEMP_DIR, LOCK_FILENAME);
        boolean rc = lockFile.createNewFile();
        if (rc == true) {
            logger.debug("Created lock file at: " + lockFile.getAbsolutePath());
            lockFile.deleteOnExit();
        }
        else {
            logger.error("Lock file [" + lockFile.getAbsolutePath() + "] already exists; exiting immediately");
            //back out of api call?
            locked = true;
        }
        return locked;
    }

    /**
     * Retrieves all job processes that are waiting to be picked up.
     * @return List<JobProcessStatus>
     */
    public List<JobProcessStatus> getWaitingJobProcesses()
    {
        return jobProcessDao.getJobStatusesByCondition(WAITING_FOR_CRON, null);
    }

    /**
     * Retrieves jobs that are still running and need to be finished.
     * @return List<JobProcessStatus>
     */
    public List<JobProcessStatus> getRunningJobProcesses()
    {
        return jobProcessDao.getJobStatusesByCondition(RUNNING, null);
    }

    /**
     * Main routine for processing a JobProcess.
     * @param jobStatus
     */
    public void processJob(JobProcessStatus jobStatus)
    {
        JobProcess jobProcess = jobStatus.getJobProcess();
        String fileName = jobProcess.getFileName();

        ExecutorService addressExecutor = Executors.newFixedThreadPool(ADDRESS_THREAD_COUNT);
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
                logger.warn("Warning: Skipping job file - No usps, geocode, or dist assign columns!");
                jobStatus.setCondition(SKIPPED);
                jobStatus.setCompleteTime(new Timestamp(new Date().getTime()));
                jobProcessDao.setJobProcessStatus(jobStatus);
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
                    jobProcessDao.setJobProcessStatus(jobStatus);
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
                            futureGeocodedBatch = geocodeExecutor.submit(new JobBatchProcessor.GeocodeJobBatch(futureValidatedBatch, jobProcess,geocodeProvider, geocodeResultLogger));
                        }
                        else {
                            futureGeocodedBatch = geocodeExecutor.submit(new JobBatchProcessor.GeocodeJobBatch(jobBatch, jobProcess, geocodeProvider, geocodeResultLogger));
                        }

                        if (jobFile.requiresDistrictAssign() && futureGeocodedBatch != null) {
                            Future<JobBatch> futureDistrictedBatch = districtExecutor.submit(new JobBatchProcessor.DistrictJobBatch(futureGeocodedBatch, districtTypes, jobProcess, districtProvider, districtResultLogger));
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
                        jobStatus = jobProcessDao.getJobProcessStatus(jobProcess.getId());
                        if (jobStatus.getCondition().equals(CANCELLED)) {
                            logger.warn("Job process has been cancelled by the user!");
                            interrupted = true;
                            break;
                        }

                        jobStatus.setCompletedRecords(jobStatus.getCompletedRecords() + batch.getJobRecords().size());
                        jobProcessDao.setJobProcessStatus(jobStatus);
                        logger.info("Wrote results of batch # " + batchNum);
                        batchNum++;
                    }
                    catch (Exception e) {
                        logger.error("" + e);
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
                        geocodeResultLogger.flushBatchRequestsCache();
                        districtResultLogger.flushBatchRequestsCache();
                        logMemoryUsage();
                    }
                    catch (Exception ex) {
                        logger.error("Failed to flush log buffer! Logged data will be discarded.", ex);
                    }
                }
            }
        }
        catch (FileNotFoundException ex) {
            logger.error("Job process " + jobProcess.getId() + "'s file could not be found!");
            setJobStatusError(jobStatus, SKIPPED, "Could not open file!");
            if (SEND_EMAILS) sendErrorMail(jobStatus, ex);
        }
        catch (IOException ex){
            logger.error("" + ex);
            setJobStatusError(jobStatus, FAILED, "IO Error! " + ex.getMessage());
            if (SEND_EMAILS) sendErrorMail(jobStatus, ex);
        }
        catch (InterruptedException ex) {
            logger.error("" + ex);
            setJobStatusError(jobStatus, FAILED, "Job Interrupted! " + ex.getMessage());
            if (SEND_EMAILS) sendErrorMail(jobStatus, ex);
        }
        catch (ExecutionException ex) {
            logger.error("" + ex);
            setJobStatusError(jobStatus, FAILED, "Execution Error! " + ex.getMessage());
            if (SEND_EMAILS) sendErrorMail(jobStatus, ex);
        }
        catch (Exception ex) {
            logger.error(fatal, "Unknown exception occurred!", ex);
            setJobStatusError(jobStatus, FAILED, "Fatal Error! " + ex.getMessage());
            if (SEND_EMAILS) sendErrorMail(jobStatus, ex);
        }
        finally {
            IOUtils.closeQuietly(jobReader);
            IOUtils.closeQuietly(jobWriter);
            addressExecutor.shutdownNow();
            geocodeExecutor.shutdownNow();
            districtExecutor.shutdownNow();
            logger.info("Closed resources.");
        }
        return;
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
            jobProcessDao.setJobProcessStatus(jobStatus);
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
        private GeocodeResultLogger geocodeResultLogger;

        public GeocodeJobBatch(JobBatch jobBatch, JobProcess jobProcess,
                               GeocodeServiceProvider geocodeServiceProvider,
                               GeocodeResultLogger geocodeResultLogger) {
            this.jobBatch = jobBatch;
            this.jobProcess = jobProcess;
        }

        public GeocodeJobBatch(Future<JobBatch> futureValidatedJobBatch, JobProcess jobProcess,
                               GeocodeServiceProvider geocodeServiceProvider,
                               GeocodeResultLogger geocodeResultLogger) {
            this.futureJobBatch = futureValidatedJobBatch;
            this.jobProcess = jobProcess;

            this.geocodeServiceProvider = geocodeServiceProvider;
            this.geocodeResultLogger = geocodeResultLogger;
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
                geocodeResultLogger.logBatchGeocodeResults(batchGeoRequest, geocodeResults, false);
                if (geocodeResultLogger.getLogCacheSize() > LOGGING_THRESHOLD) {
                    geocodeResultLogger.flushBatchRequestsCache();
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
        private DistrictResultLogger districtResultLogger;

        public DistrictJobBatch(Future<JobBatch> futureJobBatch, List<DistrictType> types, JobProcess jobProcess,
                                DistrictServiceProvider districtServiceProvider, DistrictResultLogger districtResultLogger)
                throws InterruptedException, ExecutionException
        {
            this.jobProcess = jobProcess;
            this.futureJobBatch = futureJobBatch;
            this.districtTypes = types;
            /** Change the strategy if the types contain town or school since they are typically missing in street files */
            if (this.districtTypes.contains(DistrictType.TOWN) || this.districtTypes.contains(DistrictType.SCHOOL)) {
                this.districtStrategy = DistrictStrategy.streetFallback;
            }
            this.districtServiceProvider = districtServiceProvider;
            this.districtResultLogger = districtResultLogger;
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
                districtResultLogger.logBatchDistrictResults(batchDistRequest, districtResults, false);
                if (districtResultLogger.getLogCacheSize() > LOGGING_THRESHOLD) {
                    districtResultLogger.flushBatchRequestsCache();
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
        List<JobProcessStatus> jobStatuses = jobProcessDao.getJobStatusesByCondition(RUNNING, null);
        for (JobProcessStatus jobStatus : jobStatuses) {
            jobStatus.setCondition(CANCELLED);
            jobStatus.setMessages(Arrays.asList("Cancelled during cleanup."));
            jobStatus.setCompleteTime(TimeUtil.currentTimestamp());
            jobProcessDao.setJobProcessStatus(jobStatus);
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