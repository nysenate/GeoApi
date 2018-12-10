package gov.nysenate.sage.provider.geocode;

import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.provider.mapquest.HttpMapQuestDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.service.geo.*;
import org.springframework.stereotype.Service;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static gov.nysenate.sage.model.result.ResultStatus.*;

/**
 * MapQuest Geocoding provider implementation
 * Documentation: http://www.mapquestapi.com/geocoding/
 *
 * @author Graylin Kim, Ash Islam
 */
@Service
public class MapQuest implements GeocodeService, RevGeocodeService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private HttpMapQuestDao httpMapQuestDao;

    private GeocodeServiceValidator geocodeServiceValidator;
    private ParallelRevGeocodeService parallelRevGeocodeService;
    private BaseDao baseDao;
    private Environment env;

    public MapQuest(BaseDao baseDao, HttpMapQuestDao httpMapQuestDao, GeocodeServiceValidator geocodeServiceValidator,
                    ParallelRevGeocodeService parallelRevGeocodeService, Environment env)
    {
        this.baseDao = baseDao;
        this.env = env;
        this.geocodeServiceValidator = geocodeServiceValidator;
        this.parallelRevGeocodeService = parallelRevGeocodeService;
        this.httpMapQuestDao = httpMapQuestDao;

    }

    /** Geocode Service Implementation ------------------------------------------------------------------*/

    /**
     * Geocodes a single address by delegating to the batch geocoding method
     * @param address Address to geocode
     * @return        GeocodeResult
     */
    @Override
    public GeocodeResult geocode(Address address)
    {
        GeocodeResult geocodeResult = new GeocodeResult(this.getClass());

        /** Proceed only on valid input. */
        if (!GeocodeServiceValidator.validateGeocodeInput(address, geocodeResult)) {
            return geocodeResult;
        }

        /** Delegate to the batch method. */
        ArrayList<GeocodeResult> results = geocode(new ArrayList<>(Arrays.asList(address)));
        if (results != null && results.size() > 0) {
            geocodeResult = results.get(0);
        }
        else {
            geocodeResult = new GeocodeResult(this.getClass(), RESPONSE_PARSE_ERROR);
        }
        return geocodeResult;
    }

    /**
    * Geocodes multiple addresses in a batch. Mapquest provides a native batch
    * geocoding api so this is used for all requests.
    * @param addresses List of Addresses to geocode
    * @return          List of GeocodeResults
    */
    @Override
    public ArrayList<GeocodeResult> geocode(ArrayList<Address> addresses)
    {
        logger.trace("Performing geocoding using MapQuest");
        ArrayList<GeocodeResult> geocodeResults = new ArrayList<>();

        /** Ensure that the geocoder is active, otherwise return list of error results. */
        if (!geocodeServiceValidator.isGeocodeServiceActive(this.getClass(), geocodeResults, addresses.size())) {
            return geocodeResults;
        }

        /** Retrieve geocoded addresses from dao */
        List<GeocodedAddress> geocodedAddresses = this.httpMapQuestDao.getGeocodedAddresses(addresses);

        /** Validate and return */
        if (!geocodeServiceValidator.validateBatchGeocodeResult(this.getClass(), addresses, geocodeResults, geocodedAddresses, true)){
            logger.warn("Failed to batch geocode using MapQuest!");
        }
        return geocodeResults;
    }

    /** Reverse Geocode Service Implementation ----------------------------------------------------------*/

    /**
    * Given a lat lng pair return the address that is closest to that point.
    * Mapquest does not have a bulk option for this operation.
    * @param point     Point to reverse geocode.
    * @return          GeocodeResult
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
        GeocodedAddress revGeocodedAddress = this.httpMapQuestDao.getGeocodedAddress(point);

        /** Validate and set response */
        if (!RevGeocodeServiceValidator.validateGeocodeResult(revGeocodedAddress, geocodeResult)) {
            logger.warn("Reverse geocode failed for point " + point + " using MapQuest");
        }
        return geocodeResult;
    }

    /**
    * Performs batch reverseGeocodes
    * @param points    List<Point>
    * @return          List<GeocodeResult>
    */
    @Override
    public ArrayList<GeocodeResult> reverseGeocode(ArrayList<Point> points)
    {
        return parallelRevGeocodeService.reverseGeocode(this, points);
    }
}