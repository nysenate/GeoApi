package gov.nysenate.sage.service;

import gov.nysenate.sage.Address;
import gov.nysenate.sage.Result;

import java.util.ArrayList;
import java.util.HashMap;

public class AddressService {
    private final HashMap<String, AddressInterface> adapters = new HashMap<String, AddressInterface>();

    public interface AddressInterface {
        public Result validate(Address address);
        public ArrayList<Result> validate(ArrayList<Address> addresses);
        public ArrayList<Result> lookupCityState(ArrayList<Address> addresses);
        public Result lookupCityState(Address address);
    }

    public AddressService() throws Exception {
        //adapters.put("usps", new USPS());
    }

    public Result validate(Address address, String adapter) {
        return adapters.get(adapter).validate(address);
    }
    public ArrayList<Result> validate(ArrayList<Address> addresses, String adapter) {
        return adapters.get(adapter).validate(addresses);
    }

    public Result lookupCityState(Address address, String adapter) {
        return adapters.get(adapter).lookupCityState(address);
    }

    public ArrayList<Result> lookupCityState(ArrayList<Address> addresses, String adapter) {
        return adapters.get(adapter).lookupCityState(addresses);
    }
}
