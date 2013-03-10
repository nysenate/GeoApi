package gov.nysenate.sage.model.address;

import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictQuality;
import gov.nysenate.sage.model.geo.Geocode;

import java.io.Serializable;

/**
 * Represents an address with district information.
 */
public class DistrictedAddress implements Serializable, Cloneable
{
    protected GeocodedAddress geocodedAddress;
    protected DistrictInfo districtInfo;
    protected DistrictQuality districtQuality = DistrictQuality.NOMATCH;

    public DistrictedAddress() {}

    public DistrictedAddress(GeocodedAddress geocodedAddress, DistrictInfo districtInfo)
    {
        this(geocodedAddress, districtInfo, DistrictQuality.NOMATCH);
    }

    public DistrictedAddress(GeocodedAddress geocodedAddress, DistrictInfo districtInfo, DistrictQuality districtQuality)
    {
        this.geocodedAddress = geocodedAddress;
        this.districtInfo = districtInfo;
        this.districtQuality = districtQuality;
    }

    public GeocodedAddress getGeocodedAddress()
    {
        return this.geocodedAddress;
    }

    public void setGeocodedAddress(GeocodedAddress geocodedAddress)
    {
        this.geocodedAddress = geocodedAddress;
    }

    /** Convenience method to access the underlying Address object */
    public Address getAddress()
    {
        if (this.getGeocodedAddress() != null && this.getGeocodedAddress().getAddress() != null) {
            return this.getGeocodedAddress().getAddress();
        }
        return null;
    }

    /** Convenience method to set the underlying Address object */
    public void setAddress(Address address)
    {
        if (this.getGeocodedAddress() != null){
            this.getGeocodedAddress().setAddress(address);
        }
        else {
            this.geocodedAddress = new GeocodedAddress(address);
        }
    }

    public void setGeocode(Geocode geocode)
    {
        if (this.getGeocodedAddress() != null){
            this.getGeocodedAddress().setGeocode(geocode);
        }
        else {
            this.setGeocodedAddress(new GeocodedAddress(null, geocode));
        }
    }

    public Geocode getGeocode()
    {
        return (this.getGeocodedAddress() != null) ? this.getGeocodedAddress().getGeocode() : null;
    }

    public DistrictInfo getDistrictInfo()
    {
        return districtInfo;
    }

    public void setDistrictInfo(DistrictInfo districtInfo)
    {
        this.districtInfo = districtInfo;
    }

    public DistrictQuality getDistrictQuality() {
        return districtQuality;
    }

    public void setDistrictQuality(DistrictQuality districtQuality) {
        this.districtQuality = districtQuality;
    }
}
