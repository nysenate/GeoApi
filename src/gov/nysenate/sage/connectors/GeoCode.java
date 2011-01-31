package gov.nysenate.sage.connectors;

import gov.nysenate.sage.model.Point;
import java.util.List;

/**
 * @author Jared Williams
 * 
 *         This class processes geocoding and reverse geocoding requests
 */
public class GeoCode {

	static final String GOOGLE = "google";
	static final String YAHOO = "yahoo";
	static final String BING = "bing";
	static final String GEOCODER = "geocoder";

	/**
	 * if service is null google, yahoo and bing are attempted, if service has a
	 * value it only attempts the service specified and returns the point
	 * retrieved from the given api
	 * 
	 * address -> lat,lon
	 */
	public static Point getGeoCodedResponse(String address, String service) {
		Point p = null;

		if (serviceOrNull(YAHOO, service)
				&& (p = new YahooConnect().doParsing(address)) != null) {
			return p;

		} else if (serviceOrNull(GOOGLE, service)
				&& (p = new GoogleConnect().doParsing(address)) != null) {
			return p;

		} else if (serviceOrNull(BING, service)
				&& (p = new BingConnect().doParsing(address)) != null) {
			return p;

		} else if (serviceOrNull(GEOCODER, service)
				&& (p = new GeocoderConnect().doParsing(address)) != null) {
			return p;

		}

		return null;
	}

	/**
	 * used to avoid cluttering if statements with redundant parameters
	 */
	public static boolean serviceOrNull(String service, String given) {
		if (given == null || service.equals(given)) {
			return true;

		}
		return false;
	}

	/**
	 * same structure as getGeoCodedResponse but using reverse geocoding, i.e.
	 * lat,lon -> address
	 */
	public static List<Point> getReverseGeoCodedResponse(String latlng,
			String service) {
		List<Point> points = null;

		if (serviceOrNull(YAHOO, service) 
				&& !(points = new YahooConnect().doReverseParsing(latlng)).isEmpty()) {
			return points;

		} else if (serviceOrNull(GOOGLE, service) 
				&& !(points = new GoogleConnect().doReverseParsing(latlng)).isEmpty()) {
			return points;

		} else if (serviceOrNull(BING, service)	
				&& !(points = new BingConnect().doReverseParsing(latlng)).isEmpty()) {
			return points;

		}

		return null;
	}

	/**
	 * takes parameters, calls getGeoCodeResponse
	 */
	public static Point getApiGeocodeResponse(String address, String service) throws Exception {		
		Point p = getGeoCodedResponse(address, service);

		if (p == null) {
			throw new Exception();
		}

		return p;
	}

	/**
	 * takes parameters, calls getReverseGeoCodeResponse
	 */
	public static List<Point> getApiReverseGeocodeResponse(String latlng, String service) throws Exception {
		List<Point> points = getReverseGeoCodedResponse(latlng, service);

		if (points == null) {
			throw new Exception();
		}

		return points;
	}

	public static Point getApiGeocodeResponse(String number, String addr2,
			String city, String state, String zip4, String zip5,
			String service) throws Exception {

		if (service != null && service.equalsIgnoreCase(GEOCODER)) {
			Point point = new GeocoderConnect().doParsing(number, addr2, city,
					state, zip5);

			if (point != null)
				return point;

			service = null;
		}
		
		addr2 = (number != null ? number + " " + addr2 : addr2);
		return getApiGeocodeResponse(getExtendedAddress(addr2, city, state,
				zip4, zip5), service);
	}

	public static String getExtendedAddress(String addr, String city,
			String state, String zip4, String zip5) {
		return (addr != null ? addr : "") + ",%20" + (city != null ? city : "")
				+ ",%20" + (state != null ? state : "") + "%20"
				+ (zip5 != null ? zip5 : "") + (zip4 != null ? "-" + zip4 : "");
	}
}
