package gov.nysenate.sage.deprecated.districts;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@Deprecated
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
