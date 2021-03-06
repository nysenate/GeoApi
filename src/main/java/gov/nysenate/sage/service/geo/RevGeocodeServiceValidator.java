package gov.nysenate.sage.service.geo;

import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.GeocodeResult;
import org.springframework.stereotype.Service;

import static gov.nysenate.sage.model.result.ResultStatus.*;

@Service
public class RevGeocodeServiceValidator
{
    /**
     * Basic null checks on rev-geocode inputs. Sets errors to the geocode result
     * @param point         Point to validate
     * @param geocodeResult GeocodeResult to set status codes to
     * @return              True if valid input, false otherwise
     */
    public static boolean validateRevGeocodeInput(Point point, GeocodeResult geocodeResult)
    {
        if (point == null) {
            geocodeResult.setStatusCode(MISSING_POINT);
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
            if (!geocodedAddress.isReverseGeocoded()){
                geocodeResult.setStatusCode(NO_REVERSE_GEOCODE_RESULT);
                return false;
            }
            geocodeResult.setStatusCode(SUCCESS);
            return true;
        }
        geocodeResult.setStatusCode(RESPONSE_PARSE_ERROR);
        return false;
    }
}
