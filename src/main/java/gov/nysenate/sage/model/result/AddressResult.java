package gov.nysenate.sage.model.result;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.provider.address.AddressSource;

import java.util.List;

/**
 * AddressResult represents the response returned by an AddressService implementation.
 */
public class AddressResult extends BaseResult<AddressSource> {
    private final Address address;
    private final List<String> messages;

    public AddressResult(AddressSource source, ResultStatus status) {
        this(source, status, null, null);
    }

    public AddressResult(AddressSource source, ResultStatus status, Address address, List<String> messages) {
        super(source, status);
        this.address = address;
        this.messages = messages;
    }

    public Address getAddress() {
        return address;
    }

    public List<String> getMessages() {
        return messages;
    }
}
