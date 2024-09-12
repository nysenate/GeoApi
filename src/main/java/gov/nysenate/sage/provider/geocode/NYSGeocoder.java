package gov.nysenate.sage.provider.geocode;

import gov.nysenate.sage.dao.provider.nysgeo.HttpNYSGeoDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.service.geo.GeocodeServiceValidator;
import gov.nysenate.sage.service.geo.ParallelGeocodeService;
import gov.nysenate.sage.service.geo.RevGeocodeServiceValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NYSGeocoder implements GeocodeService, RevGeocodeService {
    private static final Logger logger = LoggerFactory.getLogger(NYSGeocoder.class);
    private final HttpNYSGeoDao httpNysGeoDao;
    private final GeocodeServiceValidator geocodeServiceValidator;
    private final ParallelGeocodeService parallelGeocodeService;

    @Autowired
    public NYSGeocoder(HttpNYSGeoDao httpNysGeoDao, GeocodeServiceValidator geocodeServiceValidator,
                       ParallelGeocodeService parallelGeocodeService) {
        this.httpNysGeoDao = httpNysGeoDao;
        this.geocodeServiceValidator = geocodeServiceValidator;
        this.parallelGeocodeService = parallelGeocodeService;
        logger.debug("Instantiated NYS Geocoder");
    }

    /** {@inheritDoc} */
    @Override
    public GeocodeResult geocode(Address address) {
        logger.trace("Performing geocoding using NYS Geocoder");
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
        GeocodedAddress geocodedAddress = this.httpNysGeoDao.getGeocodedAddress(address);

        /** Validate and set result */
        if (!geocodeServiceValidator.validateGeocodeResult(this.getClass(), geocodedAddress, geocodeResult, true)) {
            logger.warn("Failed to geocode " + address.toString() + " using NYS Geocoder!");
        }

        return geocodeResult;
    }

    /** {@inheritDoc} */
    @Override
    public List<GeocodeResult> geocode(List<Address> addresses) {
        return parallelGeocodeService.geocode(this, addresses);
    }

    /** {@inheritDoc} */
    @Override
    public GeocodeResult reverseGeocode(Point point) {
        GeocodeResult geocodeResult = new GeocodeResult(this.getClass());

        /** Validate the input */
        if (!RevGeocodeServiceValidator.validateRevGeocodeInput(point, geocodeResult)) {
            return geocodeResult;
        }

        /** Perform reverse geocoding */
        GeocodedAddress revGeocodedAddress = this.httpNysGeoDao.getGeocodedAddress(point);

        /** Validate and set response */
        if (!RevGeocodeServiceValidator.validateGeocodeResult(revGeocodedAddress, geocodeResult)) {
            logger.debug("Reverse geocode failed for point {} using NYS Geocoder", point);
        }
        return geocodeResult;
    }
}
