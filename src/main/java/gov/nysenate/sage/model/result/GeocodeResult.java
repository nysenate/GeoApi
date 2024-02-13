package gov.nysenate.sage.model.result;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Geocode;

public class GeocodeResult extends BaseResult
{
    private GeocodedAddress geocodedAddress;

    public GeocodeResult()
    {
        this(null, null, null);
    }

    public GeocodeResult(Class sourceClass)
    {
        this(sourceClass, null, null);
    }

    public GeocodeResult(Class sourceClass, ResultStatus resultStatus)
    {
        this(sourceClass, resultStatus, null);
    }

    public GeocodeResult(Class sourceClass, ResultStatus resultStatus, GeocodedAddress geocodedAddress)
    {
        this.setSource(sourceClass);
        this.setGeocodedAddress(geocodedAddress);
        if (resultStatus != null ) {
            this.setStatusCode(resultStatus);
        }
    }

    /** Convenience accessor */
    public Geocode getGeocode()
    {
        return (geocodedAddress != null) ? geocodedAddress.getGeocode() : null;
    }

    public Address getAddress()
    {
        return (geocodedAddress != null) ? geocodedAddress.getAddress() : null;
    }

    public GeocodedAddress getGeocodedAddress()
    {
        return this.geocodedAddress;
    }

    public void setGeocodedAddress(GeocodedAddress geocodedAddress)
    {
        this.geocodedAddress = geocodedAddress;
    }

    public void setAddress(Address address) {
        if (geocodedAddress == null) {
            this.geocodedAddress = new GeocodedAddress();
        }
        geocodedAddress.setAddress(address);
    }
}
