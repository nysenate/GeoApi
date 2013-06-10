package gov.nysenate.sage.model.result;

import gov.nysenate.sage.model.address.Address;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * AddressResult represents the response returned by an AddressService implementation.
 */
public class AddressResult extends BaseResult
{
    private Address address;
    private boolean validated = false;

    public AddressResult()
    {
        this(null, "", null);
    }

    public AddressResult(Class sourceClass)
    {
        this(null, "", sourceClass);
    }

    public AddressResult(Class sourceClass, ResultStatus status)
    {
        this.source = sourceClass.getSimpleName();
        this.statusCode = status;
    }

    public AddressResult(Address address, String status, Class sourceClass)
    {
        this(address,status,sourceClass,false);
    }

    public AddressResult(Address address, String status, Class sourceClass, boolean validated)
    {
        this.setAddress(address);
        this.setSource(sourceClass);
        this.setValidated(validated);
    }

    public Address getAddress()
    {
        return address;
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

    public Map<String, Object> toMap()
    {
        LinkedHashMap<String,Object> root = new LinkedHashMap<>();
        LinkedHashMap<String,Object> data = new LinkedHashMap<>();
        data.put("address", this.getAddress());
        data.put("validated", this.isValidated());
        data.put("source", this.getSource());
        data.put("messages", this.getMessages());
        root.put("addressResult", data);
        return root;
    }
}
