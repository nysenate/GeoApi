package gov.nysenate.sage.model.result;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.provider.address.AddressSource;

/**
 * AddressResult represents the response returned by an AddressService implementation.
 */
public class AddressResult extends BaseResult<AddressSource> {
    private Address address;

    public AddressResult(AddressSource source) {
        super(source);
    }

    public AddressResult(AddressSource source, ResultStatus status) {
        this(source);
        this.setStatusCode(status);
    }

    public Address getAddress() {
        return address;
    }

    public boolean isValidated() {
        return statusCode == ResultStatus.SUCCESS;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}
