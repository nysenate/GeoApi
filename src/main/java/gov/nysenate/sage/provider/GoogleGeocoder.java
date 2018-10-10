package gov.nysenate.sage.provider;

import gov.nysenate.sage.dao.provider.GoogleDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.service.geo.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

public class GoogleGeocoder implements GeocodeService, RevGeocodeService
{
    private static final Logger logger = LogManager.getLogger(GoogleGeocoder.class);

    private GoogleDao googleDao;

    public GoogleGeocoder()
    {
        googleDao = new GoogleDao();
        logger.debug("Instantiated Google geocoder");
    }

    /** Geocode Service Implementation -----------------------------------------------------------*/

    @Override
    public GeocodeResult geocode(Address address)
    {
        logger.trace("Performing geocoding using Google");
        GeocodeResult geocodeResult = new GeocodeResult(this.getClass());

        /** Ensure that the geocoder is active, otherwise return error result. */
        if (!GeocodeServiceValidator.isGeocodeServiceActive(this.getClass(), geocodeResult)) {
            return geocodeResult;
        }

        /** Proceed only on valid input */
        if (!GeocodeServiceValidator.validateGeocodeInput(address, geocodeResult)) {
            return geocodeResult;
        }

        /** Retrieve geocoded address from dao */
        GeocodedAddress geocodedAddress = this.googleDao.getGeocodedAddress(address);

        /** Validate and set result */
        if (!GeocodeServiceValidator.validateGeocodeResult(this.getClass(), geocodedAddress, geocodeResult, true)) {
            logger.warn("Failed to geocode " + address.toString() + " using Google!");
        }

        return geocodeResult;
    }

    @Override
    public ArrayList<GeocodeResult> geocode(ArrayList<Address> addresses)
    {
        return ParallelGeocodeService.geocode(this, addresses);
    }

    /** Rev-Geocode Service Implementation -------------------------------------------------------*/

    @Override
    public GeocodeResult reverseGeocode(Point point)
    {
        GeocodeResult geocodeResult = new GeocodeResult(this.getClass());

        /** Validate the input */
        if (!RevGeocodeServiceValidator.validateRevGeocodeInput(point, geocodeResult)) {
            return geocodeResult;
        }

        /** Perform reverse geocoding */
        GeocodedAddress revGeocodedAddress = this.googleDao.getGeocodedAddress(point);

        /** Validate and set response */
        if (!RevGeocodeServiceValidator.validateGeocodeResult(revGeocodedAddress, geocodeResult)) {
            logger.debug("Reverse geocode failed for point " + point + " using Google");
        }
        return geocodeResult;
    }

    @Override
    public ArrayList<GeocodeResult> reverseGeocode(ArrayList<Point> points)
    {
        return ParallelRevGeocodeService.reverseGeocode(this, points);
    }
}