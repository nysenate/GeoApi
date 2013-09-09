package gov.nysenate.sage.provider;

import gov.nysenate.sage.dao.provider.YahooBossDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.service.geo.GeocodeService;
import gov.nysenate.sage.service.geo.GeocodeServiceValidator;
import gov.nysenate.sage.service.geo.ParallelGeocodeService;
import org.apache.log4j.Logger;

import java.util.ArrayList;

/**
* Yahoo Boss - Commercial geo-coding service from Yahoo
*/
public class YahooBoss implements GeocodeService
{
    private final Logger logger = Logger.getLogger(YahooBoss.class);
    private YahooBossDao yahooBossDao;

    public YahooBoss()
    {
        this.yahooBossDao = new YahooBossDao();
        logger.info("Initialized Yahoo Adapter");
    }

    /**
     * Perform geocoding using Yahoo Boss
     * @param address  Address to geocode
     * @return         GeocodeResult
     */
    public GeocodeResult geocode(Address address)
    {
        logger.debug("Performing geocoding using Yahoo Boss");
        GeocodeResult geocodeResult = new GeocodeResult(this.getClass());

        /** Proceed only on valid input */
        if (!GeocodeServiceValidator.validateGeocodeInput(address, geocodeResult)) {
            return geocodeResult;
        }

        /** Retrieve geocoded address from dao */
        GeocodedAddress geocodedAddress = this.yahooBossDao.getGeocodedAddress(address);

        /** Validate and set result */
        if (!GeocodeServiceValidator.validateGeocodeResult(geocodedAddress, geocodeResult)) {
            logger.warn("Failed to geocode " + address.toString() + " using Yahoo Boss!");
        }
        return geocodeResult;
    }

    /**
    * Yahoo Boss doesn't implement batch geocoding so we use the single address geocoding
    * method in parallel for performance improvements on our end.
    * @param addresses Addresses to batch geocode
    * @return ArrayList<GeocodeResult>
    */
    public ArrayList<GeocodeResult> geocode(ArrayList<Address> addresses)
    {
        return ParallelGeocodeService.geocode(this, addresses);
    }
}