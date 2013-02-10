package gov.nysenate.sage.service.geo;

import gov.nysenate.sage.Address;
import gov.nysenate.sage.Result;

import java.util.ArrayList;

public interface GeoCodeServiceInterface {
    /**
     *  Accepts single address or a list of addresses and returns geocoded result:
     *      result.status_code = 0 on success
     *      result.messages = list of response messages if failure
     *      result.address = best match address for result if successful
     *      result.addresses = list of all returned address for result if successful
     *
     *  If any incoming addresses are null a NULL result value is returned in its place.
     *  For bulk requests this ensures that index matching from the incoming address
     *  list to the returned results list is kept.
     *
     *  Errors loading the API and parsing the API response are wrapped in a GeoException
     *  and thrown, terminating the geo-coding process and throwing away previous results.
     */

     public Result geocode(Address address) throws GeoCodeService;
     public ArrayList<Result> geocode(ArrayList<Address> addresses, Address.TYPE hint) throws GeoCodeService;

}
