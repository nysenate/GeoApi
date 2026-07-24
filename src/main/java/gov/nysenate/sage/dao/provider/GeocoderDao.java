package gov.nysenate.sage.dao.provider;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.provider.geocode.Geocoder;

public interface GeocoderDao {
    Geocoder geocoder();

    GeocodedAddress getGeocodedAddress(Address address);

    default GeocodedAddress getGeocodedAddress(Point point) {
        return null;
    }
}
