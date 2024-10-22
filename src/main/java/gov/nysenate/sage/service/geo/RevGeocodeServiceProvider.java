package gov.nysenate.sage.service.geo;

import gov.nysenate.sage.model.api.SingleGeocodeRequest;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.provider.geocode.Geocoder;
import gov.nysenate.sage.provider.geocode.GoogleGeocoder;
import gov.nysenate.sage.provider.geocode.NYSGeocoder;
import gov.nysenate.sage.provider.geocode.RevGeocodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
* Point of access for all reverse geocoding requests.
*/
@Service
public class RevGeocodeServiceProvider implements SageRevGeocodeServiceProvider {
    private final Map<Geocoder, RevGeocodeService> providerMap;

    @Autowired
    public RevGeocodeServiceProvider(GoogleGeocoder googleGeocoder, NYSGeocoder nysGeocoder) {
        this.providerMap = Map.of(Geocoder.GOOGLE, googleGeocoder, Geocoder.NYSGEO, nysGeocoder);
    }

    @Override
    public GeocodeResult reverseGeocode(SingleGeocodeRequest geocodeRequest) {
        if (geocodeRequest != null) {
            return reverseGeocode(geocodeRequest.getPoint(), geocodeRequest.getGeocoders());
        }
        else {
            return null;
        }
    }

    /**
     * Perform batch reverse geocoding using supplied BatchGeocodeRequest with points set.
     * @return  List<GeocodeResult> or null if batchRevGeoRequest is null.
     */
    @Override
    public List<GeocodeResult> reverseGeocode(List<Point> points, Geocoder provider) {
        List<Geocoder> geocoders = new ArrayList<>();
        geocoders.add(provider);
        providerMap.keySet().stream().filter(gc -> gc != provider).forEach(geocoders::add);
        List<GeocodeResult> geocodeResults = new ArrayList<>();
        for (Point point : points) {
            geocodeResults.add(reverseGeocode(point, geocoders));
        }
        return geocodeResults;
    }

    /**
     * Perform reverse geocoding with all options specified.
     * @param point             Point to lookup address for
     * @param providers         To use
     * @return                  GeocodeResult
     */
    private GeocodeResult reverseGeocode(Point point, List<Geocoder> providers) {
        if (providers == null || providers.isEmpty()) {
            providers = List.of(Geocoder.GOOGLE);
        }
        GeocodeResult geocodeResult = null;
        for (Geocoder provider : providers) {
            RevGeocodeService revGeocodeService = providerMap.get(provider);
            if (revGeocodeService == null) {
                continue;
            }
            geocodeResult = revGeocodeService.reverseGeocode(point);
            if (geocodeResult.isSuccess()) {
                break;
            }
        }
        return geocodeResult;
    }
}
