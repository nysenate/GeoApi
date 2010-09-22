package model;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import generated.usps.*;

@XStreamAlias("USPSResponse")
public class ValidateResponse {
	String address1;
	String address2;
	String city;
	String state;
	String zip4;
	String zip5;
	
	public ValidateResponse() {
		
	}

	public ValidateResponse(AddressValidateResponse avr) {
		address1 = avr.getAddress().getAddress1();
		address2 = avr.getAddress().getAddress2();
		city = avr.getAddress().getCity();
		state = avr.getAddress().getState();
		this.setZip4(Integer.toString(avr.getAddress().getZip4()));		
		this.setZip5(Integer.toString(avr.getAddress().getZip5()));
	}
	
	public ValidateResponse(CityStateLookupResponse ctlr) {
		city = ctlr.getZipCode().getCity();
		state = ctlr.getZipCode().getState();
		this.setZip5(Integer.toString(ctlr.getZipCode().getZip5()));
	}
	
	public ValidateResponse(ZipCodeLookupResponse zclr) {
		address1 = zclr.getAddress().getAddress2();
		city = zclr.getAddress().getCity();
		state = zclr.getAddress().getState();
		this.setZip4(Integer.toString(zclr.getAddress().getZip4()));
		this.setZip5(Integer.toString(zclr.getAddress().getZip5()));
	}
	
	public String getAddress1() {
		return address1;
	}

	public String getAddress2() {
		return address2;
	}

	public String getCity() {
		return city;
	}

	public String getState() {
		return state;
	}

	public String getZip4() {
		return zip4;
	}

	public String getZip5() {
		return zip5;
	}

	public void setAddress1(String address1) {
		this.address1 = address1;
	}

	public void setAddress2(String address2) {
		this.address2 = address2;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public void setState(String state) {
		this.state = state;
	}

	public void setZip4(String zip4) {
		while(zip4.length() < 4) {
			zip4 = "0" + zip4;
		}
		this.zip4 = zip4;
	}

	public void setZip5(String zip5) {
		while(zip5.length() < 5) {
			zip5 = "0" + zip5;
		}
		this.zip5 = zip5;
	}
	
	
}
