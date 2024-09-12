package gov.nysenate.sage.model.result;

import gov.nysenate.sage.model.address.Address;

/**
 * AddressResult represents the response returned by an AddressService implementation.
 */
public class AddressResult extends BaseResult {
    private Address address;
    private boolean validated = false;

    public AddressResult()
    {
        this(null);
    }

    public AddressResult(Class sourceClass)
    {
        this.setSource(sourceClass);
    }

    public AddressResult(Class sourceClass, ResultStatus status)
    {
        this(null, status, sourceClass);
    }

    public AddressResult(Address address, ResultStatus status, Class sourceClass)
    {
        this(address, status, sourceClass, false);
    }

    public AddressResult(Address address, ResultStatus status, Class sourceClass, boolean validated)
    {
        this.setAddress(address);
        this.setStatusCode(status);
        this.setSource(sourceClass);
        this.setValidated(validated);
        if (this.address != null) {
            this.address.setUspsValidated(validated);
        }
    }

    public Address getAddress()
    {
        return address;
    }

    public String getAdressLogString() {
        return address.toLogString();
    }

    public void setAddress(Address address)
    {
        this.address = address;
    }

    public boolean isValidated()
    {
        return this.validated;
    }

    public void setValidated(boolean isValidated)
    {
        this.validated = isValidated;
    }
}
