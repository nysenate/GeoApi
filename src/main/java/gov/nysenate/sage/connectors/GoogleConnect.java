package gov.nysenate.sage.connectors;

import generated.google.GeocodeResponse;
import generated.google.Location;
import generated.google.Result;
import gov.nysenate.sage.model.ParseStream;
import gov.nysenate.sage.model.Point;
import gov.nysenate.sage.model.abstracts.AbstractGeocoder;
import gov.nysenate.sage.model.abstracts.AbstractReverseGeocoder;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jared Williams
 * 
 * used to connect to Google maps API to geocode and reverse geocode, models for
 * response are located in package "generated.google"
 */
public class GoogleConnect extends ParseStream implements AbstractGeocoder,AbstractReverseGeocoder  {
	
	private final  String GM_SENSOR = "&sensor=false";
	private final  String GM_ADDRESS = "address=";
	private final  String GM_API = "http://maps.google.com/maps/api/geocode/xml?";
	private final  String GM_REVERSE_LATLNG = "latlng=";
	static String PACKAGE = "generated.google";
	
	public GoogleConnect() {
		super(PACKAGE);
	}
	
	public Point doParsing(String value) {
		GeocodeResponse gr = null;
		
		try {
			gr = (GeocodeResponse)parseStream(new URL(constructUrl(value)));
			
		} catch (Exception e) {
			e.printStackTrace();
			
			return null;
			
		}
		Result r = gr.getResult().iterator().next();
		
		return new Point(r.getGeometry().getLocation().getLat(), 
				r.getGeometry().getLocation().getLng(),
				r.getFormattedAddress());
	}
	
	public List<Point> doReverseParsing(String latlng) {
		List<Point> points = new ArrayList<Point>();
		GeocodeResponse gr = null;
		
		try {
			gr = (GeocodeResponse)parseStream(new URL(constructReverseUrl(latlng)));
			
		} catch (Exception e) {
			e.printStackTrace();
			
			return null;
			
		}
		for(Result r:gr.getResult()) {
			Location location = r.getGeometry().getLocation();
			points.add(new Point(location.getLat(),
					location.getLng(),
					r.getFormattedAddress()));
		}
		return points;
	}
	
	public String constructUrl(String value) {
		return GM_API + GM_ADDRESS + value + GM_SENSOR;
	}
	
	public String constructReverseUrl(String latlng) {
		return GM_API + GM_REVERSE_LATLNG + latlng + GM_SENSOR;
	}
}
