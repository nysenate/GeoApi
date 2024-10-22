package gov.nysenate.sage.provider.geocode;

import gov.nysenate.sage.dao.provider.nysgeo.GeocoderDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.service.geo.ParallelGeocodeService;
import gov.nysenate.sage.service.geo.RevGeocodeServiceValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;

import static gov.nysenate.sage.model.result.ResultStatus.*;

/**
* Base interface for providers of geocoding services.
*/
public abstract class GeocodeService implements RevGeocodeService {
    private static final Logger logger = LoggerFactory.getLogger(GeocodeService.class);
    private final GeocoderDao geocoderDao;
    private final ParallelGeocodeService parallelGeocodeService;

    protected GeocodeService(GeocoderDao geocoderDao, ParallelGeocodeService parallelGeocodeService) {
        this.geocoderDao = geocoderDao;
        this.parallelGeocodeService = parallelGeocodeService;
    }

    @Nonnull
    public abstract Geocoder name();

    public GeocodeResult geocode(@Nonnull Address address) {
        logger.trace("Performing geocoding using {}", geocoderDao.getClass().getSimpleName());
        if (address.isEmpty()) {
            return new GeocodeResult(null, INSUFFICIENT_ADDRESS);
        }
        GeocodedAddress geocodedAddress = geocoderDao.getGeocodedAddress(address);
        ResultStatus status = SUCCESS;
        if (!geocodedAddress.isValidGeocode()) {
            status = NO_GEOCODE_RESULT;
            logger.warn("Failed to geocode {} using {}}!", address, geocoderDao.getClass().getSimpleName());
        }
        return new GeocodeResult(name(), status, GeocodedAddress.from(geocodedAddress, address));
    }

    public List<GeocodeResult> geocode(List<Address> addresses) {
        return parallelGeocodeService.geocode(this, addresses);
    }

    /** {@inheritDoc} */
    @Override
    public GeocodeResult reverseGeocode(Point point) {
        if (!RevGeocodeServiceValidator.validateRevGeocodeInput(point)) {
            return new GeocodeResult(null, MISSING_POINT);
        }
        GeocodedAddress revGeocodedAddress = geocoderDao.getGeocodedAddress(point);
        ResultStatus code = SUCCESS;
        if (revGeocodedAddress == null) {
            code = RESPONSE_PARSE_ERROR;
        }
        else if (revGeocodedAddress.isReverseGeocoded()) {
            code = NO_REVERSE_GEOCODE_RESULT;
        }
        return new GeocodeResult(name(), code, revGeocodedAddress);
    }
}
