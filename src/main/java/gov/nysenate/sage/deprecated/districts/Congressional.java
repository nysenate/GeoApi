package gov.nysenate.sage.deprecated.districts;


import gov.nysenate.sage.deprecated.abstracts.annotations.PersistentObject;
import gov.nysenate.sage.deprecated.abstracts.annotations.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@Deprecated
@XStreamAlias("congressional")
public class Congressional {
	@Expose @PrimaryKey String district;
	@Expose @PersistentObject Member member;
	
	public Congressional() {
		
	}
	
	public Congressional(String district) {
		this.district = district;
	}
	
	public Congressional(String district, Member member) {
		this.district = district;
		this.member = member;
	}

	public String getDistrict() {
		return district;
	}

	public Member getMember() {
		return member;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public void setMember(Member member) {
		this.member = member;
	}
}
