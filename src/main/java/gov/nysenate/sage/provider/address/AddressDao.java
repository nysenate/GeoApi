package gov.nysenate.sage.provider.address;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.Zip5;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.model.result.CityStateResult;

import java.util.ArrayList;
import java.util.List;

public interface AddressDao {
    AddressSource source();

    default AddressResult validate(Address address) {
        List<Address> addressList = new ArrayList<>(List.of(address));
        List<AddressResult> resultList = validate(addressList);
        if (resultList == null) {
            return null;
        }
        return resultList.get(0);
    }

    List<AddressResult> validate(List<Address> addresses);

    default CityStateResult lookupCityState(Zip5 zip5) {
        List<CityStateResult> resultList = lookupCityStates(List.of(zip5));
        if (resultList == null) {
            return null;
        }
        return resultList.get(0);
    }

    List<CityStateResult> lookupCityStates(List<Zip5> zips);
}
