package gov.nysenate.sage.service.address;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.service.base.ServiceProviders;
import gov.nysenate.sage.util.AddressUtil;

import java.util.ArrayList;
import java.util.List;

public class AddressServiceProvider extends ServiceProviders<AddressService>
{
    /**
     * Validates an address using USPS or another provider if available.
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
            addressResult = this.newInstance().validate(address);
        }
        /** Otherwise address is insufficient */
        else {
            addressResult = new AddressResult(this.getClass(), ResultStatus.INSUFFICIENT_ADDRESS);
        }

        /** Apply punctuation to the address if requested */
        if (usePunct && addressResult != null && addressResult.isValidated()) {
            addressResult.setAddress(AddressUtil.addPunctuation(addressResult.getAddress()));
        }
        return addressResult;
    }

    /**
     * Validates addresses using USPS or another provider if available.
     * @param addresses List of Addresses to validate
     * @param provider Provider to use, default is usps if left null or empty
     * @param usePunct Apply address punctuation to each result.
     * @return List<AddressResult>
     */
    public List<AddressResult> validate(List<Address> addresses, String provider, Boolean usePunct)
    {
        List<AddressResult> addressResults = new ArrayList<>();

        if (addresses != null && !addresses.isEmpty()) {
            if (provider == null || provider.isEmpty()) {
                provider = this.defaultProvider;
            }
            if (this.isRegistered(provider)) {
                addressResults = this.newInstance(provider).validate(addresses);
                if (usePunct) {
                    for (AddressResult addressResult : addressResults) {
                        if (addressResult != null && addressResult.isValidated()) {
                            addressResult.setAddress(AddressUtil.addPunctuation(addressResult.getAddress()));
                        }
                    }
                }
            }
            else {
                for (int i = 0; i < addresses.size(); i++) {
                    addressResults.add(new AddressResult(this.getClass(), ResultStatus.ADDRESS_PROVIDER_NOT_SUPPORTED));
                }
            }
        }

        return addressResults;
    }

    /**
     * Use USPS for a city state lookup by default.
     */
    public AddressResult lookupCityState(Address address, String provider)
    {
        return this.newInstance(provider, true).lookupCityState(address);
    }

    /**
     * Zipcode lookup is the same as a validate request with less output.
     */
    public AddressResult lookupZipcode(Address address, String provider)
    {
        return validate(address, provider, false);
    }
}
