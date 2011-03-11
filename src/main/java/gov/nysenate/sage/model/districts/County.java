package gov.nysenate.sage.model.districts;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("county")
public class County {
	String countyName;
	
	public County() {
		
	}
	
	public County(String countyName) {
		this.countyName = countyName;
	}

	public String getCountyName() {
		return countyName;
	}

	public void setCountyName(String countyName) {
		this.countyName = countyName;
	}
	

	
	
}
