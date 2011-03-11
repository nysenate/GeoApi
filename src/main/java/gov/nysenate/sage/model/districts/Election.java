package gov.nysenate.sage.model.districts;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("election")
public class Election {
	String district;
	
	public Election() {
		
	}
	
	public Election(String district) {
		this.district = district;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	
	
}
