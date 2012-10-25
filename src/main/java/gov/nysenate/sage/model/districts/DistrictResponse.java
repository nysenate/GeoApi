package gov.nysenate.sage.model.districts;

import gov.nysenate.sage.Response;

import java.util.ArrayList;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("districts")
public class DistrictResponse extends Response {
    public boolean validated;
    public boolean geocoded;
    public boolean distassigned;

    public ArrayList<String> errors;

	double lat;
	double lon;
	public double geocode_quality;

	// Simple
	String address;

	//Extended
	public String address1;
	public String address2;
	public String city;
	public String state;
	public String zip5;
	public String zip4;

	Assembly assembly;
	Congressional congressional;
	County county;
	Election election;
	Senate senate;
	School school;
	Town town;

	//Census census;

	public DistrictResponse() {
	    this.errors = new ArrayList<String>();
	}

	public double getLat() {
		return lat;
	}

	public double getLon() {
		return lon;
	}

	public Object getAddress() {
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

	public School getSchool() {
	    return school;
	}

	public Town getTown() {
	    return town;
	}

	/*public Census getCensus() {
		return census;
	}
*/
	public void setLat(double lat) {
		this.lat = lat;
	}

	public void setLon(double lon) {
		this.lon = lon;
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

	public void setSchool(School school) {
	    this.school = school;
	}

	public void setTown(Town town) {
	    this.town = town;
	}

    public boolean isDistassigned() {
        return distassigned;
    }

    public void setDistassigned(boolean distassigned) {
        this.distassigned = distassigned;
    }

    public boolean isGeocoded() {
        return geocoded;
    }

    public void setGeocoded(boolean geocoded) {
        this.geocoded = geocoded;
    }

    public boolean isValidated() {
        return validated;
    }

    public void setValidated(boolean validated) {
        this.validated = validated;
    }

    public void setAddress(String address) {
        this.address = address;
    }

	/*public void setCensus(Census census) {
		this.census = census;
	}*/



}
