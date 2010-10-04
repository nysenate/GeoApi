package connectors;

import generated.usps.*;
import generated.usps.Error;

import java.net.URL;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;


import model.ErrorResponse;
import model.ValidateResponse;
import com.google.gson.Gson;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import control.Resource;

/**
 * @author Jared Williams
 * 
 * used to connect to USPS AMS api and retrieve valided addresses, zip codes or retrieve
 * information based on zip codes
 */
public class USPSConnect {
	
	public static final String API_ID = "usps.id";
	public static final String API_BASE = "usps.url";
	public static final String API_FORMAT = "usps.format";
	public static final String API_VERIFY = "usps.verify";
	public static final String API_CITYSTATE = "usps.citystate";
	public static final String API_ZIPCODE = "usps.zipcode";
	
	public static String validateAddress(String addr1, String addr2, 
			String city, String state, String zip5, String zip4, String format) {
				
		Object o = getResponse(constructAddressUrl(addr1,addr2,city,state,zip5,zip4).replaceAll(" ","%20"));
		
		AddressValidateResponse avr = (AddressValidateResponse)o;
			
		if(avr.getAddress().getError() != null) {
			o = new ErrorResponse(avr.getAddress().getError().getDescription());
		}
		else {
			o = new ValidateResponse(avr);
		}
		
		if(format.equals("xml")) {
			XStream xstream = new XStream(new DomDriver());
			xstream.processAnnotations(new Class[]{ValidateResponse.class, ErrorResponse.class});
			return xstream.toXML(o);
		}
		Gson gson = new Gson();
		return gson.toJson(o);
	}
	
	public static String getCityStateByZipCode(String zip5, String format) {
		
		Object o = getResponse(constructCityStateUrl(zip5).replaceAll(" ","%20"));
		
		CityStateLookupResponse cslr = (CityStateLookupResponse)o;
			
		if(cslr.getZipCode().getError() != null) {
			o = new ErrorResponse(cslr.getZipCode().getError().getDescription());
		}
		else {
			o = new ValidateResponse(cslr);
		}
		
		if(format.equals("xml")) {
			XStream xstream = new XStream(new DomDriver());
			xstream.processAnnotations(new Class[]{ValidateResponse.class, ErrorResponse.class});
			return xstream.toXML(o);
		}
		Gson gson = new Gson();
		return gson.toJson(o);
	}
	
	public static String getZipByAddress(String addr1, String addr2, String city, String state, String format) {
		
		Object o = getResponse(constructZipCodeUrl(addr1,addr2,city,state).replaceAll(" ","%20"));
		
		if(o instanceof Error) {
			Error error = (Error)o;
			o = new ErrorResponse(error.getDescription());
		}
		else {
			ZipCodeLookupResponse zclr = (ZipCodeLookupResponse)o;
			
			if(zclr.getAddress().getError() != null) {
				o = new ErrorResponse(zclr.getAddress().getError().getDescription());
			}
			else {
				o = new ValidateResponse(zclr);
			}
		}
		
		
		if(format.equals("xml")) {
			XStream xstream = new XStream(new DomDriver());
			xstream.processAnnotations(new Class[]{ValidateResponse.class, ErrorResponse.class});
			return xstream.toXML(o);
		}
		Gson gson = new Gson();
		return gson.toJson(o);
	}
	
	private static String isNull(String s) {
		return (s == null) ? "" : s;
	}
	
	public static String constructAddressUrl(String addr1, String addr2, 
				String city, String state, String zip5, String zip4) {
		
		return Resource.get(API_BASE)
		+ Resource.get(API_VERIFY)
		+ Resource.get(API_FORMAT)
		+ "<AddressValidateRequest USERID=\"" + Resource.get(API_ID) + "\">"
			+ "<Address ID=\"" + 0 + "\">"
				+ "<Address1>" + isNull(addr1) + "</Address1>"
				+ "<Address2>" + isNull(addr2) + "</Address2>"
				+ "<City>" + isNull(city) + "</City>"
				+ "<State>" + isNull(state) + "</State>"
				+ "<Zip5>" + isNull(zip5) + "</Zip5>"
				+ "<Zip4>" + isNull(zip4) + "</Zip4>"
			+ "</Address>"
		+ "</AddressValidateRequest>";
	}
	
	public static AddressValidateResponse getAddressValidationResponse(String addr1, String addr2, 
			String city, String state, String zip5, String zip4) {
		try {
			return (AddressValidateResponse)parseStream(new URL(constructAddressUrl(addr1,addr2,city,state,zip5,zip4).replaceAll(" ","%20")));
			
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		return null;
	}
	
	public static Object getResponse(String url) {
		try {
			return parseStream(new URL(url));
			
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
			
		}
	}
	
	public static String constructCityStateUrl(String zip5) {
		return Resource.get(API_BASE)
			+ Resource.get(API_CITYSTATE)
			+ Resource.get(API_FORMAT)
			+ "<CityStateLookupRequest USERID=\"" + Resource.get(API_ID) + "\">"
				+ "<ZipCode ID=\"" + 0 + "\">"
					+ "<Zip5>" + isNull(zip5) + "</Zip5>"
				+ "</ZipCode>"
			+ "</CityStateLookupRequest>";
	}
	public static CityStateLookupResponse getCityStateLookupResponse(String zip5) {
		try {
			return (CityStateLookupResponse)parseStream(new URL(constructCityStateUrl(zip5).replaceAll(" ","%20")));
			
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		return null;
	}
	
	public static String constructZipCodeUrl(String addr1, String addr2, String city, String state) {
		
		return Resource.get(API_BASE)
		+ Resource.get(API_ZIPCODE)
		+ Resource.get(API_FORMAT)
		+ "<ZipCodeLookupRequest USERID=\"" + Resource.get(API_ID) + "\">"
			+ "<Address ID=\"" + 0 + "\">"
				+ "<Address1>" + isNull(addr1) + "</Address1>"
				+ "<Address2>" + isNull(addr2) + "</Address2>"
				+ "<City>" + isNull(city) + "</City>"
				+ "<State>" + isNull(state) + "</State>"
			+ "</Address>"
		+ "</ZipCodeLookupRequest>";
	}
	public static ZipCodeLookupResponse getZipCodeLookupResponse(String addr1, String addr2, String city, String state) {
		try {
			return (ZipCodeLookupResponse)parseStream(new URL(constructZipCodeUrl(addr1,addr2,city,state).replaceAll(" ","%20")));
			
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		return null;
	}
		
	public static Object parseStream(URL url) throws Exception {
		
		String packageName = "generated.usps";
		JAXBContext jc = JAXBContext.newInstance( packageName );
		Unmarshaller u = jc.createUnmarshaller();
		Object gr = u.unmarshal(url);
		   
		return gr;
	}
}
