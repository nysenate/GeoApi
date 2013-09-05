package gov.nysenate.sage.service.address;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.service.base.ServiceProviders;
import gov.nysenate.sage.util.AddressUtil;

public class AddressServiceProvider extends ServiceProviders<AddressService>
{
    /**
     * Validates an address using USPS or MapQuest depending on the level of input supplied.
     * @param address  Address to validate
     * @param provider Provide to use (if null, defaults will be used)
     * @param usePunct If true, validated address will have periods after abbreviations.
     * @return AddressResult
     */
    public AddressResult validate(Address address, String provider, Boolean usePunct)
    {
        AddressResult addressResult;
        /** Perform null check */
        if (address == null) {
            return new AddressResult(this.getClass(), ResultStatus.MISSING_ADDRESS);
        }
        /** Use provider if specified */
        if (provider != null && !provider.isEmpty()) {
            if (this.isRegistered(provider)) {
                addressResult = this.newInstance(provider).validate(address);
            }
            else {
                return new AddressResult(this.getClass(), ResultStatus.ADDRESS_PROVIDER_NOT_SUPPORTED);
            }
        }
        /** Use USPS if address is eligible */
        else if (address.isEligibleForUSPS()) {
            addressResult = this.newInstance("usps").validate(address);
        }
        /** Otherwise fallback to mapquest */
        else {
            addressResult = this.newInstance("mapquest").validate(address);
        }
        /** Apply punctuation to the address if requested */
        if (usePunct && addressResult != null && addressResult.isValidated()) {
            addressResult.setAddress(AddressUtil.addPunctuation(addressResult.getAddress()));
        }
        return addressResult;
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
        return validate(address, provider, false);
    }
}
