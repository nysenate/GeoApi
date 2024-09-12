package gov.nysenate.sage.provider.geocode;

import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;

/**
* Base interface for providers of reverse geocoding services.
*/
public interface RevGeocodeService {
    GeocodeResult reverseGeocode(Point point);
}
