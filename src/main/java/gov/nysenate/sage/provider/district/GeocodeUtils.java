package gov.nysenate.sage.provider.district;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public final class GeocodeUtils {
    private GeocodeUtils() {}

    public static GeocodedAddress getGeocodedAddress(@Nonnull Address address, GeocodeResult result) {
        return getOrDefault(result, address);
    }

    public static GeocodedAddress getRevGeocodedAddress(Point point, GeocodeResult result) {
        return getOrDefault(result, point);
    }

    public static List<GeocodedAddress> getGeocodedAddresses(List<Address> addresses, List<GeocodeResult> results) {
        List<GeocodedAddress> finalResults = new ArrayList<>();
        for (int i = 0; i < addresses.size(); i++) {
            finalResults.add(getOrDefault(results.get(i), addresses.get(i)));
        }
        return finalResults;
    }

    public static List<GeocodedAddress> getRevGeocodedAddresses(List<Point> points, List<GeocodeResult> results) {
        List<GeocodedAddress> finalResults = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            finalResults.add(getOrDefault(results.get(i), points.get(i)));
        }
        return finalResults;
    }

    private static GeocodedAddress getOrDefault(GeocodeResult baseResult, Address defaultAddress) {
        return baseResult.isSuccess() ? baseResult.getGeocodedAddress() : new GeocodedAddress(defaultAddress);
    }

    private static GeocodedAddress getOrDefault(GeocodeResult baseResult, Point defaultPoint) {
        return baseResult.isSuccess() ? baseResult.getGeocodedAddress() :
                new GeocodedAddress(new Geocode(defaultPoint, GeocodeQuality.POINT, null, false));
    }
}
