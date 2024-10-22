package gov.nysenate.sage.service.geo;

import gov.nysenate.sage.model.api.SingleGeocodeRequest;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.provider.geocode.Geocoder;

import java.util.List;

public interface SageRevGeocodeServiceProvider {
    /**
     * Perform a reverse geocode on the submitted options
     */
    GeocodeResult reverseGeocode(SingleGeocodeRequest geocodeRequest);

    /**
     * Perform batch reverse geocoding using supploed BatchGeocodeRequest with points set.
     * @return  List<GeocodeResult> or null if batchRevGeoRequest is null.
     */
    List<GeocodeResult> reverseGeocode(List<Point> points, Geocoder provider);
}
