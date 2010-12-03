package model;

import model.districts.Senate;

public class SenateMapInfo {
	double lat;
	double lon;
	public double zoom;
	
	Senate senate;
	
	public SenateMapInfo() {
		
	}
	
	public SenateMapInfo(double lat, double lon, double zoom, Senate senate) {
		this.lat = lat;
		this.lon = lon;
		this.zoom = zoom;
		this.senate = senate;
	}
}
