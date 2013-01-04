package gov.nysenate.sage.service;

import gov.nysenate.sage.Address;
import gov.nysenate.sage.Result;
import gov.nysenate.sage.adapter.MapQuest;
import gov.nysenate.sage.adapter.OSM;
import gov.nysenate.sage.adapter.RubyGeocoder;
import gov.nysenate.sage.adapter.Yahoo;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author graylin
 *
 * Service layer that wraps all the geocode and reverse geocode adapters into
 * a simple interface.
 */
public class GeoService {

    @SuppressWarnings("serial")
    public static class GeoException extends Exception {
        public GeoException() {}
        public GeoException(String msg) { super(msg); }
        public GeoException(String msg, Throwable t) { super(msg, t); }
        public GeoException(Throwable t) { super(t); }
    }

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
     *
     *   TODO: Implement Either:
     *      A) Attach previous results to exception instead of throwing them away.
     *      B) Struggle on after exceptions via NULL results in the results list.
     */
    public interface GeocodeInterface {
        public Result geocode(Address address) throws GeoException;
        public ArrayList<Result> geocode(ArrayList<Address> addresses, Address.TYPE hint) throws GeoException;
    }

    // All adapters able to reverse geocode must implement this interface.
    public interface ReverseGeocodeInterface {
        public Result reverseGeocode(Address address) throws GeoException;
        public ArrayList<Result> reverseGeocode(ArrayList<Address> addresses, Address.TYPE hint) throws GeoException;
    }

    // Adapters implementing each interface are stored in hashes so that a class
    // using the GeoService can invoke specific adapters by name.
    private final HashMap<String, GeocodeInterface> geoAdapters = new HashMap<String, GeocodeInterface>();
    // private final HashMap<String, ReverseGeocodeInterface> revAdapters = new HashMap<String, ReverseGeocodeInterface>();


    public GeoService() throws Exception {
        Yahoo yahoo = new Yahoo();
        MapQuest mapquest = new MapQuest();
        RubyGeocoder ruby = new RubyGeocoder();
        OSM osm = new OSM();

        geoAdapters.put("yahoo", yahoo);
        geoAdapters.put("mapquest", mapquest);
        geoAdapters.put("geocoder", ruby);
        geoAdapters.put("rubygeocoder", ruby);
        geoAdapters.put("osm", osm);

        // Reverse Geocoding hasn't been implemented yet
        // revAdapters.put("yahoo", yahoo);
        // revAdapters.put("mapquest", mapquest);
    }

    public ArrayList<Result> geocode(ArrayList<Address> addresses, String adapter) throws GeoException {
        return geoAdapters.get(adapter.toLowerCase()).geocode(addresses, Address.TYPE.MIXED);
    }

    public ArrayList<Result> geocode(ArrayList<Address> addresses, String adapter, Address.TYPE hint) throws GeoException {
        return geoAdapters.get(adapter.toLowerCase()).geocode(addresses, hint);
    }

    public Result geocode(Address address, String adapter) throws GeoException {
        return geoAdapters.get(adapter.toLowerCase()).geocode(address);
    }
}
