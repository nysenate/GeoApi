package gov.nysenate.sage.dao.provider.yahoo;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;

public interface YahooBossDao {

    /**
     * Connects to Yahoo BOSS to get geocode response
     * @param address
     * @return
     */
    public GeocodedAddress getGeocodedAddress(Address address);


}
