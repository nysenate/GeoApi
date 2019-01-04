package gov.nysenate.sage.dao.provider.usps;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.result.AddressResult;

import java.util.List;

public interface USPSAMSDao {

    /**
     * Performs a single address validation request.
     * @param address Input Address
     * @return AddressResult
     */
    public AddressResult getValidatedAddressResult(Address address);

    /**
     * Performs batch address validation request.
     * @param addresses List of Addresses to validate.
     * @return List<AddressResult>
     */
    public List<AddressResult> getValidatedAddressResults(List<Address> addresses);

    /**
     * Get a single city state response from USPS AMS
     * @param address
     * @return
     */
    public AddressResult getCityStateResult(Address address);

    /**
     * Get a batch of city state responses from USPS AMS
     * @param addresses
     * @return
     */
    public List<AddressResult> getCityStateResults (List<Address> addresses);


}
