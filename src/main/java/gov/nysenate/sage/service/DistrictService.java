package gov.nysenate.sage.service;

import gov.nysenate.sage.Address;
import gov.nysenate.sage.Result;
import gov.nysenate.sage.adapter.GeoServer;
import gov.nysenate.sage.adapter.StreetData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DistrictService {

    public enum TYPE {
        ASSEMBLY,CONGRESSIONAL,COUNTY,ELECTION,SENATE,SCHOOL,TOWN
    };

    @SuppressWarnings("serial")
    public static class DistException extends Exception {
        public DistException() {}
        public DistException(String msg) { super(msg); }
        public DistException(String msg, Throwable t) { super(msg, t); }
        public DistException(Throwable t) { super(t); }
    }

    /**
     *  Accepts single address or a list of addresses and returns distassigned result:
     *      result.status_code = 0 on success
     *      result.messages = list of response messages if failure
     *      result.address = dist-assigned match address for result if successful
     *
     *  If any incoming addresses are null a NULL result value is returned in its place.
     *  For bulk requests this ensures that index matching from the incoming address
     *  list to the returned results list is kept.
     *
     *  Errors loading the API and parsing the API response are wrapped in a DistException
     *  and thrown, terminating the geo-coding process and throwing away previous results.
     *
     *   TODO: Implement Either:
     *      A) Attach previous results to exception instead of throwing them away.
     *      B) Struggle on after exceptions via NULL results in the results list.
     */
    public interface DistAssignInterface {
        public Result assignDistrict(Address address, DistrictService.TYPE type) throws DistException;
        public ArrayList<Result> assignDistrict(ArrayList<Address> addresses, DistrictService.TYPE type) throws DistException;
        public ArrayList<Result> assignDistricts(ArrayList<Address> addresses, List<TYPE> types) throws DistException;
        public Result assignDistricts(Address address, List<TYPE> types) throws DistException;
    }

    private final HashMap<String, DistAssignInterface> adapters;

    public DistrictService() throws Exception {
        adapters = new HashMap<String, DistAssignInterface>();
        adapters.put("geoserver", new GeoServer());
    }

    public Result assignDistrict(Address address, TYPE type, String adapterName) throws DistException {
        return assignDistricts(address, new ArrayList<TYPE>(Arrays.asList(type)), adapterName);
    }

    public ArrayList<Result> assignDistrict(ArrayList<Address> addresses, TYPE type, String adapterName) throws DistException {
        return assignDistricts(addresses, new ArrayList<TYPE>(Arrays.asList(type)), adapterName);
    }

    public ArrayList<Result> assignAll(ArrayList<Address> addresses, String adapterName) throws DistException {
        return assignDistricts(addresses, new ArrayList<TYPE>(Arrays.asList(TYPE.values())), adapterName);
    }

    public Result assignAll(Address address, String adapterName) throws DistException{
        return assignDistricts(address, new ArrayList<TYPE>(Arrays.asList(TYPE.values())), adapterName);
    }

    public ArrayList<Result> assignDistricts(ArrayList<Address> addresses, List<TYPE> types, String adapterName) throws DistException {
        return getAdapter(adapterName).assignDistricts(addresses, types);
    }

    public Result assignDistricts(Address address, List<TYPE> types, String adapterName) throws DistException {
        return getAdapter(adapterName).assignDistricts(address, types);
    }

    private DistAssignInterface getAdapter(String adapter) throws DistException {
        adapter = adapter.toLowerCase();
        if (adapters.containsKey(adapter)) {
            return adapters.get(adapter);
        } else {
            throw new DistException("Adapter "+adapter+" not valid.");
        }
    }
}
