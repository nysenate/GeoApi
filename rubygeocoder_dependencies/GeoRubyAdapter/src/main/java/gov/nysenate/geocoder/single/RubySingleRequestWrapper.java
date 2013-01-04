package gov.nysenate.geocoder.single;

import javax.servlet.http.HttpServletRequest;

public class RubySingleRequestWrapper {
	private static RubyConnector rubyConnector;
	
	public static synchronized String fillRequest(HttpServletRequest request) {
		if(rubyConnector == null) {
			rubyConnector = new RubyConnector();
		}
		
		return rubyConnector.getResult(request);
	}
}
