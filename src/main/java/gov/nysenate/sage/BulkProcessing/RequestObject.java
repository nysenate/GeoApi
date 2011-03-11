package gov.nysenate.sage.BulkProcessing;

public class RequestObject {
	private String street;
	private String city;
	private String state;
	private String zip5;
	
	public RequestObject() {
		
	}
	
	public RequestObject(String street, String city, String state, String zip5) {
		this.street = street;
		this.city = city;
		this.state = state;
		this.zip5 = zip5;
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

	public String getZip5() {
		return zip5;
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

	public void setZip5(String zip5) {
		this.zip5 = zip5;
	}
	
	@Override
	public String toString() {
		return this.getStreet();
	}
}

