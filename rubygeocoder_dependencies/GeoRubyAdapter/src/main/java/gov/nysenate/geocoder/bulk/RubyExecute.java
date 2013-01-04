package gov.nysenate.geocoder.bulk;
import gov.nysenate.geocoder.bulk.model.RequestObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

public class RubyExecute extends Thread {
	
	private int index;
	private final String url;
	private List<RequestObject> requestObjects;
	private RequestHandler requestHandler;
	
	public RubyExecute(int index, String url, List<RequestObject> requestObjects, RequestHandler requestHandler) {
		this.index = index;
		this.url = url;
		this.requestObjects = requestObjects;
		this.requestHandler = requestHandler;
	}

	@Override
	public void run() {
		String connectUrl = "";
				
		if(requestObjects == null ||  requestObjects.isEmpty())
			return;
		
		try {
			connectUrl = url + "/bulk_geos?json=" + URLEncoder.encode(requestHandler.getMapper().writeValueAsString(requestObjects),"utf-8");
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String returnData = null;
		try {
			returnData = new BufferedReader(new InputStreamReader(new URL(connectUrl).openStream())).readLine();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(returnData ==  null) {
			returnData = "";
			for(int i = 0; i < requestObjects.size(); i++) {
				returnData += "{},";
			}
			returnData = returnData.replaceFirst(",$", "");
		}
		
		requestHandler.fillSegment(index, returnData);
	}

}
