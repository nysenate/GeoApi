package gov.nysenate.sage.provider.geocache;

import gov.nysenate.sage.dao.provider.geocache.SqlGeoCacheDao;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.provider.geocode.GeocodeService;
import gov.nysenate.sage.provider.geocode.Geocoder;
import gov.nysenate.sage.service.geo.ParallelGeocodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class GeoCache extends GeocodeService implements GeocodeCacheService {
    private static final Logger logger = LoggerFactory.getLogger(GeoCache.class);
    private static final Set<Geocoder> cacheableGeocoders = new HashSet<>();
    private final SqlGeoCacheDao sqlGeoCacheDao;
    @Value("${geocoder.cacheable}")
    private String cacheable;

    @Value("${geocache.enabled:true}")
    // TODO
    private boolean CACHE_ENABLED;

    @Autowired
    public GeoCache(SqlGeoCacheDao sqlGeoCacheDao, ParallelGeocodeService parallelGeocodeService) {
        super(sqlGeoCacheDao,  parallelGeocodeService);
        this.sqlGeoCacheDao = sqlGeoCacheDao;
        logger.debug("Instantiated GeoCache.");
    }

    /** {@inheritDoc} */
    @Override
    public void saveToCacheAndFlush(GeocodeResult geocodeResult) {
        if (geocodeResult != null && geocodeResult.isSuccess() && geocodeResult.getSource() != null) {
            if (geocodeResult.getSource() != Geocoder.GEOCACHE) {
                sqlGeoCacheDao.cacheGeocodedAddress(geocodeResult.getGeocodedAddress());
            }
        }
        sqlGeoCacheDao.flushCacheBuffer();
    }

    /** {@inheritDoc} */
    @Override
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

    @Nonnull
    @Override
    public Geocoder name() {
        return Geocoder.GEOCACHE;
    }
}
