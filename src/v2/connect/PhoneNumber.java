package v2.connect;

import com.google.gson.annotations.Expose;

import model.Ignore;

public class PhoneNumber {
	@Ignore @ForeignKey(Office.class) Integer OfficeId;
	@Expose String number;
	public PhoneNumber(Integer officeId, String number) {
		super();
		OfficeId = officeId;
		this.number = number;
	}
	public Integer getOfficeId() {
		return OfficeId;
	}
	public String getNumber() {
		return number;
	}
	public void setOfficeId(Integer officeId) {
		OfficeId = officeId;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	
	
}
