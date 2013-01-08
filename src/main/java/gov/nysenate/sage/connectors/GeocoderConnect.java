package gov.nysenate.sage.connectors;


import generated.geocoder.GeocoderResult;
import generated.geocoder.GeocoderResults;
import gov.nysenate.sage.model.Point;
import gov.nysenate.sage.model.abstracts.AbstractGeocoder;
import gov.nysenate.sage.util.Config;

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

	private final String GEO_BASE = Config.read("geocoder.url");
	private final String GEO_BASE_BULK = Config.read("geocoder_bulk.url");

	public GeocoderConnect() {

	}

	public Point doParsing(String address) {
		return getPoint(constructUrl(address));
	}

	public Point doParsing(String addr, String city, String state, String zip4, String zip5) {
		return getPoint(constructUrl(addr, city, state, zip4, zip5));
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
		return gson.fromJson("{\"geocoderResults\":" + json + "}", GeocoderResults.class);
	}

	public String constructUrl(String address) {
		return GEO_BASE + "address=" + address;
	}

	public String constructUrl(String addr, String city, String state, String zip4, String zip5) {
		return GEO_BASE + "street=" + addr + "&city=" + city + "&state=" + state + "&zip=" + zip5;
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

		ArrayList<Point> points = new ArrayList<Point>();
		for(GeocoderResult gr:results) {
			try {
				Point point = new Point(new Double(gr.getLat()), new Double(gr.getLon()));
				points.add(point);
			}
			catch (Exception e) {
				points.add(new Point(-1, -1));
			}
		}
		return points;

	}

	public String constructBulkUrl(String json) throws UnsupportedEncodingException {
		return GEO_BASE_BULK + "json=" + URLEncoder.encode(json, "utf-8");
	}
}
