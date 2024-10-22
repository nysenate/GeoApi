package gov.nysenate.sage.dao.provider.nysgeo;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Point;

public interface GeocoderDao {
    GeocodedAddress getGeocodedAddress(Address address);
    GeocodedAddress getGeocodedAddress(Point point);
}
