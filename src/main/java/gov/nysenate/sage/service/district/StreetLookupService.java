package gov.nysenate.sage.service.district;

import gov.nysenate.sage.model.address.DistrictStreetRange;

import java.util.List;

/**
 * The street lookup service essentially takes in a zip code and returns a collection
 * of street address ranges with district information associated with each range.
 */
public interface StreetLookupService
{
    public List<DistrictStreetRange> streetLookup(String zip5);
}
