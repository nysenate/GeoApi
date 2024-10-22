package gov.nysenate.sage.provider.district;

import gov.nysenate.sage.model.address.DistrictedStreetRange;
import gov.nysenate.sage.service.street.StreetData;

import java.util.List;

/**
 * The street lookup service essentially takes in a zip code and returns a collection
 * of street address ranges with district information associated with each range.
 */
public interface StreetLookupService {
    StreetData source();
    List<DistrictedStreetRange> streetLookup(Integer zip5);
}
