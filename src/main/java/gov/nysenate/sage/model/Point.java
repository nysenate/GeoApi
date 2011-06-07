package gov.nysenate.sage.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;;

@XStreamAlias("point")
public class Point {
	public double lat;
	public double lon;
	
	public Object address;
	
	public Point(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
	}
	
	public Point(double lat, double lon, String address) {
		this.lat = lat;
		this.lon = lon;
		this.address = address;
	}
	
	static abstract class AddressInterface {
		String address;
		
		public AddressInterface(String address) {
			this.address = address;
		}
		
		public String getAddress() {
			return address;
		}
		
		public void setAddress() {
			
		}
	}
	
	static class SimpleAddress extends AddressInterface {
		public SimpleAddress(String address) {
			super(address);
		}
	}
	
	static class ExtendedAddress {
		
	}
}
