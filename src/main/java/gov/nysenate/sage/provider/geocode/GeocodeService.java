package gov.nysenate.sage.provider.geocode;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.result.GeocodeResult;

import java.util.ArrayList;

/**
* Base interface for providers of geocoding services.
*/
public interface GeocodeService
{
    public GeocodeResult geocode(Address address);
    public ArrayList<GeocodeResult> geocode(ArrayList<Address> addresses);
}
