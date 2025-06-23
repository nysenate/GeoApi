package gov.nysenate.sage.model.address;

import gov.nysenate.sage.util.Pair;
import org.apache.commons.lang3.StringUtils;

public final class BuildingAddress extends Address {
    private static final String bldgNumPattern = "^[0-9]+-?[0-9]*[a-zA-Z]?";
    private final String bldgId;
    private final String street;

    // The AMS constructor.
    public BuildingAddress(Address baseAddress, String bldgId, String street) {
        super(baseAddress);
        this.bldgId = bldgId;
        this.street = street;
    }

    // Assembles a BuildingAddress from database storage.
    public BuildingAddress(String bldgId, String street, String postalCity, String state, String zip5, String zip4) {
        super(postalCity, state, zip5, StringUtils.isBlank(zip4) ? null : new Zip4(zip4));
        this.bldgId = bldgId;
        this.street = street;
    }

    public String getBldgId() {
        return bldgId;
    }

    public String getStreet() {
        return street;
    }

    @Override
    public boolean isValid() {
        return super.isValid() && !StringUtils.isBlank(bldgId) && !StringUtils.isBlank(street);
    }

    @Override
    public boolean isUspsValidated() {
        return true;
    }

    @Override
    public String toString() {
        return bldgId + " " + street + "," + (StringUtils.isBlank(getAddr2()) ? "" : " " + getAddr2()) +
                " " + super.toString();
    }

    private static Pair<String> splitBldgId(String toSplit) {
        String[] parts = toSplit.replaceAll("#", "").trim().split(" ", 2);
        if (parts.length != 2 || !parts[0].matches(bldgNumPattern)) {
            throw new IllegalArgumentException("Cannot parse bldg ID from: " + toSplit);
        }
        return new Pair<>(parts[0], parts[1]);
    }
}
