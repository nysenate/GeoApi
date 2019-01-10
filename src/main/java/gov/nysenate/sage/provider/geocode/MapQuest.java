package gov.nysenate.sage.provider.geocode;

import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.provider.mapquest.HttpMapQuestDao;
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

    @Autowired
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

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public ArrayList<GeocodeResult> reverseGeocode(ArrayList<Point> points)
    {
        return parallelRevGeocodeService.reverseGeocode(this, points);
    }
}