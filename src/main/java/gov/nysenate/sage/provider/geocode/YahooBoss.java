package gov.nysenate.sage.provider.geocode;

import gov.nysenate.sage.dao.provider.yahoo.HttpYahooBossDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.service.geo.GeocodeServiceValidator;
import gov.nysenate.sage.service.geo.ParallelGeocodeService;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
* Yahoo Boss - Commercial geo-coding service from Yahoo
*/
@Service
public class YahooBoss implements GeocodeService
{
    private final Logger logger = LoggerFactory.getLogger(YahooBoss.class);
    private HttpYahooBossDao httpYahooBossDao;
    private GeocodeServiceValidator geocodeServiceValidator;
    private ParallelGeocodeService parallelGeocodeService;

    @Autowired
    public YahooBoss(HttpYahooBossDao httpYahooBossDao, GeocodeServiceValidator geocodeServiceValidator,
                     ParallelGeocodeService parallelGeocodeService)
    {
        this.httpYahooBossDao = httpYahooBossDao;
        this.geocodeServiceValidator = geocodeServiceValidator;
        this.parallelGeocodeService = parallelGeocodeService;
    }

    /** {@inheritDoc} */
    public GeocodeResult geocode(Address address)
    {
        logger.debug("Performing geocoding using Yahoo Boss");
        GeocodeResult geocodeResult = new GeocodeResult(this.getClass());

        /** Ensure that the geocoder is active, otherwise return error result. */
        if (!geocodeServiceValidator.isGeocodeServiceActive(this.getClass(), geocodeResult)) {
            return geocodeResult;
        }

        /** Proceed if valid address */
        if (!geocodeServiceValidator.validateGeocodeInput(address, geocodeResult)){
            return geocodeResult;
        }

        /** Retrieve geocoded address from dao */
        GeocodedAddress geocodedAddress = this.httpYahooBossDao.getGeocodedAddress(address);

        /** Validate and set result */
        if (!geocodeServiceValidator.validateGeocodeResult(this.getClass(), geocodedAddress, geocodeResult, true)) {
            logger.warn("Failed to geocode " + address.toString() + " using Yahoo Boss!");
        }
        return geocodeResult;
    }

    /** {@inheritDoc} */
    public ArrayList<GeocodeResult> geocode(ArrayList<Address> addresses)
    {
        return parallelGeocodeService.geocode(this, addresses);
    }
}