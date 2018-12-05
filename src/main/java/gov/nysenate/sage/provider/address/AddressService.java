package gov.nysenate.sage.provider.address;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.result.AddressResult;

import java.util.List;

/**
 * AddressService is used to perform address lookup and validation.
 */
public interface AddressService
{
    public AddressResult validate(Address address);
    public List<AddressResult> validate(List<Address> addresses);
    public AddressResult lookupCityState(Address address);
    public List<AddressResult> lookupCityState(List<Address> addresses);
    public AddressResult lookupZipCode(Address address);
    public List<AddressResult> lookupZipCode(List<Address> addresses);
}
