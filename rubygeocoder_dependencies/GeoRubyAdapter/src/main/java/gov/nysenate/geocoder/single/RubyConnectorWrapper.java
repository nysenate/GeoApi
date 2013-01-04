package gov.nysenate.geocoder.single;

import java.util.ArrayList;
import java.util.List;

public class RubyConnectorWrapper {
	
	private static RubyConnectorWrapper rubyConnectorWrapper;
	public static RubyConnectorWrapper getInstance() {
		if(rubyConnectorWrapper == null) {
			rubyConnectorWrapper = new RubyConnectorWrapper();
		}
		return rubyConnectorWrapper;
	}
	
	private final String[] hosts = {"http://localhost:4564/geo_codes?",
									"http://localhost:4565/geo_codes?",
									"http://localhost:4566/geo_codes?",
									"http://localhost:4567/geo_codes?"};
	
	private List<RubySingleRequest> rubySingleRequests;
	
	private RubyConnectorWrapper() {
		setup();
	}
	
	private void setup() {
		rubySingleRequests = new ArrayList<RubySingleRequest>();
		for(String host:hosts) {
			rubySingleRequests.add(new RubySingleRequest(host));
		}
	}
	
	public synchronized RubySingleRequest getRubySingleRequest() {
		while(true) {
			for(int i = 0; i < rubySingleRequests.size(); i++) {
				if(!rubySingleRequests.get(i).isActive()) {
					rubySingleRequests.get(i).setActive(true);
					return rubySingleRequests.get(i);
				}
			}
		}
	}
}
