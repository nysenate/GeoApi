package gov.nysenate.sage.dao.provider.nysgeo;

import gov.nysenate.sage.model.address.BuildingAddress;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.provider.geocode.Geocoder;

public interface GeocoderDao {
    Geocoder geocoder();

    GeocodedAddress getGeocodedAddress(BuildingAddress address);

    // TODO: should probably just return an address
    default GeocodedAddress getGeocodedAddress(Point point) {
        return null;
    }
}
