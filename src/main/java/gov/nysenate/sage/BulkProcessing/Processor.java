package gov.nysenate.sage.BulkProcessing;

import generated.geoserver.json.GeoResult;
import gov.nysenate.sage.connectors.DistrictServices.DistrictType;
import gov.nysenate.sage.connectors.GeoServerConnect;
import gov.nysenate.sage.connectors.GeocoderConnect;
import gov.nysenate.sage.model.Point;
import gov.nysenate.sage.model.BulkProcessing.*;
import gov.nysenate.sage.util.Connect;
import gov.nysenate.sage.util.DelimitedFileExtractor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

public class Processor {
	
	private Logger logger = Logger.getLogger(Processor.class);
	
	public class GeoFilenameFilter implements FilenameFilter {
		String filter;
		public GeoFilenameFilter(String filter) {
			this.filter = filter;
		}
		public boolean accept(File dir, String name) {
			return name.startsWith(filter);
		}
	}

	final String FILE_SEPERATOR = System.getProperty("file.separator");

	final String COUNTY_FILE = "counties.txt";
	
	final String ROOT_DIRECTORY = "/usr/local/tomcat/webapps/";
	final String WORK_DIRECTORY = ROOT_DIRECTORY + "upload" + FILE_SEPERATOR;
	final String DEST_DIRECTORY = ROOT_DIRECTORY + "complete" + FILE_SEPERATOR;
	final String LOCK_FILE = WORK_DIRECTORY + ".lock";
	
	
	static String ASSEMBLY = "assembly";
	static String CONGRESSIONAL = "congressional";
	static String COUNTY = "county";
	static String ELECTION = "election";
	static String SENATE = "senate";
	
	final static int DEFAULT_SIZE = 48;
	static ObjectMapper mapper = new ObjectMapper();
	
	private static final int MAX_THREADS = 12;
	private volatile List<List<BulkInterface>> responseSegments;
	
	HashMap<String,String> countyLookupMap;
	
	static long s = 0L;
	static long e;
	static long high = 0L;
	static long low = 99999999L;
	static long avg = 0L;
	
	static int count = 0;

	
	public static void main(String[] args) throws IOException, SecurityException, NoSuchMethodException {
		Processor p = new Processor();
		p.processFiles();
	}
	
	@SuppressWarnings("unchecked")
	public void processFiles() throws IOException {
		//create lock file to stop processes from running in parallel
		if(!createLock()) {			
			System.out.println("Process already running or error creating lock file");
			System.exit(0);
		}
		
		try {
			System.out.println("loading county lookup map");
			loadCountyLookupMap();
			
			//job processes ordered from oldest to newest
			System.out.println("loading job processes");
			TreeSet<JobProcess> jobProcesses = JobProcess.getJobProcesses();
			
			Connect c = new Connect();
			
			for(JobProcess jp:jobProcesses) {
				System.out.println("Current job: " + jp.getContact() + " with file: " + jp.getFileName());
				
				Class<? extends BulkInterface> clazz = null;
				try {
					 clazz = (Class<? extends BulkInterface>) Class.forName(jp.getClassName());
				} catch (ClassNotFoundException e) {
					System.err.println("could not create isntance of " + jp.getClassName());
					Mailer.mailError(e, jp);
					continue;
				}
				
				BulkFileType bulkFileType = this.getBulkFileType(clazz);
				if(bulkFileType == null)
					continue;
				
				File readFile = new File(
						WORK_DIRECTORY + jp.getFileName());
				File writeFile = new File(
						DEST_DIRECTORY + jp.getFileName());
				System.out.println("creating new work file: " + writeFile.getAbsolutePath());
				writeFile.createNewFile();
				
				BufferedReader br = new BufferedReader(new FileReader(readFile));
				BufferedWriter bw = new BufferedWriter(new FileWriter(writeFile));
				
				String newLineDelim = getNewLineDelim(br);
				
				DelimitedFileExtractor dfe = new DelimitedFileExtractor(bulkFileType.delimiter(), bulkFileType.header(), clazz);
				//write header
				bw.write(br.readLine() + newLineDelim);
				
				while(doRequest(br, bw, dfe, clazz, newLineDelim));
				bw.close();
				br.close();
				
				Mailer.mailAdminComplete(jp);
				Mailer.mailUserComplete(jp);

				System.out.println("deleting job process for file " + jp.getFileName() + " after succesful completion");
				c.deleteObjectById(JobProcess.class, "filename", jp.getFileName());
				
				//delete associated files
				File workDir = new File(WORK_DIRECTORY);
				deleteFiles(WORK_DIRECTORY, workDir.list(new GeoFilenameFilter(jp.getFileName())));
			}
			
			c.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			Mailer.mailError(e);
		}
		
		if(!deleteLock()) {
			System.err.println("Lock file does not exist or could not be deleted");
		}
		
	}
	
	private boolean doRequest(BufferedReader br, BufferedWriter bw, DelimitedFileExtractor dfe, Class<? extends BulkInterface> clazz, String newLineDelim) throws IOException {
		GeocoderConnect gc = new GeocoderConnect();
		List<Point> points = null;
		List<BulkInterface> addresses = new ArrayList<BulkInterface>();
		
		String json = "";
		
		String in = null;
		for(int i = 0; i < DEFAULT_SIZE && ((in = br.readLine()) != null); i++) {
			BulkInterface tuple = (BulkInterface) dfe.processTuple(in);
			
			if(tuple.getStreet() == null || tuple.getStreet().matches("\\s*")
					|| tuple.getStreet().matches("(?i:po box)")
					|| !tuple.getState().matches("(?i:ny|new york)")) {
				i--;
				bw.write(tuple.toString() + newLineDelim);
				continue;
			}
			
			int zip = -1;
			
			try {
				String z = tuple.getZip5();
				if(z.length() > 5) {
					z = z.substring(0, 5);
				}
				zip = new Integer(z);
			}
			catch(Exception e) {
				bw.write(tuple.toString() + newLineDelim);
				continue;
			}
			
			if((zip >= 10001 && zip <= 14975) || zip == 501 || zip == 544 || zip == 6390) {
				addresses.add(tuple);
				
				RequestObject ro = new RequestObject(tuple.getStreet(), tuple.getCity(), tuple.getState(), tuple.getZip5());
						
				json += mapper.writeValueAsString(ro) + ",";
				
				count++;
			}
			else  {
				bw.write(tuple.toString() + newLineDelim);
				continue;
			}
		}
		if(json.equals(""))
			return false;
		
		json = "[" + json.replaceFirst(",$","") + "]";
		
		s = System.currentTimeMillis();
		points = gc.doBulkParsing(json);
		e = System.currentTimeMillis();
		
		long time = (e-s);
		if(time < low)
			low = time;
		if(time > high)
			high = time;
		avg += time;
		System.out.println(count + ": geocode time (" + points.size() + "): " + time);
		
		if(points.size() == addresses.size()) {
			s = System.currentTimeMillis();
			assignDistricts(points, addresses);
			e = System.currentTimeMillis();
			System.out.println("districting time: " + (e-s));
			
			for(List<BulkInterface> list:responseSegments) {
				for(BulkInterface tuple:list) {
					bw.write(tuple.toString() + newLineDelim);
				}
			}
		}
		else {
			logger.warn("points and addresses don't match " 
					+ points.size() + " : " + addresses.size() 
					+ " : " + addresses.get(0) + " : " 
					+ addresses.get(addresses.size()-1));
		}
		
		return true;
	}
	
	private void assignDistricts(List<Point> points, List<BulkInterface> addresses) {
		int rSize = points.size();
		
		//number of threads we'll be starting up
		int max = (MAX_THREADS > rSize ? rSize : MAX_THREADS);
				
		if(responseSegments == null)
			responseSegments = new ArrayList<List<BulkInterface>>(max);
		else
			responseSegments.clear();
		
		for(int i = 0; i < max; i++) responseSegments.add(null);
		
		
		//remainder of requests modulo max
		int rRem = rSize % max;
		//number of requests per thread - the remainder
		int rNorm = rSize - rRem;
		
		int curIndex = 0;
		
		ExecutorService executor = Executors.newFixedThreadPool(max);
		
		for(int i = 0; i < max ; i++) {
			//number of requests for current thread
			int slice = (((rNorm/max)) + (rRem > 0 ? 1:0));
						
			DistrictExecute dEx = new DistrictExecute(
					i,
					points.subList(curIndex, curIndex + slice),
					addresses.subList(curIndex, curIndex + slice),
					this);
			
			executor.execute(dEx);
			
			curIndex += slice;
			rRem--;
		}
		
		executor.shutdown();
		while(!executor.isTerminated());
	}

	public synchronized void fillSegment(int index, List<BulkInterface> list) {
		responseSegments.set(index, list);
	}
	
	public void deleteFiles(String directory, String... files) {
		for(String fileName:files) {
			File file = new File(directory + fileName);
			file.delete();
			System.out.println("deleting " + file.getName());
		}
	}
	
	
	
	public boolean createLock() throws IOException {
		File lock = new File(LOCK_FILE);
		if(lock.exists()) {
			return false;
		}
		return lock.createNewFile();
	}
	
	public boolean deleteLock() {
		File lock = new File(LOCK_FILE);
		if(lock.exists() && lock.delete()) {
			return true;
		}
		return false;
	}

	public synchronized HashMap<String,String> getCountyLookupMap() throws IOException {
		if(this.countyLookupMap == null) {
			loadCountyLookupMap();
		}
		return this.countyLookupMap;
	}
	
	public void loadCountyLookupMap() throws IOException {		
		BufferedReader br = new BufferedReader(new FileReader(new File(COUNTY_FILE)));
		countyLookupMap = new HashMap<String,String>();
		
		String in = null;
		
		while((in = br.readLine()) != null) {
			//tuple[0] = NYSS county code
			//tuple[1] = county name
			//tuple[2] = fips county code
			String[] tuple = in.split(":");
			
			countyLookupMap.put(tuple[2], tuple[0]);
		}
		br.close();
	}
	
	public String replaceLeading(String str, String leading) {
		if(str.startsWith(leading)) {
			return replaceLeading(str.replaceFirst(leading, ""), leading);
		}
		else {
			return str;
		}
	}
	
	public String padLeft(String string, String padWith, int length) {
		while(string.length() < length)
			string = padWith + string;
		return string;
	}
	
	public BulkFileType getBulkFileType(Class<? extends BulkInterface> clazz) {
		for(BulkFileType bulkFileType:BulkFileType.values()) {
			if(bulkFileType.clazz().equals(clazz))
				return bulkFileType;
		}
		return null;
	}
	
	class DistrictExecute extends Thread  {
		
		private final int index;
		private final List<Point> points;
		private final List<BulkInterface> addresses;
		private final Processor processor;
		
		static final String ASSEMBLY = "assembly";
		static final String CONGRESSIONAL = "congressional";
		static final String COUNTY = "county";
		static final String ELECTION = "election";
		static final String SENATE = "senate";

		public DistrictExecute(int index, List<Point> points,
				List<BulkInterface> addresses, Processor bulkTest) {
			this.index = index;
			this.points = points;
			this.addresses = addresses;
			this.processor = bulkTest;
		}
		
		@Override
		public void run() {
			
			for(int i = 0; i < points.size(); i++)
				fillRequest(points.get(i), addresses.get(i));
			
			processor.fillSegment(index, addresses);
		}
		
		public void fillRequest(Point p, BulkInterface bi) {
			if(p.lat == -1 || p.lon == -1) {
				return;
			}
			
			GeoResult gr = null;
			GeoServerConnect gsCon = new GeoServerConnect();
			
			/*
			 * swallowing exceptions since the only reason we'd
			 * get one is if we attempt to lookup a lat/lon
			 * outside of new york state (or if we get connection errors, but
			 * that's an entirely different issue)
			 */
			try {
				bi.setLat(p.lat + "");
				bi.setLon(p.lon + "");
			}
			catch(Exception e) { }

			try {
				gr = gsCon.fromGeoserver(gsCon.new WFS_REQUEST(DistrictType.COUNTY), p);
				//converts FIPS county code from geoserver to NYSS county code
				bi.setCounty(padLeft(processor.getCountyLookupMap().get(
						replaceLeading(gr.getFeatures().iterator().next().getProperties().getCOUNTYFP(),"0")), "0", 2));
			}
			catch(Exception e) { }
				
			try {
				gr = gsCon.fromGeoserver(gsCon.new WFS_REQUEST(DistrictType.ELECTION), p);
				bi.setED(padLeft(gr.getFeatures().iterator().next().getProperties().getED(), "0", 3));
			}
			catch(Exception e) { }
			
			try {
				gr = gsCon.fromGeoserver(gsCon.new WFS_REQUEST(DistrictType.ASSEMBLY), p);
				bi.setAD(padLeft(gr.getFeatures().iterator().next().getProperties().getNAMELSAD().replace("Assembly District ",""), "0", 3));
			}
			catch(Exception e) { }
			
			try {
				gr = gsCon.fromGeoserver(gsCon.new WFS_REQUEST(DistrictType.CONGRESSIONAL), p);
				bi.setCD(padLeft(gr.getFeatures().iterator().next().getProperties().getNAMELSAD().replace("Congressional District ", ""), "0", 2));
			}
			catch(Exception e) { }
				
			try {
				gr = gsCon.fromGeoserver(gsCon.new WFS_REQUEST(DistrictType.SENATE), p);
				bi.setSD(padLeft(gr.getFeatures().iterator().next().getProperties().getNAMELSAD().replace("State Senate District ",""), "0", 2));
			}
			catch(Exception e) { }
		}
	}
	
	public static String getNewLineDelim(BufferedReader br) throws IOException {
		br.mark(65535);
		
		int size = br.readLine().length();
		
		br.reset();
		
		CharBuffer cb = CharBuffer.allocate(size + 2);
		
		br.read(cb);
		
		String feed = new String(cb.array()).substring(size);
		
		br.reset();
		br.mark(0);
		
		if(feed.matches("\r\n"))
			return feed;
		else if(feed.matches("\r."))
			return "\r";
		else 
			return "\n";
	}
}
