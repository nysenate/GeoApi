package gov.nysenate.sage.provider.geocode;

import gov.nysenate.sage.dao.provider.mapquest.HttpMapQuestDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.service.geo.GeocodeServiceValidator;
import gov.nysenate.sage.service.geo.RevGeocodeServiceValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static gov.nysenate.sage.model.result.ResultStatus.RESPONSE_PARSE_ERROR;

/**
 * MapQuest Geocoding provider implementation
 * Documentation: http://www.mapquestapi.com/geocoding/
 *
 * @author Graylin Kim, Ash Islam
 */
@Service
public class MapQuest implements GeocodeService, RevGeocodeService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final HttpMapQuestDao httpMapQuestDao;
    private final GeocodeServiceValidator geocodeServiceValidator;

    @Autowired
    public MapQuest(HttpMapQuestDao httpMapQuestDao, GeocodeServiceValidator geocodeServiceValidator) {
        this.geocodeServiceValidator = geocodeServiceValidator;
        this.httpMapQuestDao = httpMapQuestDao;

    }

    /** {@inheritDoc} */
    @Override
    public GeocodeResult geocode(Address address) {
        GeocodeResult geocodeResult = new GeocodeResult(this.getClass());

        /** Proceed only on valid input. */
        if (!GeocodeServiceValidator.validateGeocodeInput(address, geocodeResult)) {
            return geocodeResult;
        }

        /** Delegate to the batch method. */
        List<GeocodeResult> results = geocode(List.of(address));
        if (results != null && !results.isEmpty()) {
            geocodeResult = results.get(0);
        }
        else {
            geocodeResult = new GeocodeResult(this.getClass(), RESPONSE_PARSE_ERROR);
        }
        return geocodeResult;
    }

    /** {@inheritDoc} */
    @Override
    public ArrayList<GeocodeResult> geocode(List<Address> addresses) {
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
    public GeocodeResult reverseGeocode(Point point) {
        GeocodeResult geocodeResult = new GeocodeResult(this.getClass());

        /** Validate the input */
        if (!RevGeocodeServiceValidator.validateRevGeocodeInput(point, geocodeResult)) {
            return geocodeResult;
        }

        /** Perform reverse geocoding */
        GeocodedAddress revGeocodedAddress = this.httpMapQuestDao.getGeocodedAddress(point);

        /** Validate and set response */
        if (!RevGeocodeServiceValidator.validateGeocodeResult(revGeocodedAddress, geocodeResult)) {
            logger.warn("Reverse geocode failed for point {} using MapQuest", point);
        }
        return geocodeResult;
    }
}