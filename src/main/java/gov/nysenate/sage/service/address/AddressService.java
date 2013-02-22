package gov.nysenate.sage.service.address;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.service.Service;

import java.util.ArrayList;

/**
 * Comment this later..
 */
public interface AddressService
{
    public AddressResult validate(Address address);
    public ArrayList<AddressResult> validate(ArrayList<Address> addresses);

    public AddressResult lookupCityState(Address address);
    public ArrayList<AddressResult> lookupCityState(ArrayList<Address> addresses);

    public AddressResult lookupZipCode(Address address);
    public ArrayList<AddressResult> lookupZipCode(ArrayList<Address> addresses);
}
