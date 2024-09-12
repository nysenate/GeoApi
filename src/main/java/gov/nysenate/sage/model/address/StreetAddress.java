package gov.nysenate.sage.model.address;

import gov.nysenate.sage.scripts.streetfinder.model.AddressWithoutNum;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents a street address with address components broken down.
 */
public class StreetAddress {
    private String bldgId;
    // TODO: remove (TOWN|CITY)
    private AddressWithoutNum awn;
    private Zip4 zip4;
    private String internal;
    // TODO: have another class?
    private boolean isPoBox;
    // TODO: streetname can be "null", I guess

    public StreetAddress() {}

    public StreetAddress(AddressWithoutNum awn) {
        this.awn = awn;
    }

    public static String combine(String delim, String... parts) {
        return Arrays.stream(parts).filter(str -> str != null && !str.isBlank() && !str.equals("null"))
                .collect(Collectors.joining(delim)).trim();
    }

    /**
     * Converts the street address into a basic Address object performing the
     * necessary null checks.
     * @return  Address
     */
    public Address toAddress() {
        var addr1 = "";
        if (isPoBox) {
            addr1 = "PO Box " + bldgId;
        }
        else {
            addr1 = combine(bldgId, String.valueOf(awn), String.valueOf(zip4));
        }

        return new Address(addr1.trim(), internal, awn.postalCity().trim(), "",
                String.valueOf(awn.zip5()), zip4.toString());
    }

    // TODO: use str.equalsIgnoreCase
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StreetAddress that)) return false;
        return Objects.equals(bldgId, that.bldgId) &&
                Objects.equals(awn, that.awn) &&
                Objects.equals(zip4, that.zip4) &&
                Objects.equals(internal, that.internal);
    }

    public boolean hasStreet() {
        return !getStreet().isEmpty();
    }

    @Override
    public String toString() {
        return toAddress().toString();
    }

    public String getBldgId() {
        return bldgId;
    }

    public void setBldgId(String bldgId) {
        this.bldgId = bldgId;
    }

    public String getStreet() {
        return awn.street();
    }

    public String getInternal() {
        return internal;
    }

    public void setInternal(String internal) {
        this.internal = internal;
    }

    public String getPostalCity() {
        return awn.postalCity();
    }

    public Integer getZip5() {
        return awn.zip5();
    }

    public Integer getZip4() {
        return zip4.zip();
    }

    public void setZip4(Integer zip4) {
        this.zip4 = new Zip4(zip4);
    }

    public boolean isPoBoxAddress() {
        return isPoBox;
    }
}
