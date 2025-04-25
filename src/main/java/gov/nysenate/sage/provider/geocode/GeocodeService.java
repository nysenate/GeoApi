package gov.nysenate.sage.provider.geocode;

import com.google.common.collect.ImmutableList;
import gov.nysenate.sage.dao.data.PostOfficeDao;
import gov.nysenate.sage.dao.provider.nysgeo.GeocoderDao;
import gov.nysenate.sage.dao.stats.geocode.SqlGeocodeStatsDao;
import gov.nysenate.sage.model.PostOfficeCache;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.BuildingAddress;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.address.PostOfficeBox;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.provider.PostOfficeCacheManager;
import gov.nysenate.sage.provider.geocache.GeoCache;
import gov.nysenate.sage.util.ExecutorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
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
    private final SqlGeocodeStatsDao geocodeStatsDao;
    private final ThreadPoolTaskExecutor executor;
    private final PostOfficeDao postOfficeDao;
    private final PostOfficeCache<Geocoder, GeocodeResult> poBoxCache = PostOfficeCacheManager.getGeocodeCache();

    @Autowired
    public GeocodeService(List<GeocoderDao> geocoderDaos, GeoCache geoCache,
                          @Value("${geocoder.ranking}") String geocoderRankingStr,
                          SqlGeocodeStatsDao geocodeStatsDao,
                          PostOfficeDao postOfficeDao, @Value("${num.threads:3}") int numThreads) {
        this.geocoderDaoMap = geocoderDaos.stream()
                .collect(Collectors.toMap(GeocoderDao::geocoder, Function.identity()));
        this.geoCache = geoCache;
        List<Geocoder> tempRanking = new ArrayList<>();
        for (String geocoder : geocoderRankingStr.split(", *")) {
            tempRanking.add(Geocoder.valueOf(geocoder.toUpperCase()));
        }
        this.defaultRanking = ImmutableList.copyOf(tempRanking);
        this.geocodeStatsDao = geocodeStatsDao;
        this.postOfficeDao = postOfficeDao;
        this.executor = ExecutorUtil.createExecutor("geocode", numThreads);
    }

    public GeocodeResult geocode(List<Geocoder> geocoders, @Nonnull Address address) {
        var geocodedAddress = new GeocodedAddress(address);
        if (!address.isValid()) {
            return new GeocodeResult(null, INSUFFICIENT_ADDRESS, geocodedAddress);
        }
        if (address.isOutOfState()) {
            return new GeocodeResult(null, NON_NY_STATE, geocodedAddress);
        }

        if (geocoders == null) {
            geocoders = defaultRanking;
        }
        if (address instanceof PostOfficeBox poBox) {
            return getPostOfficeResult(poBox, geocoders);
        }

        ResultStatus status = MISSING_GEOCODER;
        Geocoder geocoder = null;
        for (Geocoder newGeocoder : geocoders) {
            geocoder = newGeocoder;
            geocodedAddress = geocoderDaoMap.get(geocoder).getGeocodedAddress(((BuildingAddress) address));
            geocodeStatsDao.putGeocodedAddress(geocoder, geocodedAddress);
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

    public List<GeocodeResult> geocode(List<Address> addresses) {
        return getBatchResults(addresses, address -> geocode(null, address));
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

    public List<GeocodeResult> reverseGeocode(List<Point> points) {
        return getBatchResults(points, point -> reverseGeocode(null, point));
    }

    private synchronized GeocodeResult getPostOfficeResult(PostOfficeBox poBox, @Nonnull List<Geocoder> geocoders) {
        GeocodeResult result = poBoxCache.get(poBox, geocoders);
        if (result == null) {
            List<GeocodeResult> postOfficeResults = postOfficeDao.getPostOffices(poBox.getZip5())
                    .stream().map(addr -> geocode(geocoders, addr)).toList();
            result = poBoxCache.putAndGet(poBox, geocoders, postOfficeResults);
        }
        result.setAddress(poBox);
        return result;
    }

    private <T> List<GeocodeResult> getBatchResults(List<T> inputs, Function<T, GeocodeResult> resultMapper) {
        List<GeocodeResult> geocodeResults = new ArrayList<>();
        List<Future<GeocodeResult>> futureGeocodeResults = new ArrayList<>();

        for (T input : inputs) {
            futureGeocodeResults.add(executor.submit(() -> resultMapper.apply(input)));
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

    @PreDestroy
    private void shutdownThreads() {
        executor.shutdown();
    }
}
