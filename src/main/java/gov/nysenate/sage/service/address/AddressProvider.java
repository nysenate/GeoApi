package gov.nysenate.sage.service.address;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.result.AddressResult;

import javax.annotation.Nonnull;
import java.util.List;

public interface AddressProvider {
    /**
     * Validates addresses using USPS or another provider if available.
     * @param addresses List of Addresses to validate
     * @param provider Provider to use, default provider if left null or empty
     * @param usePunct Apply address punctuation to each result.
     * @return List<AddressResult>
     */
    List<AddressResult> validate(List<Address> addresses, String provider, boolean usePunct);

    /**
     * Use USPS for a city state lookup by default.
     */
    @Nonnull
    AddressResult lookupCityState(Address address, String providerName);

    List<AddressResult> lookupCityState(List<Address> addresses, String providerName);

    /**
     * Zipcode lookup is the same as a validate request with less output.
     */
    AddressResult lookupZipcode(Address address, String provider);
}
