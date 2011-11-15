package gov.nysenate.geocoder.bulk;

import gov.nysenate.geocoder.bulk.model.RequestList;
import gov.nysenate.geocoder.bulk.model.RequestObject;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class RequestHandler {
	
	private ObjectMapper mapper = null;
	private final String[] hosts = {"http://localhost:4560",
									"http://localhost:4561",
									"http://localhost:4562",
									"http://localhost:4563"};
		
	private String json;
	private volatile String[] responseSegments;
	
	public RequestHandler(String json) {
		this.json = json;
		responseSegments = null;
	}
	
	public RequestHandler(HttpServletRequest request) {
		this.json = request.getParameter("json");
		responseSegments = null;
	}

	public String execute() {
		if(json == null)
			return "";
		
		RequestList requestList = null;
		
		try {
			requestList = (RequestList) getMapper().readValue("{\"requestObjects\":" + json + "}", RequestList.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for(RequestObject ro:requestList.getRequestObjects()) {
			ro.setStreet(RequestObject.fixStreet(ro.getStreet()));
		}
		
		executeThreads(requestList);
		
		if(requestList == null || requestList.getRequestObjects().size() == 0)
			return "";	
		
		String returnData = "";
		for(int i = 0; i < responseSegments.length; i++) {
			returnData += responseSegments[i] + ",";
		}
		return returnData.replaceFirst(",$", "");
	}
	
	public void executeThreads(RequestList requestList) {
		int rSize = requestList.getRequestObjects().size();
		
		//number of threads we'll be starting up
		int max = (hosts.length > rSize ? rSize : hosts.length);
		responseSegments = new String[max];
		
		//remainder of requests modulo max
		int rRem = rSize % max;
		//number of requests per thread - the remainder
		int rNorm = rSize - rRem;
		
		int curIndex = 0;
		
		for(int i = 0; i < max ; i++) {
			//number of requests for current thread
			int slice = (((rNorm/max)) + (rRem > 0 ? 1:0));
						
			new RubyExecute(
					i,
					hosts[i],
					requestList.getRequestObjects().subList(curIndex, curIndex + slice),
					this)
				.start();
			
			curIndex += slice;
			rRem--;
		}
		
		//read through response segments until there aren't any null values
		// (or all the threads are complete)
		boolean tog = true;
		while(tog) {
			for(int i = 0; i < responseSegments.length; i++) {
				if(responseSegments[i] == null)
					break;
				
				if((i+1) == responseSegments.length) {
					tog = false;
				}
			}
		}
	}
	
	public synchronized void fillSegment(int index, String jsonData) {
		this.responseSegments[index] = jsonData;
	}
	
	public synchronized ObjectMapper getMapper() {
		if(mapper == null) {
			mapper = new ObjectMapper();
		}
		return mapper;
	}
	
}
