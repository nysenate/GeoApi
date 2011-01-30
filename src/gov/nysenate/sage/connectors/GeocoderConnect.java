package gov.nysenate.sage.connectors;


import generated.geocoder.GeocoderResult;
import generated.geocoder.GeocoderResults;
import gov.nysenate.sage.model.Point;
import gov.nysenate.sage.model.abstracts.AbstractGeocoder;
import gov.nysenate.sage.util.Resource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

import com.google.gson.Gson;



/**
 * @author Jared Williams
 * 
 * used to connect to Google maps API to geocode and reverse geocode, models for
 * response are located in package "generated.google"
 */
public class GeocoderConnect implements AbstractGeocoder {
	
	private final String GEO_BASE = Resource.get("geocoder.url");	
	
	public GeocoderConnect() {

	}
	
	public Point doParsing(String address) {
		return getPoint(constructUrl(address));
	}
	
	public Point doParsing(String num, String street, String city, String state, String zip) {
		return getPoint(constructUrl(num, street, city, state, zip));
	}
	
	public Point getPoint(String url) {		
		BufferedReader geo = null;
		String json = null;
		try {
			geo = new BufferedReader(new InputStreamReader(
					new URL(url.replaceAll(" ", "%20")).openStream()));
			json = geo.readLine();
			geo.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
		GeocoderResults result = getGeocoderResults(json);
		
		List<GeocoderResult> results = result.getGeocoderResults();
		
		GeocoderResult point =  null;
		
		try {
			point = results.iterator().next();
			return new Point(new Double(point.getLat()), new Double(point.getLon()));
		}
		catch (Exception e) {
			return null;
		}
	}
	
	public GeocoderResults getGeocoderResults(String json) {
		Gson gson = new Gson();
		return (GeocoderResults)gson.fromJson("{\"geocoderResults\":" + json + "}", GeocoderResults.class);
	}
	
	public String constructUrl(String address) {
		return GEO_BASE + "address=" + address;
	}
	
	public String constructUrl(String num, String street, String city, String state, String zip) {
		return GEO_BASE + "number=" + num + "&street=" + street + "&city=" + city + "&state=" + state + "&zip=" + zip;
	}
}
