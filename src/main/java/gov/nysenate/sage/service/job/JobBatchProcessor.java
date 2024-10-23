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
import gov.nysenate.sage.service.geo.SageGeocodeServiceProvider;
import gov.nysenate.sage.util.FileUtil;
import gov.nysenate.sage.util.FormatUtil;
import gov.nysenate.sage.util.Mailer;
import gov.nysenate.sage.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.ArrayList;
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
    // TODO: synchronize with JobStatusController
    private static final Logger logger = LoggerFactory.getLogger(JobBatchProcessor.class);
    private static final Marker fatal = MarkerFactory.getMarker("FATAL");
    private static final int LOGGING_THRESHOLD = 1000;

    private final String uploadDir;
    private final String downloadDir;
    private final String downloadUrl;
    private final boolean loggingEnabled;

    private final Mailer mailer;
    private final AddressServiceProvider addressProvider;
    private final SageGeocodeServiceProvider geocodeProvider;
    private final DistrictServiceProvider districtProvider;
    private final SqlJobProcessDao sqlJobProcessDao;

    private final SqlGeocodeResultLogger sqlGeocodeResultLogger;
    private final SqlDistrictResultLogger sqlDistrictResultLogger;

    private final ThreadPoolTaskExecutor addressExecutor;
    private final ThreadPoolTaskExecutor geocodeExecutor;
    private final ThreadPoolTaskExecutor districtExecutor;

    @Value("${job.batch.size:40}")
    private int jobBatchSize;
    @Value("${job.send.email:true}")
    private boolean sendEmails;

    @Autowired
    public JobBatchProcessor(Environment env, Mailer mailer, AddressServiceProvider addressServiceProvider,
                             SageGeocodeServiceProvider geocodeServiceProvider,
                             DistrictServiceProvider districtServiceProvider,
                             SqlJobProcessDao sqlJobProcessDao, SqlGeocodeResultLogger sqlGeocodeResultLogger,
                             SqlDistrictResultLogger sqlDistrictResultLogger, ApplicationConfig applicationConfig) {
        this.uploadDir = env.getJobUploadDir();
        this.downloadDir = env.getJobDownloadDir();
        this.downloadUrl = env.getBaseUrl() + DOWNLOAD_BASE_URL;
        this.loggingEnabled = env.isBatchDetailedLoggingEnabled();

        this.mailer = mailer;
        this.addressProvider = addressServiceProvider;
        this.geocodeProvider = geocodeServiceProvider;
        this.districtProvider = districtServiceProvider;
        this.sqlJobProcessDao = sqlJobProcessDao;

        this.sqlGeocodeResultLogger = sqlGeocodeResultLogger;
        this.sqlDistrictResultLogger = sqlDistrictResultLogger;

        this.addressExecutor = applicationConfig.getJobAddressValidationExecutor();
        this.geocodeExecutor = applicationConfig.getJobGeocodeExecutor();
        this.districtExecutor = applicationConfig.getJobDistrictAssignExecutor();
    }


    @Scheduled(cron = "${job.process.cron}")
    public void jobCron() throws Exception {
        String[] args = new String[1];
        args[0] = "process";
        logger.info("Starting Job Processor");
        run(args);
    }

    /** Entry point for cron job */
    public synchronized void run(String[] args) throws Exception {
        if (args.length > 0) {
            switch (args[0]) {
                case "clean" : {
                    cancelRunningJobs();
                    break;
                }
                case "process" : {
                    List<JobProcessStatus> runningJobs = getRunningJobProcesses();
                    logger.info("Resuming {} jobs.", runningJobs.size());
                    for (JobProcessStatus runningJob : runningJobs) {
                        logger.info("Processing job process id {}", runningJob.getProcessId());
                        processJob(runningJob);
                    }

                    List<JobProcessStatus> waitingJobs = getWaitingJobProcesses();
                    logger.info("{} batch jobs have been queued for processing.", waitingJobs.size());
                    for (JobProcessStatus waitingJob : waitingJobs){
                        logger.info("Processing job process id {}", waitingJob.getProcessId());
                        processJob(waitingJob);
                    }
                    break;
                }
                default : {
                    logger.error("Unsupported argument. {} Exiting..", args[0]);
                }
            }
            logger.info("Finishing processing, Exiting Data Processor");
        }
        else {
            logger.error("Usage: jobBatchProcessor [process | clean]");
            logger.error("Process: Iterates through all pending jobs and completes them.");
            logger.error("Clean:   Cancels all running jobs.");
        }
    }

    /**
     * Retrieves all job processes that are waiting to be picked up.
     * @return List<JobProcessStatus>
     */
    public List<JobProcessStatus> getWaitingJobProcesses() {
        return sqlJobProcessDao.getJobStatusesByCondition(WAITING_FOR_CRON, null);
    }

    /**
     * Retrieves jobs that are still running and need to be finished.
     * @return List<JobProcessStatus>
     */
    public List<JobProcessStatus> getRunningJobProcesses() {
        return sqlJobProcessDao.getJobStatusesByCondition(RUNNING, null);
    }

    /**
     * Main routine for processing a JobProcess.
     */
    public void processJob(JobProcessStatus jobStatus) throws Exception {
        JobProcess jobProcess = jobStatus.getJobProcess();
        String fileName = jobProcess.getFileName();

        CsvListReader jobReader = null;
        CsvListWriter jobWriter = null;

        try {
            // Ensure directories for uploading and downloading exist
            ensureDirectoryExists(uploadDir);
            ensureDirectoryExists(downloadDir);

            // Initialize file readers and writers
            File uploadedFile = new File(uploadDir + fileName);
            File targetFile = new File(downloadDir, fileName);

            // Determine the type of formatting (tab, comma, semicolon)
            CsvPreference preference = FileUtil.getCsvPreference(uploadedFile);

            FileReader fileReader = new FileReader(uploadedFile);
            jobReader = new CsvListReader(fileReader, preference);

            // Target writer appends by default
            FileWriter fileWriter = new FileWriter(targetFile, true);
            jobWriter = new CsvListWriter(fileWriter, preference);

            // Retrieve the header (first line)
            String[] header = jobReader.getHeader(true);

            // Create the job file and analyze the header columns
            JobFile jobFile = new JobFile();
            jobFile.processHeader(header);

            logger.info("--------------------------------------------------------------------");
            logger.info("Starting Batch Job");
            logger.info("Job Header: {}", FormatUtil.toJsonString(header));

            // Check if file can be skipped
            if (!jobFile.requiresAny()) {
                skipFile(jobStatus);
            }
            else {
                final CellProcessor[] processors = jobFile.getProcessors().toArray(new CellProcessor[0]);

                // If process is already running, seek to the last saved record
                if (jobStatus.getCondition().equals(RUNNING)) {
                    int completedRecords = jobStatus.getCompletedRecords();
                    if (completedRecords > 0) {
                        logger.debug("Skipping ahead {} records.", completedRecords);
                        for (int i = 0; i < completedRecords; i++) {
                            jobReader.read(processors);
                        }
                    }
                    else {
                        jobWriter.writeHeader(header);
                    }
                }
                // Otherwise write the header and set the status to running
                else {
                    jobWriter.writeHeader(header);
                    jobStatus.setCondition(RUNNING);
                    jobStatus.setStartTime(new Timestamp(new Date().getTime()));
                    sqlJobProcessDao.setJobProcessStatus(jobStatus);
                }

                // Read records into a JobFile
                logMemoryUsage();
                List<Object> row;
                while( (row = jobReader.read(processors)) != null ) {
                    jobFile.addRecord(new JobRecord(jobFile, row));
                }
                logMemoryUsage();
                logger.info("{} records", jobFile.getRecords().size());
                logger.info("--------------------------------------------------------------------");

                LinkedTransferQueue<Future<JobBatch>> jobResultsQueue = new LinkedTransferQueue<>();
                List<DistrictType> districtTypes = jobFile.getRequiredDistrictTypes();

                int recordCount = jobFile.recordCount();
                int batchCount =  (recordCount + jobBatchSize - 1) / jobBatchSize; // Allows us to round up
                logger.info("Dividing job into {} batches", batchCount);

                for (int i = 0; i < batchCount; i++) {
                    int from = (i * jobBatchSize);
                    int to = Math.min(from + jobBatchSize, recordCount);
                    ArrayList<JobRecord> batchRecords = new ArrayList<>(jobFile.getRecords().subList(from, to));
                    JobBatch jobBatch = new JobBatch(batchRecords, from, to);

                    Future<JobBatch> futureValidatedBatch = null;
                    if (jobFile.requiresAddressValidation()) {
                        futureValidatedBatch = addressExecutor.submit(new JobBatchProcessor.ValidateJobBatch(jobBatch,  addressProvider));
                    }

                    if (jobFile.requiresGeocode() || jobFile.requiresDistrictAssign()) {
                        Future<JobBatch> futureGeocodedBatch;
                        // TODO: Future<X> vs. X, can be combined
                        if (jobFile.requiresAddressValidation() && futureValidatedBatch != null) {
                            futureGeocodedBatch = geocodeExecutor.submit(new JobBatchProcessor.GeocodeJobBatch(futureValidatedBatch, jobProcess,geocodeProvider, sqlGeocodeResultLogger));
                        }
                        else {
                            futureGeocodedBatch = geocodeExecutor.submit(new JobBatchProcessor.GeocodeJobBatch(jobBatch, jobProcess, geocodeProvider, sqlGeocodeResultLogger));
                        }

                        if (jobFile.requiresDistrictAssign()) {
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
                        logger.info("Waiting on batch # {}", batchNum);
                        JobBatch batch = jobResultsQueue.poll().get();
                        for (JobRecord record : batch.jobRecords()) {
                            jobWriter.write(record.getRow(), processors);
                        }
                        jobWriter.flush(); // Ensure records have been written

                        // Determine if this job process has been cancelled by the user
                        jobStatus = sqlJobProcessDao.getJobProcessStatus(jobProcess.getId());
                        if (jobStatus.getCondition().equals(CANCELLED)) {
                            logger.warn("Job process has been cancelled by the user!");
                            interrupted = true;
                            break;
                        }

                        jobStatus.setCompletedRecords(jobStatus.getCompletedRecords() + batch.jobRecords().size());
                        sqlJobProcessDao.setJobProcessStatus(jobStatus);
                        logger.info("Wrote results of batch # {}", batchNum);
                        batchNum++;
                    }
                    catch (Exception e) {
                        logger.error("{}", String.valueOf(e));
                        e.getCause().printStackTrace();
                    }
                }

                if (!interrupted) {
                    successfulProcessHandling(jobStatus);
                }

                if (sendEmails) {
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

                if (loggingEnabled) {
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
            catch (NullPointerException ignored) {}

            logger.info("Closed resources.");
        }
    }

    private void ensureDirectoryExists(String dir)  {
        try {
            Path path = Path.of(dir);
            if (Files.notExists(path)) {
                Files.createDirectory(path);
            }
        }
        catch (IOException e) {
            logger.warn("Unable to create directory {}", dir);
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
        if (sendEmails) sendErrorMail(jobStatus, ex);
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
    private void setJobStatusError(JobProcessStatus jobStatus, JobProcessStatus.Condition condition, String message) {
        if (jobStatus != null) {
            jobStatus.setCondition(condition);
            jobStatus.setMessages(List.of(message));
            sqlJobProcessDao.setJobProcessStatus(jobStatus);
        }
    }

    public static class ValidateJobBatch implements Callable<JobBatch> {
        private final JobBatch jobBatch;
        private final AddressServiceProvider addressProvider;

        public ValidateJobBatch(JobBatch jobBatch, AddressServiceProvider addressProvider) {
            this.jobBatch = jobBatch;
            this.addressProvider = addressProvider;
        }

        @Override
        public JobBatch call() throws Exception {
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
    public class GeocodeJobBatch implements Callable<JobBatch> {
        private final JobProcess jobProcess;
        private final SageGeocodeServiceProvider geocodeServiceProvider;
        private final SqlGeocodeResultLogger sqlGeocodeResultLogger;
        private JobBatch jobBatch;
        private Future<JobBatch> futureJobBatch;

        public GeocodeJobBatch(JobBatch jobBatch, JobProcess jobProcess,
                               SageGeocodeServiceProvider geocodeServiceProvider,
                               SqlGeocodeResultLogger sqlGeocodeResultLogger) {
            this.jobBatch = jobBatch;
            this.jobProcess = jobProcess;
            this.geocodeServiceProvider = geocodeServiceProvider;
            this.sqlGeocodeResultLogger = sqlGeocodeResultLogger;
        }

        public GeocodeJobBatch(Future<JobBatch> futureValidatedJobBatch, JobProcess jobProcess,
                               SageGeocodeServiceProvider geocodeServiceProvider,
                               SqlGeocodeResultLogger sqlGeocodeResultLogger) {
            this.futureJobBatch = futureValidatedJobBatch;
            this.jobProcess = jobProcess;
            this.geocodeServiceProvider = geocodeServiceProvider;
            this.sqlGeocodeResultLogger = sqlGeocodeResultLogger;
        }

        @Override
        public JobBatch call() throws Exception {
            if (jobBatch == null && futureJobBatch != null) {
                this.jobBatch = futureJobBatch.get();
            }
            logger.info("Geocoding for records {}-{}", jobBatch.fromRecord(), jobBatch.toRecord());

            var batchGeoRequest = new BatchGeocodeRequest(this.jobBatch.getAddresses(true));
            batchGeoRequest.setJobProcess(this.jobProcess);

            List<GeocodeResult> geocodeResults = geocodeServiceProvider.geocode(batchGeoRequest);
            if (geocodeResults.size() == jobBatch.jobRecords().size()) {
                for (int i = 0; i < geocodeResults.size(); i++) {
                    jobBatch.setGeocodeResult(i, geocodeResults.get(i));
                }
            }

            if (loggingEnabled) {
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
    public class DistrictJobBatch implements Callable<JobBatch> {
        private final JobProcess jobProcess;
        private final Future<JobBatch> futureJobBatch;
        private final List<DistrictType> districtTypes;
        private final DistrictStrategy districtStrategy = DistrictStrategy.shapeFallback;

        private final DistrictServiceProvider districtServiceProvider;
        private final SqlDistrictResultLogger sqlDistrictResultLogger;

        public DistrictJobBatch(Future<JobBatch> futureJobBatch, List<DistrictType> types, JobProcess jobProcess,
                                DistrictServiceProvider districtServiceProvider, SqlDistrictResultLogger sqlDistrictResultLogger)
                throws InterruptedException, ExecutionException {
            this.jobProcess = jobProcess;
            this.futureJobBatch = futureJobBatch;
            this.districtTypes = types;
            this.districtServiceProvider = districtServiceProvider;
            this.sqlDistrictResultLogger = sqlDistrictResultLogger;
        }

        @Override
        public JobBatch call() throws Exception {
            JobBatch jobBatch = futureJobBatch.get();
            logger.info("District assignment for records {}-{}", jobBatch.fromRecord(), jobBatch.toRecord());

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

            if (loggingEnabled) {
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
     */
    public void sendSuccessMail(JobProcessStatus jobStatus) throws Exception {
        JobProcess jobProcess = jobStatus.getJobProcess();
        JobUser jobUser = jobProcess.getRequestor();
        String subject = "SAGE Batch Job #" + jobProcess.getId() + " Completed";

        String message = String.format("Your request on %s has been completed and can be downloaded <a href='%s'>here</a>." +
                        "<br/>This is an automated message.", jobProcess.getRequestTime().toString(),
                downloadUrl + jobProcess.getFileName());

        String adminMessage = String.format("Request by %s on %s has been completed and can be downloaded <a href='%s'>here</a>." +
                        "<br/>This is an automated message.", jobUser.getEmail(),
                jobProcess.getRequestTime().toString(), downloadUrl + jobProcess.getFileName());

        logger.info("Sending email to {}", jobUser.getEmail());
        mailer.sendMail(jobUser.getEmail(), subject, message);
        logger.info("Sending email to {}", mailer.getAdminEmail());
        mailer.sendMail(mailer.getAdminEmail(), subject, adminMessage);
    }

    /**
     * Sends an email to the JobProcess's submitter and the admin indicating that the job has encountered an error.
     */
    public void sendErrorMail(JobProcessStatus jobStatus, Exception ex) {
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
                jobUser.getEmail(), jobProcess.getRequestTime().toString(), sw, FormatUtil.toJsonString(jobStatus));

        try {
            logger.info("Sending email to {}", jobUser.getEmail());
            mailer.sendMail(jobUser.getEmail(), subject, message);
            logger.info("Sending email to {}", mailer.getAdminEmail());
            mailer.sendMail(mailer.getAdminEmail(), subject, adminMessage);
        }
        catch (Exception ex2) { logger.error(fatal, "Failed to send error email.. sheesh", ex2); }
    }

    /**
     * Marks all running jobs as cancelled effectively removing them from the queue.
     */
    public void cancelRunningJobs() {
        logger.info("Cancelling all running jobs!");
        List<JobProcessStatus> jobStatuses = sqlJobProcessDao.getJobStatusesByCondition(RUNNING, null);
        for (JobProcessStatus jobStatus : jobStatuses) {
            jobStatus.setCondition(CANCELLED);
            jobStatus.setMessages(List.of("Cancelled during cleanup."));
            jobStatus.setCompleteTime(TimeUtil.currentTimestamp());
            sqlJobProcessDao.setJobProcessStatus(jobStatus);
        }
    }

    /**
     * Prints out memory stats.
     */
    private static void logMemoryUsage() {
        logger.info("[RUNTIME STATS]: Free Memory - {} bytes.", Runtime.getRuntime().freeMemory());
        logger.info("[RUNTIME STATS]: Total Memory - {} bytes.", Runtime.getRuntime().totalMemory());
    }
}
