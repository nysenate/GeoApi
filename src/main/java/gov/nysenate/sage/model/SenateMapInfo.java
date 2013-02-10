package gov.nysenate.sage.model;

import gov.nysenate.sage.deprecated.districts.Senate;

public class SenateMapInfo {
	double lat;
	double lon;
	public double zoom;
	
	public Double offsetLat;
	public Double offsetLon;
	
	Senate senate;
	
	public SenateMapInfo() {
		
	}
	
	public SenateMapInfo(double lat, double lon, double zoom, Double offsetLat,
			Double offsetLon, Senate senate) {
		this.lat = lat;
		this.lon = lon;
		this.offsetLat = offsetLat;
		this.offsetLon = offsetLon;
		this.zoom = zoom;
		this.senate = senate;
	}
}
