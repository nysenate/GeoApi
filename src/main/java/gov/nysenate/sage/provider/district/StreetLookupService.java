package gov.nysenate.sage.provider.district;

import gov.nysenate.sage.model.address.DistrictedStreetRange;

import java.util.List;

/**
 * The street lookup service essentially takes in a zip code and returns a collection
 * of street address ranges with district information associated with each range.
 */
public interface StreetLookupService {
    List<DistrictedStreetRange> streetLookup(String zip5);
}
