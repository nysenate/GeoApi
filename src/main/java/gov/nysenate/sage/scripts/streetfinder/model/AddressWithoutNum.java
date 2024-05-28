package gov.nysenate.sage.scripts.streetfinder.model;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.Intern;
import gov.nysenate.sage.util.AddressDictionary;
import gov.nysenate.sage.util.AddressUtil;
import gov.nysenate.sage.util.StreetAddressParser;

public record AddressWithoutNum(String street, String postalCity, int zip5) {
    private static final Intern<AddressWithoutNum> interned = new Intern<>();

    public AddressWithoutNum {
        street = correctStreet(street).intern();
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
        final String strZip = Integer.toString(zip5);
        String[] parts = postalCity.isEmpty() ? new String[] {street, strZip} :
                new String[] {street, postalCity, strZip};
        return String.join(", ", parts);
    }
    private static String correctStreet(String street) {
        String[] streetParts = street.split(" ");
        // Sometimes, USPS can validate incorrectly without this standardization.
        // E.g. (9151 71 ROAD, 11375) would become (9151 71ST AVE, 11375) without this
        for (int i = 0; i < streetParts.length; i++) {
            streetParts[i] = AddressUtil.addSuffixToNumber(streetParts[i]);
            streetParts[i] = AddressDictionary.streetTypeMap.getOrDefault(streetParts[i], streetParts[i]);
        }
        return String.join(" ", streetParts).toUpperCase();
    }
}
