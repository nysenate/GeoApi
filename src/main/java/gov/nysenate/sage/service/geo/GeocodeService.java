package gov.nysenate.sage.service.geo;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;

import java.util.ArrayList;

/**
 * GeocodeService is used to geocode addresses to obtain their coordinates or to obtain an address
 * from a coordinate pair.
 */
public interface GeocodeService
{
    public GeocodeResult geocode(Address address);
    public ArrayList<GeocodeResult> geocode(ArrayList<Address> addresses);
    public GeocodeResult reverseGeocode(Point point);
}
