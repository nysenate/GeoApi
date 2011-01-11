package BulkProcessing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeSet;

import connectors.BingConnect;
import connectors.GoogleConnect;
import connectors.YahooConnect;
import control.Connect;
import control.DelimitedFileExtracter;

import model.BulkProcessing.*;
import model.abstracts.AbstractGeocoder;

public class Processor {
	
	public class GeoFilenameFilter implements FilenameFilter {
		String filter;
		public GeoFilenameFilter(String filter) {
			this.filter = filter;
		}
		public boolean accept(File dir, String name) {
			return name.startsWith(filter);
		}
	}

	public enum Geocoder {
		Google, Yahoo, Bing;
	}
	
	final String ROOT_DIRECTORY = "C:\\workspace\\.metadata\\.plugins\\org.eclipse.wst.server.core\\tmp0\\wtpwebapps\\";
	final String WORK_DIRECTORY = ROOT_DIRECTORY + "upload\\";
	final String DEST_DIRECTORY = ROOT_DIRECTORY + "complete\\";
	final String LOCK_FILE = ".lock";
	
	/* currently set to # of milliseconds in a day */
	final long MS_BUFFER = 86400000L;
	
	final int MAX_GOOGLE_REQUESTS = 8000;
	final int MAX_YAHOO_REQUESTS = 41000;
	final int MAX_BING_REQUESTS = 41000;
	final int MIN_REQUESTS = 100;
		
	BulkRequest AVAIL_REQUESTS;
	BulkRequest CUR_REQUESTS;
	
	public static void main(String[] args) throws IOException, SecurityException, NoSuchMethodException {
/*		BulkRequest br1 = new BulkRequest(100,100,100);
		
		BulkRequest br2 = new BulkRequest(10000,10000,10000);
		br2.setRequestTime(100L);
		
		BulkRequest br3 = new BulkRequest(4000,20000,20000);
		
		Connect c = new Connect();
		c.persist(br1);
		c.persist(br2);
		c.persist(br3);
		*/
		
		Processor p = new Processor();
		
		
		BufferedReader br = new BufferedReader(new FileReader(new File("etc/sd33_boe_example.tsv")));
		
		String header = br.readLine();
		
		DelimitedFileExtracter dfe = new DelimitedFileExtracter("\t", header, Boe3rdTsv.class);
		Boe3rdTsv tuple = (Boe3rdTsv)dfe.processTuple(br.readLine());
		System.out.println(tuple.getBirth_date());
		br.close();
		
		/*p.processFiles();*/
	}
	
	public void processFiles() throws IOException {
		//create lock file to stop processes from running in parallel
		File lock = new File(WORK_DIRECTORY + LOCK_FILE);
		if(lock.exists()) {
			System.err.println("Process already running");
			System.exit(0);
		}
		lock.createNewFile();
		
		//will store requests made during processing
		CUR_REQUESTS = new BulkRequest();
		//total requests made in previous iterations
		AVAIL_REQUESTS = initilaizeRequests();
		
		//job processes ordered from oldest to newest
		TreeSet<JobProcess> jobProcesses = JobProcess.getJobProcesses();
		
		Connect c = new Connect();
		
		for(JobProcess jp:jobProcesses) {
			System.out.println("Current job: " + jp.getContact() + " with file: " + jp.getFileName());
			AbstractGeocoder geocoder = getGeocoder(jp);
			
			int segment = jp.getSegment();
			
			File readFile = new File(
					WORK_DIRECTORY + jp.getFileName() + (segment == -1 ? "":"-raw-" + (segment)));
			File writeFile = new File(
					WORK_DIRECTORY + jp.getFileName() + (segment == -1 ? "-work-1":"-work-" + (segment + 1)));
			writeFile.createNewFile();
			
			BufferedReader br = new BufferedReader(new FileReader(readFile));
			BufferedWriter bw = new BufferedWriter(new FileWriter(writeFile));
			
			String header = br.readLine();
			bw.write(header + "\n");
			
			String in = null;
			while((in  = br.readLine()) != null) {
				geocoder = checkGeocoder(geocoder, jp);
				
				if(geocoder == null) {
					System.err.println("Geocoding unavailable");
					break;
				} else {
					//fetch results
					//write results
					bw.write(in + "\n");
				}
			}
			bw.close();
			
			//if file completely processed
			if(in == null) {
				File workDir = new File(WORK_DIRECTORY);
				//merge work files in upload directory
				mergeFiles(DEST_DIRECTORY + jp.getFileName(), header,
						workDir.list(new GeoFilenameFilter(jp.getFileName() + "-work-")));
				
				//mail(contact, "processing complete")
				//mail(admin, "processing details")
				
				c.deleteObjectById(JobProcess.class, "filename", jp.getFileName());
				
				br.close();
				//delete associated files
				deleteFiles(WORK_DIRECTORY, workDir.list(new GeoFilenameFilter(jp.getFileName())));
			}
			//if file partially processed save work and raw files as segments
			//for later processing
			else {
				if(segment == -1)
					segment = 1;
				else segment += 1;
				
				//create raw file of unprocessed data
				File newRawFile = new File(WORK_DIRECTORY + jp.getFileName() + "-raw-" + segment);
				newRawFile.createNewFile();
				
				BufferedWriter rawBw = new BufferedWriter(new FileWriter(newRawFile));
				rawBw.write(header + "\n" + in + "\n");
				
				while((in  = br.readLine()) != null) {
					rawBw.write(in + "\n");
				}
				rawBw.close();
				
				//delete and recreate process
				c.deleteObjectById(JobProcess.class, "filename", jp.getFileName());
				jp.setSegment(segment);
				c.persist(jp);
				
				br.close();
			}
			
			System.out.println("\n\n");
			if(geocoder == null) {
				break;
			}
			
		}
		//write requests made during processing
		c.persist(CUR_REQUESTS);
		
		c.close();
		lock.delete();
		
	}
	

	
	public void mergeFiles(String destination, String header, String... files) throws IOException {
		File outFile = new File(destination);
		BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
		bw.write(header + "\n");
		
		System.out.print("Merging ");
		for(String fileName:files) {
			File workFile = new File(WORK_DIRECTORY + fileName);
			BufferedReader br = new BufferedReader(new FileReader(workFile));
			br.readLine(); //header
			
			String in = null;
			while((in = br.readLine()) != null) {
				bw.write(in + "\n");
			}
			br.close();
			
			System.out.print(workFile.getName() + ", ");
		}
		bw.close();
		System.out.println("into " + destination);
	}
	
	public void deleteFiles(String directory, String... files) {
		System.out.print("deleting ");
		for(String fileName:files) {
			File file = new File(directory + fileName);
			file.delete();
			System.out.print(file.getName() + ", ");
		}
		System.out.println();
	}
	
	public AbstractGeocoder checkGeocoder(AbstractGeocoder geocoder, JobProcess jp) {
		if(geocoder instanceof GoogleConnect) {
			if(AVAIL_REQUESTS.getGRequests() > MAX_GOOGLE_REQUESTS - MIN_REQUESTS)
				return getGeocoder(jp);
			else {
				AVAIL_REQUESTS.incrementGRequest(1);
				CUR_REQUESTS.incrementGRequest(1);
			}
		}
		else if(geocoder instanceof YahooConnect) {
			if(AVAIL_REQUESTS.getYRequests() > MAX_YAHOO_REQUESTS - MIN_REQUESTS)
				return getGeocoder(jp);
			else {
				AVAIL_REQUESTS.incrementYRequest(1);
				CUR_REQUESTS.incrementYRequest(1);
			}
		}
		else if(geocoder instanceof BingConnect) {
			if(AVAIL_REQUESTS.getBRequests() > MAX_BING_REQUESTS - MIN_REQUESTS)
				return getGeocoder(jp);
			else {
				AVAIL_REQUESTS.incrementBRequest(1);
				CUR_REQUESTS.incrementBRequest(1);
			}
		}
		return geocoder;
	}
	
	public AbstractGeocoder getGeocoder(JobProcess jp) {		
		int lc = jp.getLineCount();
		int g = MAX_GOOGLE_REQUESTS - AVAIL_REQUESTS.getGRequests();
		int b = MAX_BING_REQUESTS - AVAIL_REQUESTS.getBRequests();
		int y = MAX_YAHOO_REQUESTS - AVAIL_REQUESTS.getYRequests();
		
		int cur = 0;
		int max = cur = g;
		Geocoder gr = Geocoder.Google;

		if(b > lc && ((b - lc > 0) && (b - lc < cur - lc))) {
			cur = b;
			gr = Geocoder.Bing;
		}
		else {
			if(b > max && lc > max) {
				max = cur = b;
				gr = Geocoder.Bing;
			}
		}
		
		if(y > lc && ((y - lc > 0) && (y - lc < cur - lc))) {
			cur = y;
			gr = Geocoder.Yahoo;
		}
		else {
			if(y > max && lc > max) {
				max = cur = y;
				gr = Geocoder.Yahoo;
			}
		}
		
		if(cur > MIN_REQUESTS) {
			switch (gr) {
				case Google: 
					return new GoogleConnect();
				case Yahoo: 
					return new YahooConnect();
				case Bing: 
					return new BingConnect();
			}
		}	
		return null;
	}
	
	/**
	 * this function determines the amount of requests that can currently be made
	 * with our geocoders
	 * @return number of available requests
	 */
	public BulkRequest initilaizeRequests() {
		Connect connect = new Connect();
		
		long timeStamp = new Date().getTime() - MS_BUFFER;
		
		int gRequests = 0;
		int yRequests = 0;
		int bRequests = 0;
		
		TreeSet<BulkRequest> bulkRequests = BulkRequest.getBulkRequests();
				
		for(BulkRequest br:bulkRequests) {
			if(br.getRequestTime() < timeStamp) {
				//request is older than a day, remove from history
				connect.deleteObjectById(BulkRequest.class, "requesttime", Long.toString(br.getRequestTime()));
			}
			else {
				gRequests += br.getGRequests();
				yRequests += br.getYRequests();
				bRequests += br.getBRequests();
			}
		}
		connect.close();
		
		return new BulkRequest(gRequests, yRequests, bRequests);
	}
}
