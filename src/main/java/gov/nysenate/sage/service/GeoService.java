package gov.nysenate.sage.service;

import gov.nysenate.sage.Address;
import gov.nysenate.sage.Result;
import gov.nysenate.sage.adapter.MapQuest;
import gov.nysenate.sage.adapter.RubyGeocoder;
import gov.nysenate.sage.adapter.Yahoo;

import java.io.UnsupportedEncodingException;
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

    // All adapters able to geocode must implement this interface.
    public interface GeocodeInterface {
        public Result geocode(Address address);
        public ArrayList<Result> geocode(ArrayList<Address> addresses, Address.TYPE hint);
    }

    // All adapters able to reverse geocode must implement this interface.
    public interface ReverseGeocodeInterface {
        public Result reverseGeocode(Address address);
        public ArrayList<Result> reverseGeocode(ArrayList<Address> addresses, Address.TYPE hint);
    }

    // Adapters implementing each interface are stored in hashes so that a class
    // using the GeoService can invoke specific adapters by name.
    private final HashMap<String, GeocodeInterface> geoAdapters = new HashMap<String, GeocodeInterface>();
    // private final HashMap<String, ReverseGeocodeInterface> revAdapters = new HashMap<String, ReverseGeocodeInterface>();


    public GeoService() throws Exception {
        Yahoo yahoo = new Yahoo();
        MapQuest mapquest = new MapQuest();
        RubyGeocoder ruby = new RubyGeocoder();

        geoAdapters.put("yahoo", yahoo);
        geoAdapters.put("mapquest", mapquest);
        geoAdapters.put("rubygeocoder", ruby);

        // Reverse Geocoding hasn't been implemented yet
        // revAdapters.put("yahoo", yahoo);
        // revAdapters.put("mapquest", mapquest);
    }

    public ArrayList<Result> geocode(ArrayList<Address> addresses, String adapter) throws UnsupportedEncodingException {
        return geoAdapters.get(adapter.toLowerCase()).geocode(addresses, Address.TYPE.MIXED);
    }

    public ArrayList<Result> geocode(ArrayList<Address> addresses, String adapter, Address.TYPE hint) throws UnsupportedEncodingException {
        return geoAdapters.get(adapter.toLowerCase()).geocode(addresses, hint);
    }

    public Result geocode(Address address, String adapter) throws UnsupportedEncodingException {
        return geoAdapters.get(adapter.toLowerCase()).geocode(address);
    }
}
