package gov.nysenate.sage.provider.address;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.Zip5;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.model.result.CityStateResult;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * AddressService is used to perform address lookup and validation.
 */
public interface AddressService {
    AddressSource source();
    @Nonnull
    AddressResult validate(Address address);
    List<AddressResult> validate(List<Address> addresses);
    @Nonnull
    CityStateResult lookupCityState(Zip5 zip5);
    List<CityStateResult> lookupCityState(List<Zip5> zips);
}
