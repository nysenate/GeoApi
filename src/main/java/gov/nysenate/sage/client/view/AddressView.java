package gov.nysenate.sage.client.view;

import com.fasterxml.jackson.annotation.JsonRootName;
import gov.nysenate.sage.model.address.Address;

@JsonRootName("address")
public class AddressView
{
    protected String addr1 = "";
    protected String addr2 = "";
    protected String city = "";
    protected String state = "";
    protected String zip5 = "";
    protected String zip4 = "";

    public AddressView(Address address)
    {
        this.addr1 = address.getAddr1();
        this.addr2 = address.getAddr2();
        this.city = address.getCity();
        this.state = address.getState();
        this.zip5 = address.getZip5();
        this.zip4 = address.getZip4();
    }

    public AddressView(String addr1, String addr2, String city, String state, String zip5, String zip4)
    {
        this.addr1 = addr1;
        this.addr2 = addr2;
        this.city = city;
        this.state = state;
        this.zip5 = zip5;
        this.zip4 = zip4;
    }

    public String getAddr1() {
        return addr1;
    }

    public String getAddr2() {
        return addr2;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getZip5() {
        return zip5;
    }

    public String getZip4() {
        return zip4;
    }
}
