package gov.nysenate.sage.scripts.streetfinder.model;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.BuildingAddress;
import gov.nysenate.sage.model.address.Zip5;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.Intern;

import javax.annotation.Nonnull;

public record AddressWithoutNum(String street, String postalCity, Zip5 zip5) {
    private static final Intern<AddressWithoutNum> interned = new Intern<>();

    public AddressWithoutNum(String street, String postalCity, String zip5) {
        this(street, postalCity, new Zip5(zip5));
    }

    public static AddressWithoutNum fromAddress(Address addr) {
        if (!(addr instanceof BuildingAddress bldgAddr)) {
            throw new IllegalArgumentException("Address is not a BuildingAddress");
        }
        return new AddressWithoutNum(bldgAddr.getStreet(), addr.getPostalCity(), addr.getZip5());
    }

    public Address toAddress(int num) {
        return new Address(num + " " + street, "", postalCity, "NY", zip5.toString(), null);
    }

    public AddressWithoutNum intern() {
        return interned.get(this);
    }

    @Nonnull
    @Override
    public String toString() {
        String[] parts = postalCity.isEmpty() ? new String[] {street, zip5.toString()} :
                new String[] {street, postalCity, zip5.toString()};
        return String.join(", ", parts);
    }
}
