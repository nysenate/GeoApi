package gov.nysenate.sage.client.view.address;

import com.fasterxml.jackson.annotation.JsonRootName;
import gov.nysenate.sage.model.address.Address;

/**
 * AddressView represents the structure of an address representation on the response end of the API.
 */
@JsonRootName("address")
public class AddressView {
    protected String addr1;
    protected String addr2;
    protected String city;
    protected String state;
    protected Integer zip5;
    protected Integer zip4;
    protected Integer id;

    public AddressView(Address address) {
        this.addr1 = address.getAddr1();
        this.addr2 = address.getAddr2();
        this.city = address.getPostalCity();
        this.state = address.getState();
        this.zip5 = address.getZip5();
        this.zip4 = address.getZip4();
        this.id = address.getId();
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

    public Integer getZip5() {
        return zip5;
    }

    public Integer getZip4() {
        return zip4;
    }

    public Integer getId() {
        return id;
    }
}
