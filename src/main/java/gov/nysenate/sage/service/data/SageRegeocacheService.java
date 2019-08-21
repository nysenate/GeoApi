package gov.nysenate.sage.service.data;

public interface SageRegeocacheService {

    /**
     * Regeocaches all of the Zip codes we have geometry for through google
     * @return A success or failure response
     */
    public Object updateZipsInGeocache();

    /**
     * Sends the addresses from NYS GEO that are the same address label but from a different town back to NYS GEO
     * for any updates
     * @param nys_offset
     * @return
     */
    public Object updatesDupsInGeocacheWithNysGeo(int nys_offset);

    /**
     * Updates the geocache by iterating over the NYS Geo database with USPS validation
     * @param nys_offset
     * @return
     */
    public Object updateGeocacheWithNYSGeoData(int nys_offset);
}
