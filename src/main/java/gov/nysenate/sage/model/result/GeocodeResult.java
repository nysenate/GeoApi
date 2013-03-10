package gov.nysenate.sage.model.result;

import gov.nysenate.sage.model.address.GeocodedAddress;

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

    public GeocodedAddress getGeocodedAddress()
    {
        return this.geocodedAddress;
    }

    public void setGeocodedAddress(GeocodedAddress geocodedAddress)
    {
        this.geocodedAddress = geocodedAddress;
    }

    public Map<String,Object> toMap()
    {
        LinkedHashMap<String,Object> root = new LinkedHashMap<>();
        LinkedHashMap<String,Object> data = new LinkedHashMap<>();

        if (this.geocodedAddress != null){
            data.put("address", this.geocodedAddress.getAddress());
            data.put("validated", this.geocodedAddress.isGeocoded());
            data.put("geocode", this.geocodedAddress.getGeocode());
        }
        else {
            data.put("address", null);
            data.put("validated", false);
            data.put("geocode", null);
        }

        data.put("source", this.getSource());
        data.put("messages", this.getMessages());
        root.put("geocodeResult", data);
        return root;
    }
}
