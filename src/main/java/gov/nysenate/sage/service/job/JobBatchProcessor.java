package gov.nysenate.sage.service.job;

import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.dao.model.job.SqlJobProcessDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.Accuracy;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.job.*;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.provider.district.DistrictService;
import gov.nysenate.sage.provider.geocode.GeocodeService;
import gov.nysenate.sage.provider.geocode.Geocoder;
import gov.nysenate.sage.service.address.AddressService;
import gov.nysenate.sage.util.*;
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

import javax.annotation.PreDestroy;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedTransferQueue;

import static gov.nysenate.sage.model.job.JobProcessStatus.Condition.*;
import static gov.nysenate.sage.util.controller.ConstantUtil.DOWNLOAD_BASE_URL;

@Service
public class JobBatchProcessor implements JobProcessor {
    private static final Logger logger = LoggerFactory.getLogger(JobBatchProcessor.class);
    private static final Marker fatal = MarkerFactory.getMarker("FATAL");

    private final String uploadDir;
    private final String downloadDir;
    private final String downloadUrl;

    private final Mailer mailer;
    private final AddressService addressService;
    private final GeocodeService geocodeService;
    private final DistrictService districtService;
    private final SqlJobProcessDao sqlJobProcessDao;

    private final ThreadPoolTaskExecutor addressExecutor;
    private final ThreadPoolTaskExecutor geocodeExecutor;
    private final ThreadPoolTaskExecutor districtExecutor;

    @Value("${job.batch.size:40}")
    private int jobBatchSize;
    @Value("${job.send.email:true}")
    private boolean sendEmails;
    private boolean isRunning = false;

    @Autowired
    public JobBatchProcessor(Environment env, Mailer mailer, AddressService addressService,
                             GeocodeService geocodeService, DistrictService districtService,
                             @Value("${base.url:http://localhost:8080}") String baseUrl,
                             SqlJobProcessDao sqlJobProcessDao, @Value("${num.threads:3}") int numThreads) {
        this.uploadDir = env.getJobUploadDir();
        this.downloadDir = env.getJobDownloadDir();
        this.downloadUrl = baseUrl.trim() + DOWNLOAD_BASE_URL;

        this.mailer = mailer;
        this.addressService = addressService;
        this.geocodeService = geocodeService;
        this.districtService = districtService;
        this.sqlJobProcessDao = sqlJobProcessDao;

        this.addressExecutor = ExecutorUtil.createExecutor("job-validator", numThreads);
        this.geocodeExecutor = ExecutorUtil.createExecutor("job-geocoder", numThreads);
        this.districtExecutor = ExecutorUtil.createExecutor("job-dist-assign", numThreads);
    }

    @Scheduled(cron = "${job.process.cron}")
    public synchronized void run() throws Exception {
        isRunning = true;
        List<JobProcessStatus> runningJobs = sqlJobProcessDao.getJobStatusesByCondition(RUNNING, null);
        if (!runningJobs.isEmpty()) {
            logger.info("Resuming {} jobs.", runningJobs.size());
        }
        for (JobProcessStatus runningJob : runningJobs) {
            logger.info("Processing running job process with ID {}", runningJob.getProcessId());
            processJob(runningJob);
        }

        List<JobProcessStatus> waitingJobs = sqlJobProcessDao.getJobStatusesByCondition(WAITING_FOR_CRON, null);
        if (!waitingJobs.isEmpty()) {
            logger.info("{} batch jobs have been queued for processing.", waitingJobs.size());
        }
        for (JobProcessStatus waitingJob : waitingJobs) {
            logger.info("Processing waiting job process with ID {}", waitingJob.getProcessId());
            processJob(waitingJob);
        }
        if (!runningJobs.isEmpty() || !waitingJobs.isEmpty()) {
            logger.info("Finishing processing, Exiting Data Processor");
        }
        isRunning = false;
    }

    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Main routine for processing a JobProcess.
     */
    private void processJob(JobProcessStatus jobStatus) throws Exception {
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
            var jobFile = new JobFile(header);

            logger.info("--------------------------------------------------------------------");
            logger.info("Starting Batch Job");
            logger.info("Job Header: {}", FormatUtil.toJsonString(header));

            // Check if file can be skipped
            if (!jobFile.requiresAddressValidation() && !jobFile.requiresGeocode() && !jobFile.requiresDistrictAssign()) {
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
                List<Object> row;
                while( (row = jobReader.read(processors)) != null ) {
                    jobFile.addRecord(new JobRecord(jobFile.getColumnIndexMap(), row));
                }
                int recordCount = jobFile.recordCount();
                logger.info("{} records", recordCount);
                logger.info("--------------------------------------------------------------------");

                LinkedTransferQueue<Future<JobBatch>> jobResultsQueue = new LinkedTransferQueue<>();

                int batchCount =  (recordCount + jobBatchSize - 1) / jobBatchSize; // Allows us to round up
                logger.info("Dividing job into {} batches", batchCount);

                for (int i = 0; i < batchCount; i++) {
                    int from = (i * jobBatchSize);
                    int to = Math.min(from + jobBatchSize, recordCount);
                    ArrayList<JobRecord> batchRecords = new ArrayList<>(jobFile.getRecords().subList(from, to));
                    var jobBatch = new JobBatch(batchRecords, from, to);

                    Future<JobBatch> fullBatch = addressExecutor.submit(new ValidateJobBatch(jobBatch, addressService));
                    if (jobFile.requiresGeocode() || jobFile.requiresDistrictAssign()) {
                        fullBatch = geocodeExecutor.submit(new GeocodeJobBatch(fullBatch, geocodeService));
                        if (jobFile.requiresDistrictAssign()) {
                            fullBatch = districtExecutor.submit(
                                    new DistrictJobBatch(fullBatch, jobFile.getRequiredDistrictTypes(), districtService)
                            );
                        }
                    }
                    jobResultsQueue.add(fullBatch);
                }

                boolean interrupted = false;
                int batchNum = 0, inStateRecords = 0, correctedAddresses = 0;
                var geocoderUsage = new CountMap<Geocoder>();
                var geocoderAccuracyCount = new CountMap<Accuracy>();
                var districtAssignments = new CountMap<Column>();
                var districtAssignmentAccuracyCount = new CountMap<Accuracy>();
                while (jobResultsQueue.peek() != null) {
                    try {
                        logger.info("Waiting on batch # {}", batchNum);
                        JobBatch batch = jobResultsQueue.poll().get();
                        for (JobRecord record : batch.jobRecords()) {
                            jobWriter.write(record.getRow(), processors);
                            if (record.getAddress() == null || record.getAddress().isOutOfState()) {
                                continue;
                            }
                            inStateRecords++;
                            if (record.getCorrectedAddress() != null && record.getCorrectedAddress().isUspsValidated()) {
                                correctedAddresses++;
                            }
                            var geoAddr = record.getGeocodedAddress();
                            Geocoder geocoder = null;
                            Accuracy quality = null;
                            if (geoAddr != null) {
                                Geocode geocode = geoAddr.getGeocode();
                                if (geocode != null) {
                                    geocoder = geocode.geocoder();
                                    quality = geocode.accuracy();
                                }
                            }
                            geocoderAccuracyCount.put(quality);
                            geocoderUsage.put(geocoder);
                            for (Column distColumn : record.getAssignedDistricts()) {
                                districtAssignments.put(distColumn);
                            }
                            districtAssignmentAccuracyCount.put(record.getAccuracy());
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
                        logger.error("Error when processing job batch", e);
                    }
                }

                if (!interrupted) {
                    successfulProcessHandling(jobStatus);
                }

                if (sendEmails) {
                    logger.info("Sending email confirmation...");
                    try {
                        sendSuccessMail(jobStatus);
                    }
                    catch (Exception ex) {
                        logger.error("Failed to send completion email!", ex);
                    }
                    logger.info("Completed batch processing for job file!");
                }

                logger.info("""
                                Batch job results for NY addresses in {}:
                                {}% validated
                                Geocoder usage:
                                {}
                                Geocode quality:
                                {}
                                District assignments:
                                {}
                                Match level:
                                {}""",
                        fileName,
                        Math.round(100.0 * correctedAddresses/inStateRecords),
                        geocoderUsage.toString(inStateRecords, true),
                        geocoderAccuracyCount.toString(inStateRecords, true),
                        districtAssignments.toString(inStateRecords, false),
                        districtAssignmentAccuracyCount.toString(inStateRecords, true)
                );
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

    private static void ensureDirectoryExists(String dir)  {
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

    private void handleErrors(String loggerMessage, String jobStatusMessage, Exception ex, JobProcessStatus jobStatus,
                              JobProcessStatus.Condition condition) {
        logger.error(fatal, loggerMessage, ex);
        setJobStatusError(jobStatus, condition, jobStatusMessage + ex.getMessage());
        if (sendEmails) {
            sendErrorMail(jobStatus, ex);
        }
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

    private record ValidateJobBatch(JobBatch jobBatch, AddressService addressService) implements Callable<JobBatch> {
        @Override
        public JobBatch call() {
            List<Address> baseAddresses = jobBatch.jobRecords().stream().map(JobRecord::getAddress).toList();
            List<AddressResult> addressResults = addressService.validate(baseAddresses, null);
            jobBatch.setAddressResults(addressResults);
            return this.jobBatch;
        }
    }

    /**
     * A callable for the executor to perform geocoding for a JobBatch.
     */
    private static class GeocodeJobBatch implements Callable<JobBatch> {
        private final GeocodeService geocodeService;
        private final Future<JobBatch> futureJobBatch;

        public GeocodeJobBatch(Future<JobBatch> futureValidatedJobBatch, GeocodeService geocodeService) {
            this.futureJobBatch = futureValidatedJobBatch;
            this.geocodeService = geocodeService;
        }

        @Override
        public JobBatch call() throws Exception {
            JobBatch finishedBatch = futureJobBatch.get();
            LocalDateTime start = LocalDateTime.now();
            List<GeocodeResult> geocodeResults = geocodeService.geocode(finishedBatch.getBestAddresses());
            long millis = ChronoUnit.MILLIS.between(start, LocalDateTime.now());
            logger.info("Geocoded records {}-{} in {} milliseconds",
                    finishedBatch.fromRecord(), finishedBatch.toRecord(), millis);
            finishedBatch.setGeocodeResults(geocodeResults);
            return finishedBatch;
        }
    }

    /**
     * A callable for the executor to perform district assignment for a JobBatch.
     */
    private static class DistrictJobBatch implements Callable<JobBatch> {
        private final Future<JobBatch> futureJobBatch;
        private final Set<DistrictType> districtTypes;
        private final DistrictService districtService;

        public DistrictJobBatch(Future<JobBatch> futureJobBatch, Set<DistrictType> types, DistrictService districtService)
                throws InterruptedException, ExecutionException {
            this.futureJobBatch = futureJobBatch;
            this.districtTypes = types;
            this.districtService = districtService;
        }

        @Override
        public JobBatch call() throws Exception {
            JobBatch jobBatch = futureJobBatch.get();
            LocalDateTime start = LocalDateTime.now();
            List<DistrictResult> districtResults = districtService.assignDistricts(
                    jobBatch.getGeocodedAddresses(), districtTypes
            );
            long millis = ChronoUnit.MILLIS.between(start, LocalDateTime.now());
            logger.info("District assigned records {}-{} in {} milliseconds",
                    jobBatch.fromRecord(), jobBatch.toRecord(), millis);
            jobBatch.setDistrictResults(districtResults);
            return jobBatch;
        }
    }

    /**
     * Sends an email to the JobProcess's submitter and the admin indicating that the job has completed successfully.
     */
    private void sendSuccessMail(JobProcessStatus jobStatus) throws Exception {
        JobProcess jobProcess = jobStatus.getJobProcess();
        JobUser jobUser = jobProcess.getRequestor();
        String subject = "SAGE Batch Job #" + jobProcess.getId() + " Completed";

        String message = String.format("Your request on %s has been completed and can be downloaded <a href='%s'>here</a>." +
                        "<br/>This is an automated message.", jobProcess.getRequestTime().toString(),
                downloadUrl + jobProcess.getFileName());

        String adminMessage = String.format("Request by %s on %s has been completed and can be downloaded <a href='%s'>here</a>." +
                        "<br/>This is an automated message.", jobUser.getEmail(),
                jobProcess.getRequestTime().toString(), downloadUrl + jobProcess.getFileName());

        logger.info("Sending email to job user {}", jobUser.getEmail());
        mailer.sendMail(jobUser.getEmail(), subject, message);
        logger.info("Sending email to {}", mailer.getAdminEmail());
        mailer.sendMail(mailer.getAdminEmail(), subject, adminMessage);
    }

    /**
     * Sends an email to the JobProcess's submitter and the admin indicating that the job has encountered an error.
     */
    private void sendErrorMail(JobProcessStatus jobStatus, Exception ex) {
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
        catch (Exception ex2) {
            logger.error(fatal, "Failed to send error email.", ex2);
        }
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

    @PreDestroy
    private void shutdownThreads(){
        addressExecutor.shutdown();
        geocodeExecutor.shutdown();
        districtExecutor.shutdown();
    }
}
