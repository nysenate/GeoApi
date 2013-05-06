package gov.nysenate.sage.provider;

import gov.nysenate.sage.dao.provider.YahooDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.service.geo.GeocodeService;
import gov.nysenate.sage.service.geo.GeocodeServiceValidator;
import gov.nysenate.sage.service.geo.ParallelGeocodeService;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static gov.nysenate.sage.model.result.ResultStatus.*;

public class Yahoo implements GeocodeService
{
    private final Logger logger = Logger.getLogger(Yahoo.class);
    private YahooDao yahooDao;

    public Yahoo()
    {
        this.yahooDao = new YahooDao();
        logger.info("Initialized Yahoo Adapter");
    }

    @Override
    public GeocodeResult geocode(Address address)
    {
        logger.debug("Performing geocoding using Yahoo Free");
        GeocodeResult geocodeResult = new GeocodeResult(this.getClass());

        /** Proceed only on valid input */
        if (!GeocodeServiceValidator.validateGeocodeInput(address, geocodeResult)) return geocodeResult;

        /** Retrieve geocoded address from dao */
        GeocodedAddress geocodedAddress = this.yahooDao.getGeocodedAddress(address);

        /** Validate and return */
        if (!GeocodeServiceValidator.validateGeocodeResult(geocodedAddress, geocodeResult)) {
            logger.warn("Failed to geocode " + address.toString() + " using Yahoo!");
        }
        return geocodeResult;
    }

    @Override
    public ArrayList<GeocodeResult> geocode(ArrayList<Address> addresses)
    {
        logger.debug("Performing batch geocoding using Yahoo Free");
        ArrayList<GeocodeResult> geocodeResults = new ArrayList<>();

        /** Retrieve geocoded addresses from dao */
        List<GeocodedAddress> geocodedAddresses = this.yahooDao.getGeocodedAddresses(addresses);

        /** Validate and return */
        if (!GeocodeServiceValidator.validateBatchGeocodeResult(this.getClass(), addresses, geocodeResults,
                                                                geocodedAddresses)){
            logger.warn("Failed to batch geocode using Yahoo!");
        }
        return geocodeResults;
    }

    @Override
    public GeocodeResult reverseGeocode(Point point)
    {
        GeocodeResult geocodeResult = new GeocodeResult(this.getClass());

        /** Validate the input */
        if (point == null) {
            geocodeResult.setStatusCode(MISSING_POINT);
        }

        GeocodedAddress geocodedAddress = this.yahooDao.getGeocodedAddress(point);

        /** Handle error cases or set GeocodedAddress */
        if (geocodedAddress != null){
            geocodeResult.setGeocodedAddress(geocodedAddress);
            if (!geocodedAddress.isReverseGeocoded()){
                geocodeResult.setStatusCode(NO_REVERSE_GEOCODE_RESULT);
            }
            else {
                geocodeResult.setStatusCode(SUCCESS);
            }
        }
        else {
            geocodeResult.setStatusCode(RESPONSE_PARSE_ERROR);
        }
        return geocodeResult;
    }

    @Override
    public ArrayList<GeocodeResult> reverseGeocode(ArrayList<Point> points) {
        return ParallelGeocodeService.reverseGeocode(this, points);
    }
}