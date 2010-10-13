package model;

import com.thoughtworks.xstream.annotations.XStreamAlias;;

@XStreamAlias("point")
public class Point {
	public double lat;
	public double lon;
	
	public String address;
	
	public Point(double x, double y, String address) {
		this.lat = x;
		this.lon = y;
		this.address = address;
	}
	
	public static void main(String[] args) {
		System.out.println(1);
	}
}
