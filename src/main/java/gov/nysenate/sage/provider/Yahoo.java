package gov.nysenate.sage.provider;

import gov.nysenate.sage.dao.provider.YahooDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.service.geo.*;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class Yahoo implements GeocodeService, RevGeocodeService
{
    private final Logger logger = Logger.getLogger(Yahoo.class);
    private YahooDao yahooDao;

    public Yahoo()
    {
        this.yahooDao = new YahooDao();
    }

    /** Geocode Service Implementation -----------------------------------------------------------*/

    @Override
    public GeocodeResult geocode(Address address)
    {
        logger.trace("Performing geocoding using Yahoo Free");
        GeocodeResult geocodeResult = new GeocodeResult(this.getClass());

        if (!GeocodeServiceValidator.isGeocodeServiceActive(this.getClass(), geocodeResult)) {
            return geocodeResult;
        }

        /** Proceed only on valid input */
        if (!GeocodeServiceValidator.validateGeocodeInput(address, geocodeResult)) {
            return geocodeResult;
        }

        /** Retrieve geocoded address from dao */
        GeocodedAddress geocodedAddress = this.yahooDao.getGeocodedAddress(address);

        /** Validate and set result */
        if (!GeocodeServiceValidator.validateGeocodeResult(this.getClass(), geocodedAddress, geocodeResult, true)) {
            logger.warn("Failed to geocode " + address.toString() + " using Yahoo Free!");
        }

        return geocodeResult;
    }

    @Override
    public ArrayList<GeocodeResult> geocode(ArrayList<Address> addresses)
    {
        logger.trace("Performing batch geocoding using Yahoo Free");
        ArrayList<GeocodeResult> geocodeResults = new ArrayList<>();

        /** Retrieve geocoded addresses from dao */
        List<GeocodedAddress> geocodedAddresses = this.yahooDao.getGeocodedAddresses(addresses);

        /** Validate batch */
        if (!GeocodeServiceValidator.validateBatchGeocodeResult(
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
        if (!RevGeocodeServiceValidator.validateRevGeocodeInput(point, geocodeResult)) {
            return geocodeResult;
        }

        /** Perform reverse geocoding */
        GeocodedAddress revGeocodedAddress = this.yahooDao.getGeocodedAddress(point);

        /** Validate and set response */
        if (!RevGeocodeServiceValidator.validateGeocodeResult(revGeocodedAddress, geocodeResult)) {
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
        return ParallelRevGeocodeService.reverseGeocode(this, points);
    }
}