package gov.nysenate.sage.model.districts;


import gov.nysenate.sage.model.annotations.ForeignKey;
import gov.nysenate.sage.model.annotations.Ignore;
import gov.nysenate.sage.model.annotations.PrimaryKey;

import com.google.gson.annotations.Expose;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

@Deprecated
@XStreamAlias("office")
public class Office {
	@Expose String street;
	@Expose String city;
	@Expose String state;
	@Expose String zip;
	@Expose Double lat;
	@Expose Double lon;
	@Expose String officeName;
	@Expose String phone;
	@Expose String fax;
		
	@XStreamOmitField @Ignore @PrimaryKey Integer id;
	@XStreamOmitField @ForeignKey(Senator.class) String contact;
	
	public Office() {
	}

	public Office(String street,
			String city, String state, String zip, Double lat, Double lon,
			String officeName, String phone, String fax) {
		this.street = street;
		this.city = city;
		this.state = state;
		this.zip = zip;
		this.lat = lat;
		this.lon = lon;
		this.officeName = officeName;
		this.phone = phone;
		this.fax = fax;
	}

	public Integer getId() {
		return id;
	}

	public String getContact() {
		return contact;
	}

	public String getStreet() {
		return street;
	}

	public String getCity() {
		return city;
	}

	public String getState() {
		return state;
	}

	public String getZip() {
		return zip;
	}
	
	public Double getLat() {
		return lat;
	}
	
	public Double getLon() {
		return lon;
	}
	
	public String getOfficeName() {
		return officeName;
	}
	
	public String getPhone() {
		return phone;
	}
	
	public String getFax() {
		return fax;
	}

	

	public void setId(Integer id) {
		this.id = id;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public void setState(String state) {
		this.state = state;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}
	
	public void setLat(Double lat) {
		this.lat = lat;
	}

	public void setLon(Double lon) {
		this.lon = lon;
	}
	
	public void setOfficeName(String officeName) {
		this.officeName = officeName;
	}
	
	public void setPhone(String phone) {
		this.phone = phone;
	}
	
	public void setFax(String fax) {
		this.fax = fax;
	}
}
