package gov.nysenate.sage.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;;

@XStreamAlias("point")
public class Point {
	public double lat;
	public double lon;
	
	public String address;
	
	public Point(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
	}
	
	public Point(double lat, double lon, String address) {
		this.lat = lat;
		this.lon = lon;
		this.address = address;
	}
	
	
}
