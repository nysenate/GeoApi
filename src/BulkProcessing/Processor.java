package BulkProcessing;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.TreeSet;

import control.Connect;

import model.BulkProcessing.*;
import model.abstracts.AbstractGeocoder;

public class Processor {
	/* currently set to # of milliseconds in a day */
	static final long MS_BUFFER = 86400000L;
	
	
	
	static final int MAX_GOOGLE_REQUESTS = 8000;
	static final int MAX_YAHOO_REQUESTS = 41000;
	static final int MAX_BING_REQUESTS = 41000;
	static final int MAX_REQUESTS = MAX_GOOGLE_REQUESTS 
								  + MAX_YAHOO_REQUESTS 
								  + MAX_BING_REQUESTS;
	static final int MIN_REQUESTS = 1000;
	
	public static void main(String[] args) {
		/*BulkRequest br1 = new BulkRequest(100,100,100);
		
		BulkRequest br2 = new BulkRequest(10000,10000,10000);
		br2.setRequestTime(100L);
		
		BulkRequest br3 = new BulkRequest(4000,20000,20000);
		
		Connect c = new Connect();
		c.persist(br1);
		c.persist(br2);
		c.persist(br3);*/
		
		
		//write lock
		
		
		Processor p = new Processor();
		
		BulkRequest currentRequests = new BulkRequest();
		
		BulkRequest availableRequests = p.initilaizeRequests();
		
		TreeSet<JobProcess> jobProcesses = JobProcess.getJobProcesses();
		
		for(JobProcess jp:jobProcesses) {
			System.out.println(jp.getRequestTime() + " - " + jp.getContact() + " - " + jp.getFileName());
			
		}
		
		
		//remove lock
	}
	
	public AbstractGeocoder getGeocoder(BulkRequest br, JobProcess jp) {
		
		int lc = jp.getLineCount();
		
		int g = MAX_GOOGLE_REQUESTS - br.getGRequests();
		int b = MAX_BING_REQUESTS - br.getBRequests();
		int y = MAX_YAHOO_REQUESTS - br.getYRequests();
		
		int max = 0;
//		int fit
		
		if(g > max)	max = g;
		if(b > max)	max = b;
		if(y > max)	max = y;
		
		return null;
	}
	
	/**
	 * this function determines the amount of requests that can currently be made
	 * with our geocoders
	 * @return number of availab requests
	 */
	public BulkRequest initilaizeRequests() {
		Connect connect = new Connect();
		
		long timeStamp = new Date().getTime() - MS_BUFFER;
		
		int gRequests = 0;
		int yRequests = 0;
		int bRequests = 0;
		
		TreeSet<BulkRequest> bulkRequests = BulkRequest.getBulkRequests();
		
		System.out.println(bulkRequests.size());
		
		for(BulkRequest br:bulkRequests) {
			if(br.getRequestTime() < timeStamp) {
				connect.deleteObjectById(BulkRequest.class, "requesttime", Long.toString(br.getRequestTime()));
				System.out.println("removing request " + br.getRequestTime());
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
	
	public int getFileLength(BufferedReader br) {
		if(br == null)
			return 0;
		
		try {
			int count = 0;
			String in = null;
			while((in = br.readLine()) != null) {
				count++;
			}
			br.close();
			
			return count;
		}
		catch (Exception e) {
			return 0;
		}
		
	}
}
