package gov.nysenate.sage.client.view.address;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonRootName;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.util.AddressUtil;

import java.util.Objects;

/**
 * AddressView represents the structure of an address representation on the response end of the API.
 */
@JsonRootName("address")
public record AddressView(String addr1, String addr2, String city, String state, String zip5, String zip4) {
    public AddressView(Address address, boolean usePunct) {
        this(usePunct ? AddressUtil.addr1WithPunct(address) : address.getAddr1(),
                address.getAddr2(), address.getPostalCity(), address.getState(),
                Objects.toString(address.getZip5(), null), Objects.toString(address.getZip4(), null));
    }

    @JsonIgnore
    public Address getRealAddress() {
        return new Address(addr1, addr2, city, state, zip5, zip4);
    }
}
