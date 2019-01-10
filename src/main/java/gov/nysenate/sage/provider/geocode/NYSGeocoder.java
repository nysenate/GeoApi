package gov.nysenate.sage.provider.geocode;

import gov.nysenate.sage.dao.provider.nysgeo.HttpNYSGeoDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.service.geo.*;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class NYSGeocoder implements GeocodeService, RevGeocodeService {

    private static final Logger logger = LoggerFactory.getLogger(NYSGeocoder.class);

    private HttpNYSGeoDao httpNysGeoDao;
    private GeocodeServiceValidator geocodeServiceValidator;
    private RevGeocodeServiceValidator revGeocodeServiceValidator;
    private ParallelGeocodeService parallelGeocodeService;
    private ParallelRevGeocodeService parallelRevGeocodeService;

    @Autowired
    public NYSGeocoder(HttpNYSGeoDao httpNysGeoDao, GeocodeServiceValidator geocodeServiceValidator,
                       RevGeocodeServiceValidator revGeocodeServiceValidator,
                       ParallelGeocodeService parallelGeocodeService,
                       ParallelRevGeocodeService parallelRevGeocodeService) {
        this.httpNysGeoDao = httpNysGeoDao;
        this.geocodeServiceValidator = geocodeServiceValidator;
        this.revGeocodeServiceValidator = revGeocodeServiceValidator;
        this.parallelGeocodeService = parallelGeocodeService;
        this.parallelRevGeocodeService = parallelRevGeocodeService;
        logger.debug("Instantiated NYS Geocoder");
    }

    /** Geocode Service Implementation -----------------------------------------------------------*/

    /** {@inheritDoc} */
    @Override
    public GeocodeResult geocode(Address address)
    {
        logger.trace("Performing geocoding using NYS Geocoder");
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
        GeocodedAddress geocodedAddress = this.httpNysGeoDao.getGeocodedAddress(address);

        /** Validate and set result */
        if (!geocodeServiceValidator.validateGeocodeResult(this.getClass(), geocodedAddress, geocodeResult, true)) {
            logger.warn("Failed to geocode " + address.toString() + " using NYS Geocoder!");
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
    public GeocodeResult reverseGeocode(Point point)
    {
        GeocodeResult geocodeResult = new GeocodeResult(this.getClass());

        /** Validate the input */
        if (!revGeocodeServiceValidator.validateRevGeocodeInput(point, geocodeResult)) {
            return geocodeResult;
        }

        /** Perform reverse geocoding */
        GeocodedAddress revGeocodedAddress = this.httpNysGeoDao.getGeocodedAddress(point);

        /** Validate and set response */
        if (!revGeocodeServiceValidator.validateGeocodeResult(revGeocodedAddress, geocodeResult)) {
            logger.debug("Reverse geocode failed for point " + point + " using NYS Geocoder");
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
