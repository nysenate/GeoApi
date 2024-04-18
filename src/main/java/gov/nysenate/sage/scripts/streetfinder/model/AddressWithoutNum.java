package gov.nysenate.sage.scripts.streetfinder.model;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.util.StreetAddressParser;

public record AddressWithoutNum(String street, String postalCity, int zip5) {
    public AddressWithoutNum(String street, String postalCity, String zip5) {
        this(street, postalCity, Integer.parseInt(zip5));
    }

    public AddressWithoutNum(StreetAddress streetAddr) {
        this(streetAddr.getStreet(), streetAddr.getLocation(), streetAddr.getZip5());
    }

    public AddressWithoutNum(Address addr) {
        this(StreetAddressParser.parseAddress(addr));
    }
}
