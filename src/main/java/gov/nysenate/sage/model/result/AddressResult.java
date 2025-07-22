package gov.nysenate.sage.model.result;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.provider.address.AddressSource;

import java.util.ArrayList;
import java.util.List;

/**
 * AddressResult represents the response returned by an AddressService implementation.
 */
public class AddressResult extends BaseResult<AddressSource> {
    private Address address;
    private final List<String> messages = new ArrayList<>();

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

    public List<String> getMessages() {
        return messages;
    }

    public void addMessage(String message) {
        messages.add(message);
    }
}
