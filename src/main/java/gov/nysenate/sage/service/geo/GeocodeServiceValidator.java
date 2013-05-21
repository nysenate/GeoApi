package gov.nysenate.sage.service.geo;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.address.GeocodedStreetAddress;
import gov.nysenate.sage.model.result.GeocodeResult;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static gov.nysenate.sage.model.result.ResultStatus.*;

/**
* Utility class for validating geocode requests and responses.
*/
public abstract class GeocodeServiceValidator
{
    private static final Logger logger = Logger.getLogger(GeocodeServiceValidator.class);

    /**
     * Basic null checks on geocode inputs. Sets errors to the geocode result
     * @param address       Address to validate
     * @param geocodeResult GeocodeResult to set status codes to
     * @return              True if valid input, false otherwise
     */
    public static boolean validateGeocodeInput(Address address, GeocodeResult geocodeResult)
    {
        if (address == null) {
            geocodeResult.setStatusCode(MISSING_ADDRESS);
            return false;
        }
        else if (address.isEmpty()){
            geocodeResult.setStatusCode(INSUFFICIENT_ADDRESS);
            return false;
        }
        return true;
    }

    /**
     * Perform validation on a GeocodedAddress that is meant to be encapsulated in a GeocodeResult.
     * If validation fails the status code on the result object will be set.
     * @param geocodedAddress   The resulting GeocodeAddress
     * @param geocodeResult     The GeocodeResult to set
     * @return                  True if valid GeocodedAddress, false otherwise
     */
    public static boolean validateGeocodeResult(GeocodedAddress geocodedAddress, GeocodeResult geocodeResult)
    {
        if (geocodedAddress != null){
            geocodeResult.setGeocodedAddress(geocodedAddress);
            if (!geocodedAddress.isGeocoded()){
                geocodeResult.setStatusCode(NO_GEOCODE_RESULT);
                return false;
            }
            geocodeResult.setStatusCode(SUCCESS);
            return true;
        }
        geocodeResult.setStatusCode(RESPONSE_PARSE_ERROR);
        return false;
    }

    public static boolean validateGeocodeResult(GeocodedStreetAddress geoStreetAddress, GeocodeResult geocodeResult)
    {
        if (geoStreetAddress != null) {
            return validateGeocodeResult(geoStreetAddress.toGeocodedAddress(), geocodeResult);
        }
        geocodeResult.setStatusCode(NO_GEOCODE_RESULT);
        return false;
    }

    public static boolean validateBatchGeocodeResult(Class source, ArrayList<Address> addresses, ArrayList<GeocodeResult> geocodeResults,
                                                      List<GeocodedAddress> geocodedAddresses)
    {
        /** Make sure the result array is empty at first */
        geocodeResults.clear();

        /** Check each geocoded address to set the result status accordingly */
        if (geocodedAddresses != null && geocodedAddresses.size() == addresses.size()) {
            geocodeResults.clear();
            for (GeocodedAddress geocodedAddress : geocodedAddresses) {
                GeocodeResult geocodeResult = new GeocodeResult(source.getClass());
                validateGeocodeResult(geocodedAddress, geocodeResult);
                geocodeResults.add(geocodeResult);
            }
            return true;
        }
        /** If the batch is invalid, return a collection of invalidated geocode results. */
        else {
            logger.warn("Invalidating this batch! The results returned do not match the addresses given.");
            for (Address a : addresses) {
                geocodeResults.add(new GeocodeResult(source, NO_GEOCODE_RESULT, new GeocodedAddress(a)));
            }
        }
        return false;
    }
}
