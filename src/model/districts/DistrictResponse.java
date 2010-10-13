package model.districts;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("districts")
public class DistrictResponse {
	double lat;
	double lon;
	String address;
	Assembly assembly;
	Congressional congressional;
	County county;
	Election election;
	Senate senate;
	Census census;
	
	public DistrictResponse() {
		
	}

	public double getLat() {
		return lat;
	}

	public double getLon() {
		return lon;
	}

	public String getAddress() {
		return address;
	}

	public Assembly getAssembly() {
		return assembly;
	}

	public Congressional getCongressional() {
		return congressional;
	}

	public County getCounty() {
		return county;
	}

	public Election getElection() {
		return election;
	}

	public Senate getSenate() {
		return senate;
	}
	
	public Census getCensus() {
		return census;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setAssembly(Assembly assembly) {
		this.assembly = assembly;
	}

	public void setCongressional(Congressional congressional) {
		this.congressional = congressional;
	}

	public void setCounty(County county) {
		this.county = county;
	}

	public void setElection(Election election) {
		this.election = election;
	}

	public void setSenate(Senate senate) {
		this.senate = senate;
	}
	
	public void setCensus(Census census) {
		this.census = census;
	}
	

	
}
