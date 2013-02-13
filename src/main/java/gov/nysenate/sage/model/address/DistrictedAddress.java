package gov.nysenate.sage.model.address;

import gov.nysenate.sage.model.DistrictInfo;

import java.io.Serializable;

/**
 * Represents an address with district information.
 */
public class DistrictedAddress implements Serializable, Cloneable
{
    protected GeocodedAddress geocodedAddress;
    protected DistrictInfo districtInfo;

    public DistrictedAddress() {}

    public DistrictedAddress(GeocodedAddress geocodedAddress, DistrictInfo districtInfo)
    {
        this.geocodedAddress = geocodedAddress;
        this.districtInfo = districtInfo;
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

    public DistrictInfo getDistrictInfo()
    {
        return districtInfo;
    }

    public void setDistrictInfo(DistrictInfo districtInfo)
    {
        this.districtInfo = districtInfo;
    }
}
