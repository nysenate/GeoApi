package gov.nysenate.sage.model.address;

import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.util.StreetAddressParser;

public class NYSGeoAddress {

    private String addresslabel = "";
    private String citytownname = "";
    private String state = "";
    private String zipcode = "";
    private double latitude = 0.0;
    private double longitude = 0.0;
    private int pointtype = 0;

    public NYSGeoAddress() {}

    public NYSGeoAddress(String addresslabel, String citytownname, String state, String zipcode, double latitude, double longitude, int pointtype) {
        this.addresslabel = addresslabel;
        this.citytownname = citytownname;
        this.state = state;
        this.zipcode = zipcode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.pointtype = pointtype;
    }

    public Address toAddress() {
        return new Address(addresslabel, citytownname, state, zipcode);
    }

    public StreetAddress toStreetAddress() {
        return StreetAddressParser.parseAddress(this.toAddress());
    }

    public Geocode toGeocode() {
        return new Geocode(new Point(getLatitude(),getLongitude()),GeocodeQuality.HOUSE, "NYS Geo DB");
    }

    public String getAddresslabel() {
        return addresslabel;
    }

    public void setAddresslabel(String addresslabel) {
        this.addresslabel = addresslabel;
    }

    public String getCitytownname() {
        return citytownname;
    }

    public void setCitytownname(String citytownname) {
        this.citytownname = citytownname;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getPointtype() {
        return pointtype;
    }

    public void setPointtype(int pointtype) {
        this.pointtype = pointtype;
    }
}
