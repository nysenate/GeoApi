package gov.nysenate.sage.provider;

import gov.nysenate.sage.dao.provider.GeoCacheDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.address.GeocodedStreetAddress;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.service.geo.GeocodeCacheService;
import gov.nysenate.sage.service.geo.ParallelGeocodeService;
import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static gov.nysenate.sage.service.geo.GeocodeServiceValidator.validateGeocodeInput;
import static gov.nysenate.sage.service.geo.GeocodeServiceValidator.validateGeocodeResult;

public class GeoCache implements GeocodeCacheService
{
    private final Logger logger = Logger.getLogger(GeoCache.class);
    private GeoCacheDao geoCacheDao;

    public GeoCache() {
        this.geoCacheDao = new GeoCacheDao();
    }

    @Override
    public GeocodeResult geocode(Address address)
    {
        logger.trace("Attempting geocode cache lookup");
        GeocodeResult geocodeResult  = new GeocodeResult(this.getClass());

        /** Proceed only on valid input */
        if (!validateGeocodeInput(address, geocodeResult)) return geocodeResult;

        /** Retrieve geocoded address from cache */
        GeocodedStreetAddress geocodedStreetAddress = geoCacheDao.getCacheHit(address);

        /** Validate and return */
        if (!validateGeocodeResult(geocodedStreetAddress, geocodeResult)) {
            logger.trace("Failed to find cache hit for " + address.toString());
        }
        return geocodeResult;
    }

    @Override
    public ArrayList<GeocodeResult> geocode(ArrayList<Address> addresses)
    {
        return ParallelGeocodeService.geocode(this, addresses);
    }

    @Override
    public GeocodeResult reverseGeocode(Point point)
    {
        throw new NotImplementedException("No reverse geocoding yet!");
    }

    @Override
    public void saveToCache(GeocodeResult geocodeResult)
    {
        if (geocodeResult != null && geocodeResult.isSuccess()) {
            geoCacheDao.cacheGeocodedAddress(geocodeResult.getGeocodedAddress());
        }
    }

    @Override
    public void saveToCache(List<GeocodeResult> geocodeResults)
    {
        List<GeocodedAddress> geocodedAddresses = new ArrayList<>();
        for (GeocodeResult geocodeResult : geocodeResults) {
            if (geocodeResult != null && geocodeResult.isSuccess()) {
                geocodedAddresses.add(geocodeResult.getGeocodedAddress());
            }
        }
        geoCacheDao.cacheGeocodedAddresses(geocodedAddresses);
    }
}
