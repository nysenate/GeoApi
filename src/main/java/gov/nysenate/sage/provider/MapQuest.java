package gov.nysenate.sage.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nysenate.sage.dao.provider.MapQuestDao;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.service.address.AddressService;
import gov.nysenate.sage.service.geo.GeocodeService;
import gov.nysenate.sage.util.Config;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

/**
 * MapQuest Geocoding provider implementation
 * Documentation: http://www.mapquestapi.com/geocoding/
 *
 * @author Graylin Kim, Ash Islam
 */
public class MapQuest implements AddressService, GeocodeService, Observer
{
    private final Logger logger = Logger.getLogger(MapQuest.class);
    private MapQuestDao mapQuestDao;
    private Config config;
    ObjectMapper mapper;

    public MapQuest()
    {
        this.config = ApplicationFactory.getConfig();
        this.mapQuestDao = new MapQuestDao();
        this.mapper = new ObjectMapper();
        configure();
        config.notifyOnChange(this);
    }

    public void update(Observable o, Object arg)
    {
        configure();
    }

    private void configure()
    {
        this.mapQuestDao.setGeoUrl(config.getValue("mapquest.geo.url"));
        this.mapQuestDao.setRevGeoUrl(config.getValue("mapquest.rev.url"));
        this.mapQuestDao.setKey(config.getValue("mapquest.key"));
    }

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
            geocodeResult = new GeocodeResult(this.getClass());
            geocodeResult.setStatusCode(ResultStatus.RESPONSE_PARSE_ERROR);
        }
        return geocodeResult;
    }

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
        if (point != null) {
            GeocodedAddress revGeocodedAddress = this.mapQuestDao.getGeocodedAddress(point);
            geocodeResult.setGeocodedAddress(revGeocodedAddress);
            if (revGeocodedAddress != null && revGeocodedAddress.isReverseGeocoded()){
                geocodeResult.setStatusCode(ResultStatus.NO_REVERSE_GEOCODE_RESULT);
            }
        }
        else {
            geocodeResult.setStatusCode(ResultStatus.MISSING_POINT);
        }
        return geocodeResult;
    }

    /**
     * Geocodes multiple addresses in a batch. Mapquest provides a native batch
     * geocoding api so this is used for all requests. In order to keep error
     * handling as local as possible the error conditions are set on the result
     * objects as status codes. Thus if the batch geocoding failed, there will be
     * a single result with a ParseError status. If a given address could not be
     * geocoded it will have a NoGeocodeResult status.
     *
     * @param addresses List of Addresses to geocode
     * @return          List of GeocodeResults
     */
    @Override
    public ArrayList<GeocodeResult> geocode(ArrayList<Address> addresses)
    {
        ArrayList<GeocodeResult> geocodeResults = new ArrayList<>();
        ArrayList<GeocodedAddress> geocodedAddresses = this.mapQuestDao.getGeocodedAddresses(addresses);

        if (geocodedAddresses != null){
            for (GeocodedAddress geocodedAddress : geocodedAddresses) {
                GeocodeResult geocodeResult = new GeocodeResult(this.getClass());
                geocodeResult.setGeocodedAddress(geocodedAddress);
                if (!geocodedAddress.isGeocoded()) {
                    geocodeResult.setStatusCode(ResultStatus.NO_GEOCODE_RESULT);
                }
                geocodeResults.add(geocodeResult);
            }
        }
        else {
            /** Return a GeocodeResult with an error status */
            GeocodeResult errorResult = new GeocodeResult();
            errorResult.setStatusCode(ResultStatus.RESPONSE_PARSE_ERROR);
            geocodeResults.add(errorResult);
        }
        return geocodeResults;
    }

    /** Proxy to validate(addresses) */
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
            if (geocodeResult.getStatusCode() == ResultStatus.SUCCESS){
                GeocodedAddress geocodedAddress = geocodeResult.getGeocodedAddress();
                if (geocodedAddress.isAddressValid() && geocodedAddress.isGeocoded()) {
                    valid = true;
                }
                addressResult.setAddress(geocodedAddress.getAddress());
                addressResult.setValidated(valid);
            }
            else {
                addressResult.setValidated(false);
                addressResult.setStatusCode(ResultStatus.NO_ADDRESS_VALIDATE_RESULT);
            }
            addressResults.add(addressResult);
        }
        return addressResults;
    }

    /** No special functionality here. Just proxy to validate */
    @Override
    public AddressResult lookupCityState(Address address)
    {
        return validate(address);
    }

    /** No special functionality here. Just proxy to validate */
    @Override
    public ArrayList<AddressResult> lookupCityState(ArrayList<Address> addresses)
    {
        return validate(addresses);
    }

    /** No special functionality here. Just proxy to validate */
    @Override
    public AddressResult lookupZipCode(Address address)
    {
        return validate(address);
    }

    /** No special functionality here. Just proxy to validate */
    @Override
    public ArrayList<AddressResult> lookupZipCode(ArrayList<Address> addresses)
    {
        return validate(addresses);
    }
}