package gov.nysenate.sage.service.address;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.result.AddressResult;

import javax.annotation.Nonnull;
import java.util.List;

public interface AddressProvider {

    /**
     * Validates an address using USPS or another provider if available.
     * @param address  Address to validate
     * @param provider Provide to use (if null, defaults will be used)
     * @param usePunct If true, validated address will have periods after abbreviations.
     * @return AddressResult
     */
    public AddressResult validate(Address address, String provider, Boolean usePunct);

    /**
     * Validates addresses using USPS or another provider if available.
     * @param addresses List of Addresses to validate
     * @param provider Provider to use, default provider if left null or empty
     * @param usePunct Apply address punctuation to each result.
     * @return List<AddressResult>
     */
    public List<AddressResult> validate(List<Address> addresses, String provider, Boolean usePunct);

    /**
     * Use USPS for a city state lookup by default.
     */
    @Nonnull
    public AddressResult lookupCityState(Address address, String providerName);

    /**
     * Zipcode lookup is the same as a validate request with less output.
     */
    public AddressResult lookupZipcode(Address address, String provider);
}
