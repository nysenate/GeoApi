package gov.nysenate.sage.service.address;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.service.base.ServiceProviders;

public class AddressServiceProvider extends ServiceProviders<AddressService>
{
    /**
     * USPS requires city so if we get an un-parsed address use MapQuest as the default option.
     */
    public AddressResult validate(Address address, String provider)
    {
        if (provider != null && !provider.isEmpty()) {
            return this.newInstance(provider).validate(address);
        }
        if (address != null && address.isEligibleForUSPS()) {
            return this.newInstance("usps").validate(address);
        }
        return this.newInstance("mapquest").validate(address);
    }

    /**
     * Use USPS for a city state lookup by default.
     */
    public AddressResult lookupCityState(Address address, String provider)
    {
        return this.newInstance(provider, "usps").lookupCityState(address);
    }

    /**
     * Zipcode lookup is the same as a validate request with less output.
     */
    public AddressResult lookupZipcode(Address address, String provider)
    {
        return validate(address, provider);
    }
}
