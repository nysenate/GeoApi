package gov.nysenate.sage.provider.geocode;

import gov.nysenate.sage.dao.provider.google.HttpGoogleDao;
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

@Service
public class GoogleGeocoder implements GeocodeService, RevGeocodeService
{
    private static final Logger logger = LoggerFactory.getLogger(GoogleGeocoder.class);

    private HttpGoogleDao httpGoogleDao;
    private ParallelGeocodeService parallelGeocodeService;
    private ParallelRevGeocodeService parallelRevGeocodeService;
    private GeocodeServiceValidator geocodeServiceValidator;
    private RevGeocodeServiceValidator revGeocodeServiceValidator;

    @Autowired
    public GoogleGeocoder(HttpGoogleDao httpGoogleDao, ParallelGeocodeService parallelGeocodeService,
                          ParallelRevGeocodeService parallelRevGeocodeService,
                          GeocodeServiceValidator geocodeServiceValidator,
                          RevGeocodeServiceValidator revGeocodeServiceValidator)
    {
        this.httpGoogleDao = httpGoogleDao;
        this.parallelGeocodeService = parallelGeocodeService;
        this.parallelRevGeocodeService = parallelRevGeocodeService;
        this.geocodeServiceValidator = geocodeServiceValidator;
        this.revGeocodeServiceValidator = revGeocodeServiceValidator;
        logger.debug("Instantiated Google geocoder");
    }

    /** Geocode Service Implementation -----------------------------------------------------------*/

    @Override
    public GeocodeResult geocode(Address address)
    {
        logger.trace("Performing geocoding using Google");
        GeocodeResult geocodeResult = new GeocodeResult(this.getClass());

        /** Ensure that the geocoder is active, otherwise return error result. */
        if (!geocodeServiceValidator.isGeocodeServiceActive(this.getClass(), geocodeResult)) {
            return geocodeResult;
        }

        /** Proceed only on valid input */
        if (!geocodeServiceValidator.validateGeocodeInput(address, geocodeResult)) {
            return geocodeResult;
        }

        /** Retrieve geocoded address from dao */
        GeocodedAddress geocodedAddress = this.httpGoogleDao.getGeocodedAddress(address);

        /** Validate and set result */
        if (!geocodeServiceValidator.validateGeocodeResult(this.getClass(), geocodedAddress, geocodeResult, true)) {
            logger.warn("Failed to geocode " + address.toString() + " using Google!");
        }

        return geocodeResult;
    }

    @Override
    public ArrayList<GeocodeResult> geocode(ArrayList<Address> addresses)
    {
        return parallelGeocodeService.geocode(this, addresses);
    }

    /** Rev-Geocode Service Implementation -------------------------------------------------------*/

    @Override
    public GeocodeResult reverseGeocode(Point point)
    {
        GeocodeResult geocodeResult = new GeocodeResult(this.getClass());

        /** Validate the input */
        if (!revGeocodeServiceValidator.validateRevGeocodeInput(point, geocodeResult)) {
            return geocodeResult;
        }

        /** Perform reverse geocoding */
        GeocodedAddress revGeocodedAddress = this.httpGoogleDao.getGeocodedAddress(point);

        /** Validate and set response */
        if (!revGeocodeServiceValidator.validateGeocodeResult(revGeocodedAddress, geocodeResult)) {
            logger.debug("Reverse geocode failed for point " + point + " using Google");
        }
        return geocodeResult;
    }

    @Override
    public ArrayList<GeocodeResult> reverseGeocode(ArrayList<Point> points)
    {
        return parallelRevGeocodeService.reverseGeocode(this, points);
    }
}