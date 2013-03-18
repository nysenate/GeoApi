package gov.nysenate.sage.model.result;

import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Geocode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

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
        this.setSource(sourceClass);
    }

    /** Convenience accessor */
    public Geocode getGeocode()
    {
        return (geocodedAddress != null) ? geocodedAddress.getGeocode() : null;
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
