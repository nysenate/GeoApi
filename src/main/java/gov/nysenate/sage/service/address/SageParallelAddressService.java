package gov.nysenate.sage.service.address;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.provider.address.AddressService;

import java.util.List;

/**
 * Parallel address validation for use when an AddressService implementation does not provide
 * native batch methods.
 */
public interface SageParallelAddressService {

    /**
     * Validates a list of addresses with the specified address service
     * @param addressService
     * @param addresses
     * @return
     */
    public List<AddressResult> validate(AddressService addressService, List<Address> addresses);

    /**
     * This method is used by spring to shut down Sage without errors
     */
    public void shutdownThread();
}
