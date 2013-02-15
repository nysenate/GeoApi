package gov.nysenate.sage.model.result;

import gov.nysenate.sage.model.address.GeocodedAddress;

import java.util.ArrayList;

public class GeocodeResult extends BaseResult
{
    private GeocodedAddress geocodedAddress;

    public GeocodeResult()
    {
        this(null, "", null);
    }

    public GeocodeResult(Class sourceClass)
    {
        this(null, "", sourceClass);
    }

    public GeocodeResult(GeocodedAddress geocodedAddress, String status, Class sourceClass )
    {
        this.setGeocodedAddress(geocodedAddress);
        this.setStatus(status);
        this.setSource(sourceClass);
    }

    public GeocodedAddress getGeocodedAddress()
    {
        return this.geocodedAddress;
    }

    public void setGeocodedAddress(GeocodedAddress geocodedAddress)
    {
        this.geocodedAddress = geocodedAddress;
    }
}
