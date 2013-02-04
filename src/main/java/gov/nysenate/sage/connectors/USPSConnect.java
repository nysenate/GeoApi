package gov.nysenate.sage.connectors;

import generated.usps.AddressValidateResponse;
import generated.usps.CityStateLookupResponse;
import generated.usps.Error;
import generated.usps.ZipCodeLookupResponse;
import gov.nysenate.sage.model.ErrorResponse;
import gov.nysenate.sage.model.ValidateResponse;
import gov.nysenate.sage.util.Config;

import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

/**
 * @author Jared Williams
 *
 * used to connect to USPS AMS api and retrieve valided addresses, zip codes or retrieve
 * information based on zip codes
 */
@Deprecated
public class USPSConnect {

	public static final String API_ID = Config.read("usps.key");
	public static final String API_BASE = "http://production.shippingapis.com/ShippingAPI.dll?API=";
	public static final String API_FORMAT = "&XML=";
	public static final String API_VERIFY = "Verify";
	public static final String API_CITYSTATE = "CityStateLookup";
	public static final String API_ZIPCODE = "ZipCodeLookup";

	public static Object validateAddress(String addr1, String addr2,
			String city, String state, String zip5, String zip4, String punctuation) {

		if(nOE(addr2) || nOE(city)){
			return null;
		}

		Object o = getResponse(constructAddressUrl(addr1,addr2,city,state,zip5,zip4).replaceAll(" ","%20"));

		if(o instanceof AddressValidateResponse) {
			AddressValidateResponse avr = (AddressValidateResponse)o;

			if(avr.getAddress().getError() != null) {
				o = new ErrorResponse(avr.getAddress().getError().getDescription());
			}
			else {
				o = new ValidateResponse(avr, punctuation);
			}

			return o;
		}
		else if(o instanceof Error) {
			return new ErrorResponse(((Error)o).getDescription());
		}
		return o;
	}

	public static Object getCityStateByZipCode(String zip5) {

		Object o = getResponse(constructCityStateUrl(zip5).replaceAll(" ","%20"));

		CityStateLookupResponse cslr = (CityStateLookupResponse)o;

		if(cslr.getZipCode().getError() != null) {
			o = new ErrorResponse(cslr.getZipCode().getError().getDescription());
		}
		else {
			o = new ValidateResponse(cslr);
		}

		return o;
	}

	private static boolean nOE(String val) {
		if(val == null || val.matches("\\s*"))
			return true;
		return false;
	}

	public static Object getZipByAddress(String addr1, String addr2, String city, String state) {

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

		return o;
	}

	private static String isNull(String s) {
		return (s == null) ? "" : s;
	}

	public static String constructAddressUrl(String addr1, String addr2,
				String city, String state, String zip5, String zip4) {

		return API_BASE
		+ API_VERIFY
		+ API_FORMAT
		+ "<AddressValidateRequest USERID=\"" + API_ID + "\">"
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
		return API_BASE
			+ API_CITYSTATE
			+ API_FORMAT
			+ "<CityStateLookupRequest USERID=\"" + API_ID + "\">"
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

		return API_BASE
		+ API_ZIPCODE
		+ API_FORMAT
		+ "<ZipCodeLookupRequest USERID=\"" + API_ID + "\">"
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
