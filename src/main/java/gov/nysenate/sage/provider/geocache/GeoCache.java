package gov.nysenate.sage.provider.geocache;

import gov.nysenate.sage.dao.provider.geocache.SqlGeoCacheDao;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.provider.geocode.Geocoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GeoCache {
    private final SqlGeoCacheDao sqlGeoCacheDao;

    @Value("${geocache.enabled:true}")
    // TODO
    private boolean CACHE_ENABLED;

    @Autowired
    public GeoCache(SqlGeoCacheDao sqlGeoCacheDao) {
        this.sqlGeoCacheDao = sqlGeoCacheDao;
    }

    public void saveToCacheAndFlush(GeocodeResult geocodeResult) {
        if (geocodeResult != null && geocodeResult.isSuccess() && geocodeResult.getSource() != null) {
            if (geocodeResult.getSource() != Geocoder.GEOCACHE) {
                sqlGeoCacheDao.cacheGeocodedAddress(geocodeResult.getGeocodedAddress());
            }
        }
        sqlGeoCacheDao.flushCacheBuffer();
    }

    public void saveToCacheAndFlush(List<GeocodeResult> geocodeResults) {
        List<GeocodedAddress> geocodedAddresses = new ArrayList<>();
        for (GeocodeResult geocodeResult : geocodeResults) {
            if (geocodeResult != null && geocodeResult.isSuccess() && geocodeResult.getSource() != Geocoder.GEOCACHE) {
                geocodedAddresses.add(geocodeResult.getGeocodedAddress());
            }
        }
        sqlGeoCacheDao.cacheGeocodedAddresses(geocodedAddresses);
        sqlGeoCacheDao.flushCacheBuffer();
    }
}
