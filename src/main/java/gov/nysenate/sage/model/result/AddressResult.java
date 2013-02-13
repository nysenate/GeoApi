package gov.nysenate.sage.model.result;

import gov.nysenate.sage.model.addr.Address;

import java.util.ArrayList;

/**
 * AddressResult represents the response returned by an AddressService implementation.
 */
public class AddressResult
{
    private Address address;
    private boolean validated;
    private ArrayList<String> messages = new ArrayList<>();
    private String status;
    private String source;

    public AddressResult()
    {
        this(null, "", "");
    }

    public AddressResult(Class sourceClass)
    {
        this(null, "", sourceClass.getSimpleName());
    }

    public AddressResult(Address address, String status, String source)
    {
        this.address = address;
        this.status = status;
        this.source = source;
        this.validated = false;
    }

    public void addMessage(String message)
    {
        this.messages.add(message);
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

    public ArrayList<String> getMessages()
    {
        return messages;
    }

    public void setMessages(ArrayList<String> messages)
    {
        this.messages = messages;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getSource()
    {
        return source;
    }

    public void setSource(Class sourceClass)
    {
        this.source = sourceClass.getSimpleName();
    }
}