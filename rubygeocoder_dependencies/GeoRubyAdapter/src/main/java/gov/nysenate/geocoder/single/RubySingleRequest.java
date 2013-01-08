package gov.nysenate.geocoder.single;

public class RubySingleRequest {
	private boolean active;
	private String url;
	private RubyConnector rubyConnector;
	
	public RubySingleRequest(String url) {
		this.active = false;
		this.url = url;
		rubyConnector = new RubyConnector(url);
	}
	
	public boolean isActive() {
		return active;
	}
	
	public String getUrl() {
		return url;
	}
	
	public RubyConnector getRubyConnector() {
		return rubyConnector;
	}
		
	public void setActive(boolean active) {
		this.active = active;
	}
}
