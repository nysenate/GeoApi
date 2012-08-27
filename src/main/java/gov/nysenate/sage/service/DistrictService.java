package gov.nysenate.sage.service;

import gov.nysenate.sage.Address;
import gov.nysenate.sage.Result;
import gov.nysenate.sage.adapter.GeoServer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class DistrictService {
    public enum TYPE {ASSEMBLY,CONGRESSIONAL,COUNTY,ELECTION,SENATE,SCHOOL,TOWN};
    private final HashMap<String, DistAssignInterface> adapters;

    public interface DistAssignInterface {
        public Result distAssign(Address address, DistrictService.TYPE type);
        public ArrayList<Result> distAssign(ArrayList<Address> addresses, DistrictService.TYPE type);
    }

    public DistrictService() throws Exception {
        adapters = new HashMap<String, DistAssignInterface>();
        adapters.put("geoserver", new GeoServer());
    }

    public Result assign(Address address, TYPE type, String adapterName) {
        return assignDistricts(address, new ArrayList<TYPE>(Arrays.asList(type)), adapterName);
    }

    public Result assignAll(Address address, String adapterName) {
        return assignDistricts(address, new ArrayList<TYPE>(Arrays.asList(TYPE.values())), adapterName);
    }

    public Result assignDistricts(Address address, ArrayList<TYPE> types, String adapterName) {
        Result result = new Result();
        DistAssignInterface adapter = adapters.get(adapterName);
        for (TYPE type : types) {
            Result typeResult = adapter.distAssign(address, type);
            if (typeResult == null)
                return null;

            // Don't quit on one bad lookup, try them all but alter that status code
            if (!typeResult.status_code.equals("0")) {
                result.status_code = typeResult.status_code;
                result.messages.addAll(typeResult.messages);
            } else {
                // Accumulate districts on the result address
                address = typeResult.address;
            }
        }
        result.address = address;
        return result;
    }
}
