package gov.nysenate.sage.provider.geocode;

import com.google.common.collect.ImmutableList;
import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.dao.data.PostOfficeDao;
import gov.nysenate.sage.dao.provider.nysgeo.GeocoderDao;
import gov.nysenate.sage.model.PostOfficeData;
import gov.nysenate.sage.model.address.*;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.provider.geocache.GeoCache;
import gov.nysenate.sage.util.ExecutorUtil;
import gov.nysenate.sage.util.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;

import static gov.nysenate.sage.model.result.ResultStatus.*;

/**
* Provides methods for mapping Address to GeocodedAddress or vice-versa, with or without Result information.
*/
@Service
public class GeocodeService {
    private static final Logger logger = LoggerFactory.getLogger(GeocodeService.class);
    private final Map<Geocoder, GeocoderDao> geocoderDaoMap;
    private final GeoCache geoCache;
    private final ImmutableList<Geocoder> defaultRanking;
    private final ThreadPoolTaskExecutor executor;
    private final PostOfficeDao postOfficeDao;
    private final Map<Tuple<Zip5, List<Geocoder>>, PostOfficeData<List<GeocodedAddress>>> poBoxCache = new HashMap<>();

    @Autowired
    public GeocodeService(List<GeocoderDao> geocoderDaos, GeoCache geoCache,
                          @Value("${geocoder.ranking}") String geocoderRankingStr,
                          PostOfficeDao postOfficeDao, Environment env) {
        this.geocoderDaoMap = geocoderDaos.stream().collect(Collectors.toMap(GeocoderDao::geocoder, Function.identity()));
        this.geoCache = geoCache;
        List<Geocoder> tempRanking = new ArrayList<>();
        for (String geocoder : geocoderRankingStr.split(", *")) {
            tempRanking.add(Geocoder.valueOf(geocoder.toUpperCase()));
        }
        this.defaultRanking = ImmutableList.copyOf(tempRanking);
        this.postOfficeDao = postOfficeDao;
        this.executor = ExecutorUtil.createExecutor("geocode", env.getValidateThreads());
    }

    public GeocodedAddress getGeocodedAddress(List<Geocoder> geocoders, @Nonnull Address address) {
        return getOrDefault(geocode(geocoders, address), address);
    }

    public GeocodeResult geocode(List<Geocoder> geocoders, @Nonnull Address address) {
        var geocodedAddress = new GeocodedAddress(address);
        if (!address.isValid()) {
            return new GeocodeResult(null, INSUFFICIENT_ADDRESS, geocodedAddress);
        }

        if (geocoders == null) {
            geocoders = defaultRanking;
        }
        if (address.isPoBox()) {
            var cacheKey = new Tuple<>(address.getZip5(), geocoders);
            if (!poBoxCache.containsKey(cacheKey)) {
                final List<Geocoder> finalGeocoders = geocoders;
                List<GeocodeResult> postOfficeResults = postOfficeDao.getPostOffices(address.getZip5())
                        .stream().map(addr -> geocode(finalGeocoders, addr)).toList();
                poBoxCache.put(cacheKey, PostOfficeData.getGeocodeData(postOfficeResults));
            }
            return getGeocodeResult(((PostOfficeBox) address), poBoxCache.get(cacheKey).getData(address.getPostalCity()));
        }

        ResultStatus status = MISSING_GEOCODER;
        Geocoder geocoder = null;
        for (Geocoder newGeocoder : geocoders) {
            geocoder = newGeocoder;
            geocodedAddress = geocoderDaoMap.get(geocoder).getGeocodedAddress(((BuildingAddress) address));
            if (geocodedAddress == null || !geocodedAddress.isValidGeocode()) {
                status = NO_GEOCODE_RESULT;
            } else {
                status = SUCCESS;
                break;
            }
        }
        var result = new GeocodeResult(geocoder, status, GeocodedAddress.from(geocodedAddress, address));
        geoCache.cache(result);
        return result;
    }

    public List<GeocodedAddress> getGeocodedAddresses(List<Address> addresses) {
        List<GeocodeResult> results = geocode(addresses);
        List<GeocodedAddress> finalResults = new ArrayList<>();
        for (int i = 0; i < addresses.size(); i++) {
            finalResults.add(getOrDefault(results.get(i), addresses.get(i)));
        }
        return finalResults;
    }

    public List<GeocodeResult> geocode(List<Address> addresses) {
        List<GeocodeResult> geocodeResults = new ArrayList<>();
        List<Future<GeocodeResult>> futureGeocodeResults = new ArrayList<>();

        for (Address address : addresses) {
            futureGeocodeResults.add(executor.submit(() -> geocode(null, address)));
        }

        for (Future<GeocodeResult> geocodeResult : futureGeocodeResults) {
            try {
                geocodeResults.add(geocodeResult.get());
            }
            catch (Exception ex) {
                geocodeResults.add(new GeocodeResult(null, INTERNAL_ERROR));
                logger.error("Error while processing Future", ex);
            }
        }
        geoCache.cache(geocodeResults);
        return geocodeResults;
    }

    public GeocodedAddress getRevGeocodedAddress(List<Geocoder> geocoders, Point point) {
        return getOrDefault(reverseGeocode(geocoders, point), point);
    }

    public GeocodeResult reverseGeocode(List<Geocoder> geocoders, Point point) {
        if (point == null) {
            return new GeocodeResult(null, MISSING_POINT);
        }
        if (geocoders == null) {
            geocoders = defaultRanking;
        }
        ResultStatus status = MISSING_GEOCODER;
        Geocoder revGeocoder = null;
        GeocodedAddress revGeocodedAddress = null;
        for (Geocoder newGeocoder : geocoders) {
            revGeocoder = newGeocoder;
            revGeocodedAddress = geocoderDaoMap.get(revGeocoder).getGeocodedAddress(point);
            if (revGeocodedAddress == null) {
                status = RESPONSE_PARSE_ERROR;
            }
            else if (!revGeocodedAddress.isValidAddress()) {
                status = NO_REVERSE_GEOCODE_RESULT;
            }
            else {
                status = SUCCESS;
                break;
            }
        }
        return new GeocodeResult(revGeocoder, status, revGeocodedAddress);
    }

    public List<GeocodedAddress> getRevGeocodedAddresses(List<Point> points) {
        List<GeocodeResult> results = reverseGeocode(points);
        List<GeocodedAddress> finalResults = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            finalResults.add(getOrDefault(results.get(i), points.get(i)));
        }
        return finalResults;
    }

    public List<GeocodeResult> reverseGeocode(List<Point> points) {
        List<GeocodeResult> geocodeResults = new ArrayList<>();
        List<Future<GeocodeResult>> futureGeocodeResults = new ArrayList<>();

        for (Point point : points) {
            futureGeocodeResults.add(executor.submit(() -> reverseGeocode(null, point)));
        }

        for (Future<GeocodeResult> geocodeResult : futureGeocodeResults) {
            try {
                geocodeResults.add(geocodeResult.get());
            }
            catch (Exception ex) {
                geocodeResults.add(new GeocodeResult(null, INTERNAL_ERROR));
                logger.error("Error while processing Future", ex);
            }
        }
        return geocodeResults;
    }

    private static GeocodedAddress getOrDefault(GeocodeResult baseResult, Address defaultAddress) {
        return baseResult.isSuccess() ? baseResult.getGeocodedAddress() : new GeocodedAddress(defaultAddress);
    }

    private static GeocodedAddress getOrDefault(GeocodeResult baseResult, Point defaultPoint) {
        return baseResult.isSuccess() ? baseResult.getGeocodedAddress() :
                new GeocodedAddress(new Geocode(defaultPoint, GeocodeQuality.POINT, null, false));
    }

    private static GeocodeResult getGeocodeResult(PostOfficeBox poBox, List<GeocodedAddress> postOffices) {
        if (postOffices.isEmpty()) {
            return new GeocodeResult(null, MISSING_GEOCODED_ADDRESS, new GeocodedAddress(poBox));
        }
        final Geocoder firstGeocoder = postOffices.get(0).getGeocode().originalGeocoder();
        boolean hasCommonGeocoder = postOffices.stream().map(geoAddr -> geoAddr.getGeocode().originalGeocoder())
                .allMatch(firstGeocoder::equals);
        var postalGeoAddr = new GeocodedPostOfficeBox(poBox, postOffices);
        return new GeocodeResult(hasCommonGeocoder ? firstGeocoder : null, SUCCESS, postalGeoAddr);
    }
}
