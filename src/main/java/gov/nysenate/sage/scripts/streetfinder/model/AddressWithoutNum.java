package gov.nysenate.sage.scripts.streetfinder.model;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.Intern;
import gov.nysenate.sage.util.StreetAddressParser;

public record AddressWithoutNum(String street, String postalCity, int zip5) {
    private static final Intern<AddressWithoutNum> interned = new Intern<>();

    public AddressWithoutNum(String street, String postalCity, String zip5) {
        this(street, postalCity, Integer.parseInt(zip5));
    }

    public static AddressWithoutNum fromAddress(Address addr) {
        StreetAddress sa = StreetAddressParser.parseAddress(addr);
        return new AddressWithoutNum(sa.getStreet(), sa.getPostalCity(), sa.getZip5());
    }

    public AddressWithoutNum intern() {
        return interned.get(this);
    }

    @Override
    public String toString() {
        final String strZip = Integer.toString(zip5);
        String[] parts = postalCity.isEmpty() ? new String[] {street, strZip} :
                new String[] {street, postalCity, strZip};
        return String.join(", ", parts);
    }

}
