package gov.nysenate.sage.model.districts;

import gov.nysenate.sage.model.abstracts.AbstractDistrict;
import gov.nysenate.sage.model.annotations.ForeignKey;

import com.google.gson.annotations.Expose;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

@XStreamAlias("member")
public class Member {
	@Expose String name;
	@Expose String url;
	
	@XStreamOmitField @ForeignKey(AbstractDistrict.class) String district;
	
	public Member() {
		
	}
	
	public Member(String name, String url) {
		this.name = name;
		this.url = url;
	}

	public String getName() {
		return name;
	}

	public String getUrl() {
		return url;
	}
	
	public String getDistrict() {
		return district;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public void setDistrict(String district) {
		this.district = district;
	}
}
