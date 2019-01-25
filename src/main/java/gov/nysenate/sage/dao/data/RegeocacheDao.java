package gov.nysenate.sage.dao.data;

import gov.nysenate.sage.model.address.NYSGeoAddress;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.model.geo.Geocode;

import java.util.List;

public interface RegeocacheDao {

    /**
     * Retreives a total count of all rows contained from the NYS GEO DB
     * @return
     */
    public Integer getNYSTotalAddresses();

    /**
     * Retrieves a batch of addresses from the NYS GEO DB
     * This is typically done in batches of 2000 addresses at a time
     * @param nys_limit
     * @param nys_offset
     * @return
     */
    public List<NYSGeoAddress> getBatchOfNYSGeoAddresses(int nys_limit, int nys_offset);

    /**
     * Checks our geocache for the given street address. It will return the geoprovider of the address
     * @param nysStreetAddress
     * @return
     */
    public String getProviderOfAddressInCacheIfExists(StreetAddress nysStreetAddress);

    /**
     * Inserts a new address into our geocache
     * @param nysStreetAddress
     * @param nysGeocode
     */
    public void insetIntoGeocache(StreetAddress nysStreetAddress, Geocode nysGeocode);

    /**
     * Updates an address we already have in the geocache
     * @param nysStreetAddress
     * @param nysGeocode
     */
    public void updateGeocache(StreetAddress nysStreetAddress, Geocode nysGeocode);

    /**
     * Reteives a list of all zip codes that we have in the geoapi database
     * @return
     */
    public List<String> getAllZips();
}
