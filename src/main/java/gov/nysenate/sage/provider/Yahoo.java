package gov.nysenate.sage.provider;

import gov.nysenate.sage.dao.provider.YahooDao;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.service.geo.GeocodeService;
import gov.nysenate.sage.service.geo.ParallelGeocodeService;
import gov.nysenate.sage.util.Config;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import static gov.nysenate.sage.model.result.ResultStatus.*;

public class Yahoo implements GeocodeService, Observer
{
    private final Logger logger = Logger.getLogger(Yahoo.class);
    private YahooDao yahooDao;
    private Config config;

    public Yahoo()
    {
        this.config = ApplicationFactory.getConfig();
        this.yahooDao = new YahooDao();
        configure();
        config.notifyOnChange(this);
        logger.info("Initialized Yahoo Adapter");
    }

    private void configure()
    {
        String baseUrl = config.getValue("yahoo.url");
        this.yahooDao.setBaseUrl(baseUrl);
    }

    public void update(Observable o, Object arg)
    {
        configure();
    }

    @Override
    public GeocodeResult geocode(Address address)
    {
        logger.debug("Performing geocoding using Yahoo Free");
        GeocodeResult geocodeResult = new GeocodeResult(this.getClass());

        /** Proceed only on valid input */
        if (address == null) {
            geocodeResult.setStatusCode(MISSING_ADDRESS);
            return geocodeResult;
        }
        else if (address.isEmpty()){
            geocodeResult.setStatusCode(INSUFFICIENT_ADDRESS);
            return geocodeResult;
        }

        GeocodedAddress geocodedAddress = this.yahooDao.getGeocodedAddress(address);

        /** Handle error cases or set GeocodedAddress */
        if (geocodedAddress != null){
            geocodeResult.setGeocodedAddress(geocodedAddress);
            if (!geocodedAddress.isGeocoded()){
                geocodeResult.setStatusCode(NO_GEOCODE_RESULT);
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
    public ArrayList<GeocodeResult> geocode(ArrayList<Address> addresses)
    {
        logger.debug("Performing batch geocoding using Yahoo Free");
        ArrayList<GeocodeResult> geocodeResults = new ArrayList<>();

        List<GeocodedAddress> geocodedAddresses = this.yahooDao.getGeocodedAddresses(addresses);
        for (GeocodedAddress geocodedAddress : geocodedAddresses) {
            GeocodeResult geocodeResult = new GeocodeResult(this.getClass());
            if (geocodedAddress != null){
                geocodeResult.setGeocodedAddress(geocodedAddress);
                if (!geocodedAddress.isGeocoded()){
                    geocodeResult.setStatusCode(NO_GEOCODE_RESULT);
                }
                else {
                    geocodeResult.setStatusCode(SUCCESS);
                }
            }
            else {
                geocodeResult.setStatusCode(RESPONSE_PARSE_ERROR);
            }
            geocodeResults.add(geocodeResult);
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
}