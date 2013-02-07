package gov.nysenate.sage.model.districts;

import gov.nysenate.sage.model.abstracts.AbstractDistrict;
import gov.nysenate.sage.model.annotations.ForeignKey;

import com.google.gson.annotations.Expose;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

@Deprecated
@XStreamAlias("member")
public class Member {
	public static enum MemberType {
		Congress(0), Assembly(1);
		
		int value;
		
		public int value() { return value; }
		
		MemberType(int value) {
			this.value = value;
		}
	}
	
	@Expose String name;
	@Expose String url;
	@XStreamOmitField Integer type;
	
	@XStreamOmitField @ForeignKey(AbstractDistrict.class) String district;
	
	public Member() {
		
	}
	
	public Member(String name, String url, MemberType type) {
		this.name = name;
		this.url = url;
		
		this.type = type.value();
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
	
	
	public Integer getType() {
		return type;
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
	
	public void setType(Integer type) {
		this.type = type;
	}
	
	public void setType(MemberType type) {
		this.type = type.value();;
	}
}
