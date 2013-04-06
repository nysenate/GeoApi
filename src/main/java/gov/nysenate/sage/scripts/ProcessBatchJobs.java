package gov.nysenate.sage.scripts;

import gov.nysenate.sage.dao.model.JobProcessDao;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.job.JobProcess;
import gov.nysenate.sage.model.job.JobProcessStatus;
import gov.nysenate.sage.model.job.file.JobFile;
import gov.nysenate.sage.model.job.file.JobRecord;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.service.district.DistrictServiceProvider;
import gov.nysenate.sage.service.geo.GeocodeServiceProvider;
import gov.nysenate.sage.util.Config;
import gov.nysenate.sage.util.FormatUtil;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

import static gov.nysenate.sage.model.job.JobProcessStatus.Condition.*;

public class ProcessBatchJobs
{
    public static Config config;
    public static String UPLOAD_DIR;
    public static String DOWNLOAD_DIR;
    public static Integer GEOCODE_THREAD_COUNT;
    public static Integer DISTRICT_THREAD_COUNT;
    public static Integer JOB_BATCH_SIZE;

    public static Logger logger = Logger.getLogger(ProcessBatchJobs.class);
    public static GeocodeServiceProvider geocodeProvider;
    public static DistrictServiceProvider districtProvider;
    public static JobProcessDao jobProcessDao;

    /**
     * Represents a subset of job records that belong to a job file.
     */
    public static class JobBatch
    {
        private List<JobRecord> jobRecords;
        private int fromRecord;
        private int toRecord;

        public JobBatch(List<JobRecord> jobRecords, int fromRecord, int toRecord) {
            this.jobRecords = jobRecords;
            this.fromRecord = fromRecord;
            this.toRecord = toRecord;
        }

        public List<Address> getAddresses() {
            List<Address> addresses = new ArrayList<>();
            for (JobRecord jobRecord : jobRecords) {
                addresses.add(jobRecord.getAddress());
            }
            return addresses;
        }

        public List<GeocodedAddress> getGeocodedAddresses() {
            List<GeocodedAddress> geocodedAddresses = new ArrayList<>();
            for (JobRecord jobRecord : jobRecords) {
                geocodedAddresses.add(jobRecord.getGeocodedAddress());
            }
            return geocodedAddresses;
        }

        public void setGeocodeResult(int index, GeocodeResult geocodeResult) {
            this.jobRecords.get(index).applyGeocodeResult(geocodeResult);
        }

        public void setDistrictResult(int index, DistrictResult districtResult) {
            this.jobRecords.get(index).applyDistrictResult(districtResult);
        }

        public List<JobRecord> getJobRecords() {
            return jobRecords;
        }
    }

    public ProcessBatchJobs()
    {
        config = ApplicationFactory.getConfig();
        UPLOAD_DIR = config.getValue("job.upload.dir");
        DOWNLOAD_DIR = config.getValue("job.download.dir");
        GEOCODE_THREAD_COUNT = Integer.parseInt(config.getValue("job.threads.geocode"));
        DISTRICT_THREAD_COUNT = Integer.parseInt(config.getValue("job.threads.distassign"));
        JOB_BATCH_SIZE = Integer.parseInt(config.getValue("job.batch.size", "95"));

        geocodeProvider = ApplicationFactory.getGeocodeServiceProvider();
        districtProvider = ApplicationFactory.getDistrictServiceProvider();
        jobProcessDao = new JobProcessDao();
    }

    /** Entry point for cron job */
    public static void main(String[] args)
    {
        /** Bootstrap the application */
        ApplicationFactory.buildInstances();
        ProcessBatchJobs processBatchJobs = new ProcessBatchJobs();

        if (args.length > 0) {
            switch (args[0]) {
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
                    logger.error("Unsupported argument. Exiting..");
                }
            }
        }

        logger.info("Wrapping things up..");
        ApplicationFactory.close();
        System.exit(0);
    }

    /**
     * Retrieves all job processes that are waiting to be picked up.
     * @return
     */
    public List<JobProcessStatus> getWaitingJobProcesses()
    {
        JobProcessDao jobProcessDao = new JobProcessDao();
        List<JobProcessStatus> jobs = jobProcessDao.getJobStatusesByCondition(WAITING_FOR_CRON, null);
        return jobs;
    }

    public void processJob(JobProcessStatus jobStatus)
    {
        JobProcess jobProcess = jobStatus.getJobProcess();
        String fileName = jobProcess.getFileName();

        ExecutorService geocodeExecutor = Executors.newFixedThreadPool(GEOCODE_THREAD_COUNT);
        ExecutorService districtExecutor = Executors.newFixedThreadPool(DISTRICT_THREAD_COUNT);
        ICsvBeanReader jobReader = null;
        ICsvBeanWriter jobWriter = null;

        try {
            /** Initialize file readers and writers */
            FileReader fileReader = new FileReader(new File(UPLOAD_DIR + fileName));
            jobReader = new CsvBeanReader(fileReader, CsvPreference.TAB_PREFERENCE);

            FileWriter fileWriter = new FileWriter(new File(DOWNLOAD_DIR, fileName));
            jobWriter = new CsvBeanWriter(fileWriter, CsvPreference.TAB_PREFERENCE);

            String[] header = jobReader.getHeader(true);
            jobWriter.writeHeader(header);

            /** Create the job file and analyze the header columns */
            JobFile jobFile = new JobFile();
            header = jobFile.processHeader(header);
            JobRecord jobRecord;

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
                while( (jobRecord = jobReader.read(JobRecord.class, header, processors)) != null ) {
                    jobFile.addRecord(jobRecord);
                }
                logger.info(jobFile.getRecords().size() + " records");
                logger.info("--------------------------------------------------------------------");

                JobBatch jobBatch;
                ArrayList<Future<JobBatch>> jobResults = new ArrayList<>();
                List<DistrictType> districtTypes = jobFile.getRequiredDistrictTypes();

                int recordCount = jobFile.recordCount();
                int batchCount =  (recordCount + JOB_BATCH_SIZE - 1) / JOB_BATCH_SIZE; // Allows us to round up
                logger.info("Dividing job into " + batchCount + " batches");

                for (int i = 0; i < batchCount; i++) {
                    int from = (i * JOB_BATCH_SIZE);
                    int to = (from + JOB_BATCH_SIZE < recordCount) ? (from + JOB_BATCH_SIZE) : recordCount;
                    ArrayList<JobRecord> batchRecords = new ArrayList<>(jobFile.getRecords().subList(from, to));
                    jobBatch = new JobBatch(batchRecords, from, to);

                    Future<JobBatch> futureGeocodedJobBatch = geocodeExecutor.submit(new GeocodeJobBatch(jobBatch));
                    if (jobFile.requiresDistrictAssign()) {
                         Future<JobBatch> futureDistrictedJobBatch = districtExecutor.submit(new DistrictJobBatch(futureGeocodedJobBatch, districtTypes));
                         jobResults.add(futureDistrictedJobBatch);
                    }
                    else {
                        jobResults.add(futureGeocodedJobBatch);
                    }
                }
                for (int i = 0; i < batchCount; i++) {
                    try {
                        logger.info("Waiting on batch # " + i);
                        JobBatch batch = jobResults.get(i).get();
                        for (JobRecord record : batch.getJobRecords()) {
                            jobWriter.write(record, header, processors);
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

                logger.info("--------------------------------------------------------------------");
                logger.info("Completed batch processing for job file!                           |");
                logger.info("--------------------------------------------------------------------");

                jobStatus.setCompleted(true);
                jobStatus.setCompleteTime(new Timestamp(new Date().getTime()));
                jobStatus.setCondition(COMPLETED);
                jobProcessDao.setJobProcessStatus(jobStatus);

                logger.info("Got here");
            }
        }
        catch (FileNotFoundException ex) {
            logger.error("Job process " + jobProcess.getId() + "'s file could not be found!");
            jobStatus.setCondition(SKIPPED);
            jobStatus.setMessages(Arrays.asList("Could not open file!"));
            jobProcessDao.setJobProcessStatus(jobStatus);
        }
        catch (IOException ex){
            logger.error(ex);
        }
        catch (InterruptedException ex) {
            logger.error(ex);
        }
        catch (ExecutionException ex) {
            logger.error(ex);
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
            if (geocodeResults.size() == jobBatch.jobRecords.size()) {
                for (int i = 0; i < geocodeResults.size(); i++) {
                    jobBatch.setGeocodeResult(i, geocodeResults.get(i));
                }
            }
            return this.jobBatch;
        }
    }

    public static class DistrictJobBatch implements Callable<JobBatch>
    {
        private JobBatch jobBatch;
        private List<DistrictType> districtTypes;
        public DistrictJobBatch(Future<JobBatch> futureJobBatch, List<DistrictType> types) throws InterruptedException,
                                                                                                  ExecutionException
        {
            this.jobBatch = futureJobBatch.get();
            this.districtTypes = types;
        }

        @Override
        public JobBatch call() throws Exception
        {
            System.out.print("District assignment for records " + jobBatch.fromRecord + "-" + jobBatch.toRecord);
            List<DistrictResult> districtResults = districtProvider.assignDistricts(jobBatch.getGeocodedAddresses(),
                                                                                    this.districtTypes);
            for (int i = 0; i < districtResults.size(); i++) {
                jobBatch.setDistrictResult(i, districtResults.get(i));
            }
            return this.jobBatch;
        }
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