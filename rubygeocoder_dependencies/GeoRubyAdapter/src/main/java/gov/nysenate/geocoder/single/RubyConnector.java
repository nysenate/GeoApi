package gov.nysenate.geocoder.single;

import gov.nysenate.geocoder.bulk.model.RequestObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;

public class RubyConnector {
	private String url;
	
	public RubyConnector() {
		url = "http://localhost:4567/geo_codes?";
	}
	
	public RubyConnector(String url) {
		this.url = url;
	}
	
	public String getResult(HttpServletRequest request) {
		String uri = null;
		String response = null;
		
		String address = request.getParameter("address");
		if(address != null) {
			uri = url + "?address=" + address;
		}
		else {
			String number = request.getParameter("number");;
			String street = request.getParameter("street");;
			String city = request.getParameter("city");;
			String state = request.getParameter("state");;
			String zip = request.getParameter("zip");
			
			if((number == null && street == null) || street == null || city == null || state == null || zip == null) {
				uri = null;
			}
			else {
				uri = url + (number == null || number.equals("") ? "":"number=" + number + "&")+ "street=" + RequestObject.fixStreet(street) +
					"&city=" + city + "&state=" + state + "&zip=" + zip;
			}
		}
		if(uri != null) {
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(new URL(uri.replaceAll(" ","%20")).openStream()));
				String in = null;
				while((in = br.readLine()) != null) {
					response = (response == null ? in : response + "\n" + in);
				}
				br.close();
			}
			catch (Exception e) {
				uri = null;
				response = e.getMessage();
			}
			
		}
		
		if(response != null && !response.equals("")) {
			return response;
		}
		else {
			return "[{\"error\":\"error\"}]";
		}
	}
}
