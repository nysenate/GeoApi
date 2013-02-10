package gov.nysenate.sage.service.address;

import gov.nysenate.sage.Address;
import gov.nysenate.sage.Result;
import java.util.ArrayList;

public interface AddressServiceInterface {

    public Result validate(Address address);
    public ArrayList<Result> validate(ArrayList<Address> addresses);
    public ArrayList<Result> lookupCityState(ArrayList<Address> addresses);
    public Result lookupCityState(Address address);

}
