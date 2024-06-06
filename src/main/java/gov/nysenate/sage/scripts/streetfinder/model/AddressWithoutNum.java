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
        street = street.intern();
        postalCity = postalCity.intern();
    }

    public AddressWithoutNum(String street, String postalCity, String zip5, boolean standardizeAddress) {
        this(standardizeAddress ? standardizeStreet(street) : street, postalCity, Integer.parseInt(zip5));
    }

    public static AddressWithoutNum fromAddress(Address addr) {
        StreetAddress sa = StreetAddressParser.parseAddress(addr);
        return new AddressWithoutNum(sa.getStreet(), sa.getLocation(), sa.getZip5(), false);
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
    /**
     * Sometimes, USPS can validate incorrectly without this standardization.
     * E.g. (9151 71 ROAD, 11375) would become (9151 71ST AVE, 11375) without this.
     */
    private static String standardizeStreet(String street) {
        String[] streetParts = street.toUpperCase().split(" ");
        int tempIdx = 0;
        // Can't be too aggressive in correction: "20 STREET" -> "20th STREET", but "ROUTE 20" is correct.
        if (AddressDictionary.directionMap.containsKey(streetParts[0]) && streetParts.length  > 1) {
            tempIdx = 1;
        }
        streetParts[tempIdx] = AddressUtil.addSuffixToNumber(streetParts[tempIdx]);
        tempIdx = streetParts.length - 1;
        streetParts[tempIdx] = AddressDictionary.streetTypeMap.getOrDefault(streetParts[tempIdx], streetParts[tempIdx]);
        return String.join(" ", streetParts);
    }
}
