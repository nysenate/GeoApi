package gov.nysenate.sage.scripts;

import gov.nysenate.sage.dao.model.JobProcessDao;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.job.BulkInterface;
import gov.nysenate.sage.model.job.JobProcess;
import gov.nysenate.sage.model.job.JobProcessStatus;
import gov.nysenate.sage.model.job.file.*;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.service.district.DistrictService;
import gov.nysenate.sage.service.geo.GeocodeService;
import gov.nysenate.sage.service.geo.GeocodeServiceProvider;
import gov.nysenate.sage.util.Config;
import gov.nysenate.sage.util.FormatUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.log4j.Logger;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import javax.print.attribute.standard.DateTimeAtCompleted;
import java.io.*;
import java.sql.Timestamp;
import java.util.ArrayList;
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
    public static JobProcessDao jobProcessDao;

    /**
     * Represents a collection of
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

        public void setGeocodeResult(int index, GeocodeResult geocodeResult) {
            this.jobRecords.get(index).applyGeocodeResult(geocodeResult);
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
        jobProcessDao = new JobProcessDao();
    }

    /** Entry point for cron job */
    public static void main(String[] args)
    {
        /** Bootstrap the application */
        ApplicationFactory.buildInstances();
        ProcessBatchJobs processBatchJobs = new ProcessBatchJobs();

        List<JobProcessStatus> jobs = processBatchJobs.getWaitingJobProcesses();
        logger.info(jobs.size() + " batch jobs have been queued for processing.");
        for (JobProcessStatus job : jobs){
            logger.info("Processing job process id " + job.getProcessId());
        }
        processBatchJobs.processJob(null);
    }

    /**
     * Retrieves all job processes that are waiting to be picked up.
     * @return
     */
    public List<JobProcessStatus> getWaitingJobProcesses()
    {
        JobProcessDao jobProcessDao = new JobProcessDao();
        List<JobProcessStatus> jobs = jobProcessDao.getJobStatusesByCondition(WAITING_FOR_CRON);
        return jobs;
    }

    public void processJob(JobProcessStatus jobStatus)
    {
        if (jobStatus != null) {
            JobProcess jobProcess = jobStatus.getJobProcess();
            String fileName = jobProcess.getFileName();
        }

        ExecutorService geocodeExecutor = Executors.newFixedThreadPool(GEOCODE_THREAD_COUNT);
        ExecutorService districtExecutor = Executors.newFixedThreadPool(DISTRICT_THREAD_COUNT);
        ICsvBeanReader jobReader = null;
        ICsvBeanWriter jobWriter = null;

        try {
            FileReader fileReader = new FileReader(new File(UPLOAD_DIR + "geocodeBatch1.csv"));
            jobReader = new CsvBeanReader(fileReader, CsvPreference.TAB_PREFERENCE);
            String[] header = jobReader.getHeader(true);

            JobFile jobFile = new JobFile();
            JobRecord jobRecord;
            jobFile.processHeader(header);

            /** Only proceed if there are fields that need populating */
            if (!jobFile.requiresGeocode() && !jobFile.requiresDistrictAssign()) {
                return;
            }

            /** Read records into a JobFile */
            final CellProcessor[] processors = jobFile.getProcessors().toArray(new CellProcessor[0]);
            while( (jobRecord = jobReader.read(JobRecord.class, header, processors)) != null ) {
                jobFile.addRecord(jobRecord);
            }
            logger.info("Stored " + jobFile.getRecords().size() + " records into memory.");

            /** Set the job status to running and record the start time */
            //jobStatus.setCondition(RUNNING);
            //jobStatus.setStartTime(new Timestamp(new Date().getTime()));
            //jobProcessDao.setJobProcessStatus(jobStatus);

            JobBatch jobBatch;
            ArrayList<Future<JobBatch>> jobResults = new ArrayList<>();

            int recordCount = jobFile.recordCount();
            int batchCount =  (recordCount + JOB_BATCH_SIZE - 1) / JOB_BATCH_SIZE; // Allows us to round up

            for (int i = 0; i < batchCount; i++) {
                int from = (i * JOB_BATCH_SIZE);
                int to = (from + JOB_BATCH_SIZE < recordCount) ? (from + JOB_BATCH_SIZE - 1) : recordCount - 1;
                ArrayList<JobRecord> batchRecords = new ArrayList<>(jobFile.getRecords().subList(from, to));
                jobBatch = new JobBatch(batchRecords, from, to);

                Future<JobBatch> futureGeocodedJobBatch = geocodeExecutor.submit(new GeocodeJobBatch(jobBatch));
                if (jobFile.requiresDistrictAssign()) {
                    Future<JobBatch> futureDistrictedJobBatch = districtExecutor.submit(new DistrictJobBatch(futureGeocodedJobBatch.get()));
                    jobResults.add(futureDistrictedJobBatch);
                }
                else {
                    jobResults.add(futureGeocodedJobBatch);
                }
            }

            for (int i = 0; i < batchCount; i++) {
                JobBatch batch;
                try {
                    logger.info("got to batch # " + i);
                    batch = jobResults.get(i).get();
                }
                catch (Exception e) {
                    logger.error(e);
                    e.getCause().printStackTrace();
                }
            }
            //    FileWriter fileWriter = new FileWriter(new File(DOWNLOAD_DIR, "geocodeBatch1.csv"));
            //jobWriter = new CsvBeanWriter(fileWriter, CsvPreference.TAB_PREFERENCE);
            /*
            List<GeocodeResult> results = geoServiceProvider.geocode((ArrayList) geocodeBatchFile.getAddresses());

            for (int i = 0; i < results.size(); i++) {
                if (results.get(i) != null && results.get(i).isSuccess()) {
                    geocodeBatchFile.getRecord(i).setGeocode(results.get(i).getGeocode());
                    //accuracyBatchFile.getRecord(i).setReferenceGeocode(yahooResults.get(i).getGeocode());
                    //accuracyBatchFile.getRecord(i).setDistance(GeocodeUtil.getDistanceInFeet(tigerResults.get(i).getGeocode(), yahooResults.get(i).getGeocode()));
                }
            }

            logger.info("Got here");

            jobWriter.writeHeader(header);
            for ( final GeocodeBatchRecord r : geocodeBatchFile.getRecords()) {
                jobWriter.write(r, header, processors);
            } */
        }
        catch (FileNotFoundException ex) {
            //logger.error("Job process " + jobProcess.getId() + "'s file could not be found!", ex);
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
        public DistrictJobBatch(JobBatch jobBatch) {
            this.jobBatch = jobBatch;
        }

        @Override
        public JobBatch call() throws Exception
        {
            //List<GeocodeResult> geocodeResults = geocodeProvider.geocode(jobBatch.getAddresses());
            System.out.print("From district callable");
            FormatUtil.printObject(jobBatch);
            return this.jobBatch;
        }
    }
}
