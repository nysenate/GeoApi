package connectors;

import generated.yahoo.*;
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
 * used to connect to Yahoo maps API to geocode and reverse geocode, models for
 * response are located in package "generated.yahoo"
 */
public class YahooConnect extends ParseStream implements AbstractGeocoder,AbstractReverseGeocoder {
	private final String YH_API = "http://where.yahooapis.com/geocode?q=";
	private final String YH_KEY = "&appid=";
	private final String YH_APPID = "yahoo.consumerkey";
	private final String YH_REVERSE_API = "http://where.yahooapis.com/geocode?location=";
	private final String YH_REVERSE_FLAGS = "&gflags=R";
	static String PACKAGE = "generated.yahoo";
	
	public YahooConnect() {
		super(PACKAGE);
	}
	
	public Point doParsing(String address) {
		ResultSet rs = null;
		
		try {
			rs = (ResultSet)parseStream(new URL(constructUrl(address)));
			
		}
		catch (Exception e) {
			e.printStackTrace();
			
			return null;
		}
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
				+ Resource.get(YH_APPID);
		}
		else {
			return YH_REVERSE_API 
				+ lat 
				+ "+" 
				+ (lng.startsWith("-") ? lng : "+" + lng) 
				+ YH_REVERSE_FLAGS 
				+ YH_KEY
				+ Resource.get(YH_APPID);
		}
	}

	public String constructUrl(String value) {
		return YH_API + value + YH_KEY + Resource.get(YH_APPID);
	}
}
