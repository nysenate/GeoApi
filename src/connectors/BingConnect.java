package connectors;

import generated.bing.Location;
import generated.bing.Response;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import control.Resource;
import model.ParseStream;
import model.Point;
import model.abstracts.AbstractGeocoder;
import model.abstracts.AbstractReverseGeocoder;

/**
 * @author Jared Williams
 * 
 * used to connect to Bing maps API to geocode and reverse geocode, models for
 * response are located in package "generated.bing"
 */
public class BingConnect extends ParseStream implements AbstractGeocoder,AbstractReverseGeocoder {
	private final String BING_API = "http://dev.virtualearth.net/REST/v1/Locations/";
	private final String BING_KEY = "?o=xml&key=";
	private final String BING_API_END = "bing.key";
	private final String BING_CS = "US/NY/";
	static String PACKAGE = "generated.bing";
	
	public BingConnect() {
		super(PACKAGE);
	}
	
	/**
	 * Used for geocoding, handles generated JAXB content and returns point
	 * with necessary data
	 * 
	 * @param address is the address being searched
	 * @return point with lat, lon and address
	 */
	public Point doParsing(String address) {
		Response r = null;
		
		try {
			r = (Response)parseStream(new URL(constructUrl(address)));
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
			
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		Location l = r.getResourceSets().getResourceSet().getResources().getLocation().iterator().next();
		
		return new Point(l.getPoint().getLatitude(),
				l.getPoint().getLongitude(),
				l.getAddress().getFormattedAddress());
	}
	
	/**
	 * Used for reverse geocoding, handles generated JAXB content and returns point
	 * with necessary data
	 * 
	 * @param address is the address being searched
	 * @return point with lat, lon and address
	 */
	public List<Point> doReverseParsing(String latlng) {
		List<Point> points = new ArrayList<Point>();
		
		Response r = null;
		
		try {
			r = (Response)parseStream(new URL(constructReverseUrl(latlng)));
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
			
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		
		for(Location l:r.getResourceSets().getResourceSet().getResources().getLocation()) {
			points.add(new Point(l.getPoint().getLatitude(),
					l.getPoint().getLongitude(),
					l.getAddress().getFormattedAddress()));
		
		}
		return points;
	}
	
	/**
	 * constructs the restful url that will make the api call
	 * 
	 * @param address is the address being searched
	 * @returns a string containing the formatted url
	 */
	public String constructUrl(String address) {
		String[] strings = address.split(",");
		
		if(strings.length != 3) {
			return null;
		}
		return (BING_API
				+ BING_CS
				+ strings[2].replaceAll("[a-zA-Z]","").trim() 
				+ "/" + strings[1].trim() 
				+ "/" + strings[0].trim() 
				+ BING_KEY 
				+ Resource.get(BING_API_END)).replaceAll(" ","%20");
	}
	
	/**
	 * constructs the restful url that will make the api cal
	 * 
	 * @param latlon lat,lon being searched
	 * @returns a string containing the formatted url
	 */
	public String constructReverseUrl(String latlon) {
		return BING_API + latlon + BING_KEY + Resource.get(BING_API_END);
	}
}


