package gov.nysenate.sage.scripts.streetfinder.model;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.Intern;
import gov.nysenate.sage.util.StreetAddressParser;

public record AddressWithoutNum(String street, String postalCity, int zip5) {
    private static final Intern<AddressWithoutNum> interned = new Intern<>();

    public AddressWithoutNum {
        street = street.intern();
        postalCity = postalCity.intern();
    }

    public AddressWithoutNum(String street, String postalCity, String zip5) {
        this(street, postalCity, Integer.parseInt(zip5));
    }

    public AddressWithoutNum(StreetAddress streetAddr) {
        this(streetAddr.getStreet(), streetAddr.getLocation(), streetAddr.getZip5());
    }

    public AddressWithoutNum(Address addr) {
        this(StreetAddressParser.parseAddress(addr));
    }

    public AddressWithoutNum intern() {
        return interned.get(this);
    }

    @Override
    public String toString() {
        return String.join(", ", street, postalCity, Integer.toString(zip5));
    }
}
