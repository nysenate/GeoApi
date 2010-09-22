package control;

import java.util.List;

import com.google.gson.Gson;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import connectors.BingConnect;
import connectors.GoogleConnect;
import connectors.YahooConnect;

import model.Point;

/**
 * @author Jared Williams
 *
 * This class processes geocoding and reverse geocoding requests
 */
public class GeoCode {
	
	static final String GOOGLE = "google";
	static final String YAHOO = "yahoo";
	static final String BING = "bing";
	
	/**
	 * if service is null google, yahoo and bing are attempted, if service has a value it only attempts
	 * the service specified and returns the point retrieved from the given api
	 * 
	 * address -> lat,lon
	 */
	public static Point getGeoCodedResponse(String address, String service) {
		Point p = null;
		
		if(serviceOrNull(YAHOO,service) && (p = new YahooConnect().doParsing(address)) != null) {
			return p;
			
		}
		else if(serviceOrNull(GOOGLE,service) && (p = new GoogleConnect().doParsing(address)) != null) {
			return p;
			
		}
		else if(serviceOrNull(BING,service) && (p = new BingConnect().doParsing(address)) != null) {
			return p;
			
		}
		
		return null;
	}
	
	/**
	 * used to avoid cluttering if statements with redundant parameters
	 */
	public static boolean serviceOrNull(String service, String given) {
		if(given == null || service.equals(given)) {
			return true;
			
		}
		return false;
	}
	
	/**
	 * same structure as getGeoCodedResponse but using reverse geocoding, i.e. lat,lon -> address
	 */
	public static List<Point> getReverseGeoCodedResponse(String latlng, String service) {
		List<Point> points = null;
		
		if(serviceOrNull(YAHOO,service) && !(points = new YahooConnect().doReverseParsing(latlng)).isEmpty()) {
			return points;
			
		}
		else if(serviceOrNull(GOOGLE,service) && !(points = new GoogleConnect().doReverseParsing(latlng)).isEmpty()) {
			return points;
			
		}
		else if(serviceOrNull(BING,service) && !(points = new BingConnect().doReverseParsing(latlng)).isEmpty()) {
			return points;
			
		}
		
		return null;
	}
	
	/**
	 * takes parameters, calls getGeoCodeResponse then returns string in desired format
	 */
	public static String getApiGeocodeResponse(String address, String format, String service) throws Exception {
		Gson gson = new Gson();
		
		Point p = getGeoCodedResponse(address, service);
		
		if(p == null) {
			throw new Exception();
		}
		
		if(format.equals("xml")) {
			XStream xstream = new XStream(new DomDriver());
			xstream.processAnnotations(Point.class);
			return xstream.toXML(p);
		}
		return gson.toJson(p);
	}
	
	/**
	 * takes parameters, calls getReverseGeoCodeResponse then returns string in desired format
	 */
	public static String getApiReverseGeocodeResponse(String latlng, String format, String service) throws Exception {
		Gson gson = new Gson();
		
		List<Point> points = getReverseGeoCodedResponse(latlng, service);
		
		if(points == null) {
			throw new Exception();
		}
		
		if(format.equals("xml")) {
			XStream xstream = new XStream(new DomDriver());
			xstream.processAnnotations(Point.class);
			return xstream.toXML(points);
		}
		return gson.toJson(points);
	}
}
