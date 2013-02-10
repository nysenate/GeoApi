package gov.nysenate.sage.connectors;

import generated.yahoo.ResultSet;
import gov.nysenate.sage.model.ParseStream;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.deprecated.abstracts.AbstractGeocoder;
import gov.nysenate.sage.deprecated.abstracts.AbstractReverseGeocoder;
import gov.nysenate.sage.util.Config;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jared Williams
 *
 * used to connect to Yahoo maps API to geocode and reverse geocode, models for
 * response are located in package "generated.yahoo"
 */
@Deprecated
public class YahooConnect extends ParseStream implements AbstractGeocoder,AbstractReverseGeocoder {
	private final String YH_API = "http://where.yahooapis.com/geocode?q=";
	private final String YH_KEY = "&appid=";
	private final String YH_APPID = Config.read("yahoo.key");
	private final String YH_REVERSE_API = "http://where.yahooapis.com/geocode?location=";
	private final String YH_REVERSE_FLAGS = "&gflags=R";
	static String PACKAGE = "generated.yahoo";

	public YahooConnect() {
		super(PACKAGE);
	}

	public Point doParsing(String addr, String city, String state, String zip4, String zip5) {
		return doParsing(GeoCode.getExtendedAddress(addr, city, state, zip4, zip5));
	}

	public Point doParsing(String address) {
		ResultSet rs = null;
		address = this.cleanUrl(address);
		try {
			rs = (ResultSet)parseStream(new URL(constructUrl(address)));

		}
		catch (Exception e) {
			e.printStackTrace();

			return null;
		}

		if(rs.getResult() ==  null)
			return null;

		return new Point(rs.getResult().getLatitude(),
				rs.getResult().getLongitude(),
				rs.getResult().getLine1() + " " + rs.getResult().getLine2());
	}

	public List<Point> doReverseParsing(String latlon) {
		List<Point> points = new ArrayList<Point>();
		ResultSet rs = null;

		try {
			rs = (ResultSet)parseStream(new URL(constructReverseUrl(latlon)));

		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		points.add(new Point(rs.getResult().getLatitude(),
				rs.getResult().getLongitude(),
				rs.getResult().getLine1() + " " + rs.getResult().getLine2()));

		return points;
	}

	public String constructReverseUrl(String latlon) {
		String lat = latlon.split(",")[0];
		String lng = latlon.split(",")[1];
		if(lat.startsWith("-")) {
			lat.replaceAll("^-", "");
			return YH_REVERSE_API
				+ lat
				+ "-"
				+ (lng.startsWith("-") ? lng : "+" + lng)
				+ YH_REVERSE_FLAGS
				+ YH_KEY
				+ YH_APPID;
		}
		else {
			return YH_REVERSE_API
				+ lat
				+ "+"
				+ (lng.startsWith("-") ? lng : "+" + lng)
				+ YH_REVERSE_FLAGS
				+ YH_KEY
				+ YH_APPID;
		}
	}

	public String constructUrl(String value) {
		return YH_API + value + YH_KEY + YH_APPID;
	}

	public String cleanUrl(String url) {
		return url.replaceAll("&", "%26").replaceAll("#", "%23").replaceAll("/", "%2F");
	}
}
