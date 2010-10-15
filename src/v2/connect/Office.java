package v2.connect;

import java.util.ArrayList;

import com.google.gson.annotations.Expose;

import model.Ignore;
import model.PersistentObject;

public class Office {
	@Expose String street;
	@Expose String city;
	@Expose String state;
	@Expose String zip;
	
	@Expose @PersistentObject @ListType(PhoneNumber.class) ArrayList<PhoneNumber> phoneNumbers;
	
	@Ignore	@PrimaryKey	Integer id;
	@ForeignKey(Senator.class) String contact;
	
	public Office() {
		phoneNumbers = new ArrayList<PhoneNumber>();
	}

	public Office(Integer id, String street,
			String city, String state, String zip, String  contact,
			ArrayList<PhoneNumber> phoneNumbers) {
		this.id = id;
		this.contact = contact;
		this.street = street;
		this.city = city;
		this.state = state;
		this.zip = zip;
		this.phoneNumbers = phoneNumbers;
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

	public ArrayList<PhoneNumber> getPhoneNumbers() {
		return phoneNumbers;
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

	public void setPhoneNumbers(ArrayList<PhoneNumber> phoneNumbers) {
		this.phoneNumbers = phoneNumbers;
	}
	
	public void addPhoneNumbers(PhoneNumber pn) {
		phoneNumbers.add(pn);
	}
}
