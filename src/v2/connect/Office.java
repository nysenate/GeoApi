package v2.connect;

import java.util.List;

import com.google.gson.annotations.Expose;

import model.Ignore;
import model.PersistentObject;

public class Office {
	@Expose String street;
	@Expose String city;
	@Expose String state;
	@Expose String zip;
	
	@Expose @PersistentObject List<PhoneNumber> phoneNumbers;
	
	@Ignore	@PrimaryKey	Integer id;
	@Ignore	@ForeignKey(Senator.class) String SenatorContact;

	public Office(Integer id, String senatorContact, String street,
			String city, String state, String zip,
			List<PhoneNumber> phoneNumbers) {
		super();
		this.id = id;
		SenatorContact = senatorContact;
		this.street = street;
		this.city = city;
		this.state = state;
		this.zip = zip;
		this.phoneNumbers = phoneNumbers;
	}

	public Integer getId() {
		return id;
	}

	public String getSenatorContact() {
		return SenatorContact;
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

	public List<PhoneNumber> getPhoneNumbers() {
		return phoneNumbers;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public void setSenatorContact(String senatorContact) {
		SenatorContact = senatorContact;
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

	public void setPhoneNumbers(List<PhoneNumber> phoneNumbers) {
		this.phoneNumbers = phoneNumbers;
	}
	
	
}
