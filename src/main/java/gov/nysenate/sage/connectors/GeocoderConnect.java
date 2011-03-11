package gov.nysenate.sage.connectors;


import generated.geocoder.GeocoderResult;
import generated.geocoder.GeocoderResults;
import gov.nysenate.sage.model.Point;
import gov.nysenate.sage.model.abstracts.AbstractGeocoder;
import gov.nysenate.sage.util.Resource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
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
	private final String GEO_BASE_BULK = Resource.get("geocoder_bulk.url");
	
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
					new URL(url.replaceAll(" ", "%20").replaceAll("#", "%23")).openStream()));
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
		return GEO_BASE + (num == null || num.equals("") ? "":"number=" + num + "&")+ "street=" + street + "&city=" + city + "&state=" + state + "&zip=" + zip;
	}
	
	public List<Point> doBulkParsing(String json) throws UnsupportedEncodingException {
		return getBulkPoints(constructBulkUrl(json));
	}
	
	public List<Point> getBulkPoints(String url) {
		BufferedReader geo = null;
		String json = null;
		try {
			geo = new BufferedReader(new InputStreamReader(
					new URL(url).openStream()));
			json = geo.readLine();
			geo.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
						
		GeocoderResults result = getGeocoderResults("[" + json + "]");
		
		List<GeocoderResult> results = result.getGeocoderResults();
				
		try {
			ArrayList<Point> points = new ArrayList<Point>();
			for(GeocoderResult gr:results) {
				points.add(new Point(new Double(gr.getLat()), new Double(gr.getLon())));
			}
			return points;
		}
		catch (Exception e) {
			return null;
		}
	}
	
	public String constructBulkUrl(String json) throws UnsupportedEncodingException {
		return GEO_BASE_BULK + "json=" + URLEncoder.encode(json, "utf-8");
	}
}
