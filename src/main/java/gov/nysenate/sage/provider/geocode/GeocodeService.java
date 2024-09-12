package gov.nysenate.sage.provider.geocode;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.result.GeocodeResult;

import java.util.List;

/**
* Base interface for providers of geocoding services.
*/
public interface GeocodeService {
    GeocodeResult geocode(Address address);
    List<GeocodeResult> geocode(List<Address> addresses);
}
