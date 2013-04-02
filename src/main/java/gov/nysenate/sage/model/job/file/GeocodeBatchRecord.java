package gov.nysenate.sage.model.job.file;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Geocode;

/**
 * Represents a record of a GeocodeBatchFile.
 */
public class GeocodeBatchRecord
{
    protected String street;
    protected String city;
    protected String state;
    protected String zip5;
    protected String zip4;
    protected String geoMethod;
    protected double lat;
    protected double lon;
    protected String quality;
    protected int rawQuality;

    /**
     * Convenience method to apply data from a GeocodedAddress.
     * @param geoAddress
     */
    public void setGeocodedAddress(GeocodedAddress geoAddress)
    {
        if (geoAddress != null) {
            this.setAddress(geoAddress.getAddress());
            this.setGeocode(geoAddress.getGeocode());
        }
    }

    /**
     * Apply address model data into the batch record.
     * @param address   The address to get data from
     */
    public void setAddress(Address address)
    {
        if (address != null) {
            this.setStreet(address.getAddr1());
            this.setCity(address.getCity());
            this.setState(address.getState());
            this.setZip5(address.getZip5());
            this.setZip4(address.getZip4());
        }
    }

    /**
     * Apply geocode model data into the batch record.
     * @param geocode
     */
    public void setGeocode(Geocode geocode)
    {
        if (geocode != null) {
            this.setLat(geocode.getLat());
            this.setLon(geocode.getLon());
            this.setGeoMethod(geocode.getMethod());
            this.setQuality(geocode.getQuality().name());
            this.setRawQuality(geocode.getRawQuality());
        }
    }

    /**
     * Constructs and returns a new Address model based on the address information in this record.
     * @return Address
     */
    public Address toAddress() {
        return new Address(street, "", city, state, zip5, zip4);
    }

    /** Normal Getters/Setters below */

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip5() {
        return zip5;
    }

    public void setZip5(String zip5) {
        this.zip5 = zip5;
    }

    public String getZip4() {
        return zip4;
    }

    public void setZip4(String zip4) {
        this.zip4 = zip4;
    }

    public String getGeoMethod() {
        return geoMethod;
    }

    public void setGeoMethod(String geoMethod) {
        this.geoMethod = geoMethod;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }

    public int getRawQuality() {
        return rawQuality;
    }

    public void setRawQuality(int rawQuality) {
        this.rawQuality = rawQuality;
    }
}
