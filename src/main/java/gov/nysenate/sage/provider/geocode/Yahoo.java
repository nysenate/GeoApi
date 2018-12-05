package gov.nysenate.sage.provider.geocode;

import gov.nysenate.sage.dao.provider.yahoo.HttpYahooDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.service.geo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

@Service
public class Yahoo implements GeocodeService, RevGeocodeService
{
    private final Logger logger = LoggerFactory.getLogger(Yahoo.class);
    private HttpYahooDao httpYahooDao;
    private GeocodeServiceValidator geocodeServiceValidator;
    private ParallelRevGeocodeService parallelRevGeocodeService;
    private RevGeocodeServiceValidator revGeocodeServiceValidator;

    @Autowired
    public Yahoo(HttpYahooDao httpYahooDao, GeocodeServiceValidator geocodeServiceValidator,
                 ParallelRevGeocodeService parallelRevGeocodeService, RevGeocodeServiceValidator revGeocodeServiceValidator)
    {
        this.httpYahooDao = httpYahooDao;
        this.geocodeServiceValidator = geocodeServiceValidator;
        this.parallelRevGeocodeService = parallelRevGeocodeService;
        this.revGeocodeServiceValidator = revGeocodeServiceValidator;
        logger.debug("Instantiated Yahoo.");
    }

    /** Geocode Service Implementation -----------------------------------------------------------*/

    @Override
    public GeocodeResult geocode(Address address)
    {
        logger.trace("Performing geocoding using Yahoo Free");
        GeocodeResult geocodeResult = new GeocodeResult(this.getClass());

        /** Ensure that the geocoder is active, otherwise return error result. */
        if (!geocodeServiceValidator.isGeocodeServiceActive(this.getClass(), geocodeResult)) {
            return geocodeResult;
        }

        /** Proceed only on valid input */
        if (!GeocodeServiceValidator.validateGeocodeInput(address, geocodeResult)) {
            return geocodeResult;
        }

        /** Retrieve geocoded address from dao */
        GeocodedAddress geocodedAddress = this.httpYahooDao.getGeocodedAddress(address);

        /** Validate and set result */
        if (!geocodeServiceValidator.validateGeocodeResult(this.getClass(), geocodedAddress, geocodeResult, true)) {
            logger.warn("Failed to geocode " + address.toString() + " using Yahoo Free!");
        }

        return geocodeResult;
    }

    @Override
    public ArrayList<GeocodeResult> geocode(ArrayList<Address> addresses)
    {
        logger.trace("Performing batch geocoding using Yahoo Free");
        ArrayList<GeocodeResult> geocodeResults = new ArrayList<>();

        /** Ensure that the geocoder is active, otherwise return list of error results. */
        if (!geocodeServiceValidator.isGeocodeServiceActive(this.getClass(), geocodeResults, addresses.size())) {
            return geocodeResults;
        }

        /** Retrieve geocoded addresses from dao */
        List<GeocodedAddress> geocodedAddresses = this.httpYahooDao.getGeocodedAddresses(addresses);

        /** Validate batch */
        if (!geocodeServiceValidator.validateBatchGeocodeResult(
                this.getClass(), addresses, geocodeResults, geocodedAddresses, true)){
            logger.warn("Yahoo batch result is inconsistent with input addresses.");
        }

        return geocodeResults;
    }

    /** Reverse Geocode Service Implementation ---------------------------------------------------*/

    /**
    * Reverse Geocode using Yahoo Free
    * @param point Point to lookup address for
    * @return      GeocodeResult
    */
    @Override
    public GeocodeResult reverseGeocode(Point point)
    {
        GeocodeResult geocodeResult = new GeocodeResult(this.getClass());

        /** Validate the input */
        if (!revGeocodeServiceValidator.validateRevGeocodeInput(point, geocodeResult)) {
            return geocodeResult;
        }

        /** Perform reverse geocoding */
        GeocodedAddress revGeocodedAddress = this.httpYahooDao.getGeocodedAddress(point);

        /** Validate and set response */
        if (!revGeocodeServiceValidator.validateGeocodeResult(revGeocodedAddress, geocodeResult)) {
            logger.debug("Reverse geocode failed for point " + point + " using Yahoo Free");
        }
        return geocodeResult;
    }

    /**
    * Batch Reverse Geocode using Yahoo Free. Simply runs the single version in parallel.
    * @param points Points to lookup addresses for
    * @return       ArrayList<GeocodeResult>
    */
    @Override
    public ArrayList<GeocodeResult> reverseGeocode(ArrayList<Point> points)
    {
        return parallelRevGeocodeService.reverseGeocode(this, points);
    }
}