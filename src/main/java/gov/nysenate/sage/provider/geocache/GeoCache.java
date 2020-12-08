package gov.nysenate.sage.provider.geocache;

import gov.nysenate.sage.dao.provider.geocache.SqlGeoCacheDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.address.GeocodedStreetAddress;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.provider.geocode.GeocodeService;
import gov.nysenate.sage.service.geo.GeocodeServiceValidator;
import gov.nysenate.sage.service.geo.ParallelGeocodeService;
import gov.nysenate.sage.util.StreetAddressParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class GeoCache implements GeocodeCacheService
{
    private final Logger logger = LoggerFactory.getLogger(GeoCache.class);
    private static Set<GeocodeService> cacheableProviders = new HashSet<>();
    private static Set<Class<? extends GeocodeService>> cacheableProvidersClasses = new HashSet<>();
    private SqlGeoCacheDao sqlGeoCacheDao;
    private ParallelGeocodeService parallelGeocodeService;
    private GeocodeServiceValidator geocodeServiceValidator;

    @Autowired
    public GeoCache(SqlGeoCacheDao sqlGeoCacheDao, ParallelGeocodeService parallelGeocodeService, GeocodeServiceValidator geocodeServiceValidator) {
        this.sqlGeoCacheDao = sqlGeoCacheDao;
        this.parallelGeocodeService = parallelGeocodeService;
        this.geocodeServiceValidator = geocodeServiceValidator;
        logger.debug("Instantiated GeoCache.");
    }

    /**
     * Designates a provider (that has been registered) as a reliable source for caching results.
     * @param provider the provider to be added to the cacheableProviders list
     */
    public void registerProviderAsCacheable(GeocodeService provider)
    {
        if (provider != null) {
            cacheableProviders.add(provider);
            cacheableProvidersClasses.add(provider.getClass());
        }
    }

    /**
     * Checks if providerName is allowed to save result into cache
     * @param provider check name of this provider to see if it is registered as cacheable
     * @return true if it is allowed, false otherwise
     */
    public boolean isProviderCacheable(Class<? extends GeocodeService> provider)
    {
        return cacheableProvidersClasses.contains(provider);
    }

    /** {@inheritDoc} */
    @Override
    public GeocodeResult geocode(Address address)
    {
        logger.trace("Attempting geocode cache lookup");
        GeocodeResult geocodeResult  = new GeocodeResult(this.getClass());

        /* Proceed only on valid input */
        if (!geocodeServiceValidator.validateGeocodeInput(address, geocodeResult)) {
            logger.info(address + " is invalid");
            geocodeResult.setGeocodedAddress(new GeocodedAddress());
            return geocodeResult;
        }
        /* Retrieve geocoded address from cache */
        StreetAddress sa = StreetAddressParser.parseAddress(address);
        GeocodedStreetAddress geocodedStreetAddress = sqlGeoCacheDao.getCacheHit(sa);
        if ( geocodedStreetAddress == null )  {
            geocodedStreetAddress = new GeocodedStreetAddress(sa);
        }

        /* Validate and return */
        if (!geocodeServiceValidator.validateGeocodeResult(this.getClass(), geocodedStreetAddress.toGeocodedAddress(), geocodeResult, false)) {
            logger.info("Failed to find cache hit for " + address.toString());
        }
        return geocodeResult;
    }

    /** {@inheritDoc} */
    @Override
    public ArrayList<GeocodeResult> geocode(ArrayList<Address> addresses)
    {
        return parallelGeocodeService.geocode(this, addresses);
    }

    /** {@inheritDoc} */
    @Override
    public void saveToCache(GeocodeResult geocodeResult)
    {
        if (geocodeResult != null && geocodeResult.isSuccess() && geocodeResult.getSource() != null) {
            if (isProviderCacheable(geocodeResult.getSource())) {
                sqlGeoCacheDao.cacheGeocodedAddress(geocodeResult.getGeocodedAddress());
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void saveToCacheAndFlush(GeocodeResult geocodeResult)
    {
        this.saveToCache(geocodeResult);
        sqlGeoCacheDao.flushCacheBuffer();
    }

    /** {@inheritDoc} */
    @Override
    public void saveToCache(List<GeocodeResult> geocodeResults)
    {
        List<GeocodedAddress> geocodedAddresses = new ArrayList<>();
        for (GeocodeResult geocodeResult : geocodeResults) {
            if (geocodeResult != null && geocodeResult.isSuccess()) {
                if (isProviderCacheable(geocodeResult.getSource())) {
                    geocodedAddresses.add(geocodeResult.getGeocodedAddress());
                }
            }
        }
        sqlGeoCacheDao.cacheGeocodedAddresses(geocodedAddresses);
    }

    /** {@inheritDoc} */
    @Override
    public void saveToCacheAndFlush(List<GeocodeResult> geocodeResults)
    {
        this.saveToCache(geocodeResults);
        sqlGeoCacheDao.flushCacheBuffer();
    }
}
