package model.districts;

import model.annotations.ForeignKey;

import com.google.gson.annotations.Expose;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

public class PhoneNumber {
	@XStreamOmitField @ForeignKey(Office.class) Integer id;
	@Expose String number;
	
	public PhoneNumber() {
		
	}
	
	public PhoneNumber(Integer id, String number) {
		this.id = id;
		this.number = number;
	}
	public Integer getId() {
		return id;
	}
	public String getNumber() {
		return number;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	
	
}
