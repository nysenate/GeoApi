package gov.nysenate.sage.provider;

import gov.nysenate.sage.dao.provider.MapQuestDao;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.service.address.AddressService;
import gov.nysenate.sage.service.geo.*;
import gov.nysenate.sage.util.Config;
import org.apache.log4j.Logger;

import java.util.*;

import static gov.nysenate.sage.model.result.ResultStatus.*;
import static gov.nysenate.sage.service.geo.GeocodeServiceValidator.validateBatchGeocodeResult;

/**
 * MapQuest Geocoding provider implementation
 * Documentation: http://www.mapquestapi.com/geocoding/
 *
 * @author Graylin Kim, Ash Islam
 */
public class MapQuest implements AddressService, GeocodeService, RevGeocodeService
{
    private final Logger logger = Logger.getLogger(MapQuest.class);
    private MapQuestDao mapQuestDao;
    private Config config;

    public MapQuest()
    {
        this.config = ApplicationFactory.getConfig();
        this.mapQuestDao = new MapQuestDao();
        configure();
    }

    private void configure()
    {
        this.mapQuestDao.setGeoUrl(config.getValue("mapquest.geo.url"));
        this.mapQuestDao.setRevGeoUrl(config.getValue("mapquest.rev.url"));
        this.mapQuestDao.setKey(config.getValue("mapquest.key"));
    }

    /** Geocode Service Implementation ------------------------------------------------------------------*/

    /**
     * Geocodes a single address by delegating to the batch geocoding method
     * @param address Address to geocode
     * @return        GeocodeResult
     */
    @Override
    public GeocodeResult geocode(Address address)
    {
        GeocodeResult geocodeResult;
        ArrayList<GeocodeResult> results = geocode(new ArrayList<>(Arrays.asList(address)));
        if (results != null && results.size() > 0){
            geocodeResult = results.get(0);
        }
        else {
            geocodeResult = new GeocodeResult(this.getClass(), RESPONSE_PARSE_ERROR);
        }
        return geocodeResult;
    }

    /**
    * Geocodes multiple addresses in a batch. Mapquest provides a native batch
    * geocoding api so this is used for all requests.
    * @param addresses List of Addresses to geocode
    * @return          List of GeocodeResults
    */
    @Override
    public ArrayList<GeocodeResult> geocode(ArrayList<Address> addresses)
    {
        logger.debug("Performing geocoding using MapQuest");
        ArrayList<GeocodeResult> geocodeResults = new ArrayList<>();

        /** Retrieve geocoded addresses from dao */
        List<GeocodedAddress> geocodedAddresses = this.mapQuestDao.getGeocodedAddresses(addresses);

        /** Validate and return */
        if (!validateBatchGeocodeResult(this.getClass(), addresses, geocodeResults, geocodedAddresses)){
            logger.warn("Failed to batch geocode using Yahoo!");
        }
        return geocodeResults;
    }

    /** Reverse Geocode Service Implementation ----------------------------------------------------------*/

    /**
    * Given a lat lng pair return the address that is closest to that point.
    * Mapquest does not have a bulk option for this operation.
    * @param point     Point to reverse geocode.
    * @return          GeocodeResult
    */
    @Override
    public GeocodeResult reverseGeocode(Point point)
    {
        GeocodeResult geocodeResult = new GeocodeResult(this.getClass());

        /** Validate the input */
        if (!RevGeocodeServiceValidator.validateRevGeocodeInput(point, geocodeResult)) {
            return geocodeResult;
        }

        /** Perform reverse geocoding */
        GeocodedAddress revGeocodedAddress = this.mapQuestDao.getGeocodedAddress(point);

        /** Validate and set response */
        if (!RevGeocodeServiceValidator.validateGeocodeResult(revGeocodedAddress, geocodeResult)) {
            logger.warn("Reverse geocode failed for point " + point + " using MapQuest");
        }
        return geocodeResult;
    }

    /**
    * Performs batch reverseGeocodes
    * @param points    List<Point>
    * @return          List<GeocodeResult>
    */
    @Override
    public ArrayList<GeocodeResult> reverseGeocode(ArrayList<Point> points)
    {
        return ParallelRevGeocodeService.reverseGeocode(this, points);
    }

    /** Address Service Implementation ------------------------------------------------------------------*/

    /** Proxy to <code>validate(addresses)</code> */
    @Override
    public AddressResult validate(Address address)
    {
        ArrayList<AddressResult> addressResults = validate(new ArrayList<>(Arrays.asList(address)));
        return (addressResults != null) ? addressResults.get(0) : null;
    }

    /** MapQuest's geocoding service auto corrects addresses so that service can be extended here. */
    @Override
    public ArrayList<AddressResult> validate(ArrayList<Address> addresses)
    {
        ArrayList<GeocodeResult> geocodeResults = this.geocode(addresses);
        ArrayList<AddressResult> addressResults = new ArrayList<>();

        /** Loop through the geocode results and retrieve the address objects. Perform sanity
         *  checks to ensure that they are actually validated. */
        for (GeocodeResult geocodeResult : geocodeResults) {
            AddressResult addressResult = new AddressResult(this.getClass());
            boolean valid = false;
            if (geocodeResult.getStatusCode().equals(SUCCESS)){
                GeocodedAddress geocodedAddress = geocodeResult.getGeocodedAddress();
                if (geocodedAddress.isAddressValid() && geocodedAddress.isGeocoded()) {
                    valid = true;
                }
                addressResult.setAddress(geocodedAddress.getAddress());
                addressResult.setValidated(valid);
            }
            else {
                addressResult.setValidated(false);
                addressResult.setStatusCode(NO_ADDRESS_VALIDATE_RESULT);
            }
            addressResults.add(addressResult);
        }
        return addressResults;
    }

    /** Proxy to <code>validate</code> */
    @Override
    public AddressResult lookupCityState(Address address)
    {
        return validate(address);
    }

    /** Proxy to <code>validate</code> */
    @Override
    public ArrayList<AddressResult> lookupCityState(ArrayList<Address> addresses)
    {
        return validate(addresses);
    }

    /** Proxy to <code>validate</code> */
    @Override
    public AddressResult lookupZipCode(Address address)
    {
        return validate(address);
    }

    /** Proxy to <code>validate</code> */
    @Override
    public ArrayList<AddressResult> lookupZipCode(ArrayList<Address> addresses)
    {
        return validate(addresses);
    }
}