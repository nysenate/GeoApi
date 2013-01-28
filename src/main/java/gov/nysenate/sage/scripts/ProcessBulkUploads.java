package gov.nysenate.sage.scripts;

import gov.nysenate.sage.Address;
import gov.nysenate.sage.Result;
import gov.nysenate.sage.BulkProcessing.Mailer;
import gov.nysenate.sage.model.BulkProcessing.BulkFileType;
import gov.nysenate.sage.model.BulkProcessing.BulkInterface;
import gov.nysenate.sage.model.BulkProcessing.JobProcess;
import gov.nysenate.sage.service.DistrictService;
import gov.nysenate.sage.service.DistrictService.DistException;
import gov.nysenate.sage.service.GeoService;
import gov.nysenate.sage.service.GeoService.GeoException;
import gov.nysenate.sage.util.Config;
import gov.nysenate.sage.util.Connect;
import gov.nysenate.sage.util.DelimitedFileExtractor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;


public class ProcessBulkUploads
{
  public class BatchResult
  {
    public ArrayList<Address> addressSet;
    public ArrayList<BulkInterface> recordSet;

    public BatchResult(ArrayList<Address> addressSet, ArrayList<BulkInterface> recordSet)
    {
      this.addressSet = addressSet;
      this.recordSet = recordSet;
    }
  }

 
  public class GeocodeBatch implements Callable<BatchResult>
  {
    public BatchResult input;
    public GeocodeBatch(BatchResult input) { this.input = input; }

    @Override
    public BatchResult call() throws GeoException
    {
      long start = System.currentTimeMillis();
      ArrayList<Result> results = geoService.geocode(input.addressSet, "yahoo", Address.TYPE.PARSED);
      long time = (System.currentTimeMillis()-start);
      logger.info("GeocodeBatch: "+time);

      ArrayList<Address> geocodedAddresses = new ArrayList<Address>();
      for (int i=0; i < results.size(); i++) {
        Result result = results.get(i);
        BulkInterface record = input.recordSet.get(i);
        if (result == null) {
          geocodedAddresses.add(null);

        }
        else if (!result.status_code.equals("0")) {
          logger.info(result.messages);
          geocodedAddresses.add(null);

        }
        else {
          Address geocodedAddress = result.addresses.get(0);
          geocodedAddresses.add(geocodedAddress);
          record.setLat(geocodedAddress.latitude+"");
          record.setLon(geocodedAddress.longitude+"");
        }
      }

      input.addressSet = geocodedAddresses;
      return input;
    }
  }


  public class DistAssignBatch implements Callable<BatchResult>
  {
    public Future<BatchResult> input;
    public DistAssignBatch(Future<BatchResult> input) { this.input = input; }

    @Override
    public BatchResult call() throws ExecutionException, InterruptedException, DistException
    {
      BatchResult batchResult = input.get();
      long start = System.currentTimeMillis();
      ArrayList<Result> results = districtService.assignAll(batchResult.addressSet, "geoserver");
      long time = (System.currentTimeMillis()-start);
      logger.info("DistAssignBatch: " + time);

      ArrayList<Address> distAssignedAddresses = new ArrayList<Address>();
      for (int i = 0; i < results.size(); i++) {
        Result result = results.get(i);
        BulkInterface record = batchResult.recordSet.get(i);

        if (result != null) {
          Address distAssignAddr = result.address;
          record.setCounty(String.format("%02d", distAssignAddr.county_code));
          record.setED(String.format("%03d", distAssignAddr.election_code));
          record.setAD(String.format("%03d", distAssignAddr.assembly_code));
          record.setCD(String.format("%02d", distAssignAddr.congressional_code));
          record.setSD(String.format("%02d", distAssignAddr.senate_code));
          record.setSchool(String.format("%03d", distAssignAddr.school_code));
          record.setTown(distAssignAddr.town_code);
          distAssignedAddresses.add(distAssignAddr);
        }
        else {
          distAssignedAddresses.add(null);
        }
      }
      batchResult.addressSet = distAssignedAddresses;
      return batchResult;
    }
  }


  final int BATCH_SIZE;
  final int GEOCODE_THREADS;
  final int DISTASSIGN_THREADS;

  private static final String TEMP_FILENAME = "bulk_process.lock";
  private final Logger logger;

  private final File UPLOAD_DIR;
  private final File DOWNLOAD_DIR;

  private final GeoService geoService;
  private final DistrictService districtService;


  public static void main(String[] args) throws Exception
  {
    new ProcessBulkUploads().process_files();
  }


  public ProcessBulkUploads() throws Exception
  {
    logger = Logger.getLogger(this.getClass());
    geoService = new GeoService();
    districtService = new DistrictService();

    BATCH_SIZE = Integer.parseInt(Config.read("bulk.batch_size"));
    GEOCODE_THREADS = Integer.parseInt(Config.read("bulk.threads.geocode"));
    DISTASSIGN_THREADS = Integer.parseInt(Config.read("bulk.threads.distassign"));
    UPLOAD_DIR = new File(Config.read("bulk.upload.dir"));
    FileUtils.forceMkdir(UPLOAD_DIR);
    DOWNLOAD_DIR = new File(Config.read("bulk.download.dir"));
    FileUtils.forceMkdir(DOWNLOAD_DIR);

    // If the lock file already exists, then fail. Otherwise, create
    // it and arrange for it to be automatically deleted on exit.
    String tempDir = System.getProperty("java.io.tmpdir", "/tmp");
    File lockFile = new File(tempDir, TEMP_FILENAME);
    boolean rc = lockFile.createNewFile();
    if (rc == true) {
      lockFile.deleteOnExit();
    }
    else {
      System.err.println("Lock file ["+lockFile.getAbsolutePath()+"] already exists; exiting immediately");
      System.exit(1);
    }
  }


  public BulkFileType getBulkFileType(Class<? extends BulkInterface> clazz)
  {
    for (BulkFileType bulkFileType : BulkFileType.values()) {
      if (bulkFileType.clazz().equals(clazz)) {
        return bulkFileType;
      }
    }
    return null;
  }


  /*
   * @author: Jared Williams
   *
   * Determine the line-ending type of the contents of a BufferedReader.
   */
  public static String getNewLineDelim(BufferedReader br) throws IOException
  {
    // Create a CharBuffer to store the first line + line ending then reset
    br.mark(65535);
    int size = br.readLine().length();
    CharBuffer cb = CharBuffer.allocate(size + 2);
    br.reset();

    // Fill the CharBuffer and isolate the line ending characters.
    br.read(cb);
    br.reset();
    String lineEnding = new String(cb.array()).substring(size);

    // Use regex to determine the line ending because the
    // second byte could be part of the next line.
    if (lineEnding.matches("\r\n")) {
      return "\r\n";
    }
    else if (lineEnding.matches("\r.")) {
      return "\r";
    }
    else {
      return "\n";
    }
  }


  private void process_files() throws IOException
  {
    Connect db = new Connect();

    try {
      //job processes ordered from oldest to newest
      logger.info("Loading job processes");
      for (JobProcess jp:JobProcess.getJobProcesses()) {
        logger.info("Current job: " + jp.getContact() + " with file: " + jp.getFileName());

        // Load the appropriate bulk interface tools
        @SuppressWarnings("unchecked")
        Class<? extends BulkInterface> clazz = (Class<? extends BulkInterface>) Class.forName(jp.getClassName());
        BulkFileType bulkFileType = this.getBulkFileType(clazz);
        if (bulkFileType == null) {
          logger.error("Unknown BulkFileType in file: "+jp.getFileName());
          continue;
        }
        DelimitedFileExtractor dfe = new DelimitedFileExtractor(bulkFileType.delimiter(), bulkFileType.header(), clazz);

        // Create our input and output buffers for this job.
        File readFile = new File(UPLOAD_DIR, jp.getFileName());
        File writeFile = new File(DOWNLOAD_DIR, jp.getFileName());
        logger.info("Reading from "+readFile.getAbsolutePath());
        logger.info("Writing to "+writeFile.getAbsolutePath());
        writeFile.createNewFile();
        BufferedReader source = new BufferedReader(new FileReader(readFile));
        BufferedWriter dest = new BufferedWriter(new FileWriter(writeFile));

        // Get the delimiter of the input file for use writing the output file.
        String newLineDelim = getNewLineDelim(source);
        dest.write(source.readLine() + newLineDelim);
        process_file(source, dest, dfe, newLineDelim);

        // Cleanup and move on
        logger.info("Closing file handles.");
        source.close();
        dest.close();
        logger.info("Sending out completion emails");
        Mailer.mailAdminComplete(jp);
        Mailer.mailUserComplete(jp);
        logger.info("deleting job process for file " + jp.getFileName() + " after succesful completion");
        if (!db.deleteObjectById(JobProcess.class, "filename", jp.getFileName())) {
          logger.error("Unable to remove job "+jp.getFileName());
          continue;
        }
      }

    }
    catch (Exception e) {
      logger.error(e);
      Mailer.mailError(e);
    }
    finally {
      db.close();
    }
  }


  private void process_file(BufferedReader source, BufferedWriter dest, DelimitedFileExtractor dfe, String newLine) throws IOException
  {
    BatchResult batch;
    ExecutorService geocodeExecutor = Executors.newFixedThreadPool(GEOCODE_THREADS);
    ExecutorService distAssignExecutor = Executors.newFixedThreadPool(DISTASSIGN_THREADS);
    ArrayList<BatchResult> originalBatches = new ArrayList<BatchResult>();
    ArrayList<Future<BatchResult>> endResults = new ArrayList<Future<BatchResult>>();

    // Queue up all the batches for execution
    do {
      batch = readBatch(source, dfe, BATCH_SIZE);
      Future<BatchResult> futureGeoResults = geocodeExecutor.submit(new GeocodeBatch(batch));
      Future<BatchResult> futureDistAssignResults = distAssignExecutor.submit(new DistAssignBatch(futureGeoResults));
      endResults.add(futureDistAssignResults);
      originalBatches.add(batch);
    } while (batch.recordSet.size() == BATCH_SIZE);

    // Write out results in order, substitute original rows for failed batch jobs
    for (int i=0; i < endResults.size(); i++) {
      ArrayList<BulkInterface> records;
      try {
        records = endResults.get(i).get().recordSet;
      } catch (Exception e) {
        logger.error(e);
        e.getCause().printStackTrace();
        records = originalBatches.get(i).recordSet;
      }

      logger.info("Writing results for batch "+i+1);
      for (BulkInterface record : records) {
        dest.write(record + newLine);
      }
    }
    logger.info("Finished processing batches, shutting down threads.");
    geocodeExecutor.shutdown();
    distAssignExecutor.shutdown();
  }


  private BatchResult readBatch(BufferedReader source, DelimitedFileExtractor dfe, int batchSize) throws IOException
  {
    ArrayList<Address> addressSet = new ArrayList<Address>();
    ArrayList<BulkInterface> recordSet = new ArrayList<BulkInterface>();
    for(int i=0; i < batchSize; i++) {
      String in = source.readLine();
      if (in == null) break;

      BulkInterface tuple = (BulkInterface) dfe.processTuple(in);

      // The address for this record only gets filled if it passes the tests
      // below. By keeping it null we can tell the services to skip it.
      Address address = null;

      try {
        String city = tuple.getCity();
        String state = tuple.getState();
        String street = tuple.getStreet();
        int zip = new Integer(tuple.getZip5().substring(0,5));

        if (street.matches("(?i:\\s*|po box)")) {
          logger.warn("Skipping bad street: "+street);
        } else if (!state.matches("(?i:ny|new york)")) {
          logger.warn("Skipping bad state: "+state);
        } else if((zip < 10001 && zip !=501 && zip!=544 && zip!=6390) || zip>14975) {
          logger.warn("Skipping bad zip code: "+zip);
        } else {
          address = new Address("", street, city, state, ""+zip, "");
        }
      }
      catch (NumberFormatException e) {
        logger.warn("Skipping bad zip code: "+tuple.getZip5());
      }
      catch (StringIndexOutOfBoundsException e) {
        logger.warn("Skipping bad zip code: "+tuple.getZip5());
      }

      recordSet.add(tuple);
      addressSet.add(address);
    }

    return new BatchResult(addressSet,recordSet);
  }
}
