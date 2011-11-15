package gov.nysenate.geocoder.bulk.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	
	public static Pattern unitPattern = Pattern.compile(
			"(?i)" + 		//case insensitive
			"(.*?)" + 		//match beginning of string: group 1
			"([, ]*?(" + 	//begin looking for excess meta data: group 2
				" (apt\\.?|apartment|room|rm|site|suite|unit) " +
				"|\\d+(st|nd|rd|th) (fl|floor)" +
				"|\\-(pob|bo?x)|(?!^) b \\d+" +
			").+)");
	public static String fixStreet(String street) {
		Matcher unitMatcher = unitPattern.matcher(street);
		if(unitMatcher.find()) {
			return unitMatcher.group(1);
		}
		return street;
	}
}
