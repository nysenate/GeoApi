package generated.geocoder;

public class GeocoderResult {
	
	String number;
	String street;
	String city;
	String state;
	String zip;
	
	String lat;
	String lon;
	String fips_county;
	
	String score;
	String str_scr;
	String prenum;
	String procesion;
	
	public GeocoderResult () {
		
	}

	public String getNumber() {
		return number;
	}

	public String getStreet() {
		return street;
	}

	public String getCity() {
		return city;
	}

	public String getState() {
		return state;
	}

	public String getZip() {
		return zip;
	}

	public String getLat() {
		return lat;
	}

	public String getLon() {
		return lon;
	}

	public String getFips_county() {
		return fips_county;
	}

	public String getScore() {
		return score;
	}

	public String getStr_scr() {
		return str_scr;
	}

	public String getPrenum() {
		return prenum;
	}

	public String getProcesion() {
		return procesion;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public void setState(String state) {
		this.state = state;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public void setLat(String lat) {
		this.lat = lat;
	}

	public void setLon(String lon) {
		this.lon = lon;
	}

	public void setFips_county(String fipsCounty) {
		fips_county = fipsCounty;
	}

	public void setScore(String score) {
		this.score = score;
	}

	public void setStr_scr(String strScr) {
		str_scr = strScr;
	}

	public void setPrenum(String prenum) {
		this.prenum = prenum;
	}

	public void setProcesion(String procesion) {
		this.procesion = procesion;
	}

	@Override
	public String toString() {
		return number + ", " + street + ", " + city + ", " + state + ", " + zip
				+ ", " + lat + ", " + lon + ", " + fips_county + ", " + score
				+ ", " + str_scr + ", " + prenum + ", " + procesion;
	}
	
	
}
