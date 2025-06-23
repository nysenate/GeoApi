package gov.nysenate.sage.model.address;

import gov.nysenate.sage.util.Pair;
import org.apache.commons.lang3.StringUtils;

public final class BuildingAddress extends Address {
    private static final String bldgNumPattern = "^[0-9]+-?[0-9]*[a-zA-Z]?";
    private final String bldgId;
    private final String street;

    public BuildingAddress(Address baseAddress, String bldgId, String street) {
        super(baseAddress);
        this.bldgId = bldgId;
        this.street = street;
    }

    public BuildingAddress(String streetWithNum, String postalCity, String state, String zip5) {
        this(streetWithNum, postalCity, state, zip5, null);
    }

    public BuildingAddress(String streetWithNum, String postalCity, String state, String zip5, String zip4) {
        super(postalCity, state, zip5, zip4);
        Pair<String> parts = splitBldgId(streetWithNum);
        this.bldgId = parts.first();
        // The following line would remove all numerical suffixes and special characters.
        // This causes problems when matching the street file table. This may adversely affect the geocache table.
        this.street = parts.second().replaceAll("[#:;.,']", "").replaceAll("[ -]+", " ").toUpperCase();
    }

    public BuildingAddress(String bldgId, String street, String postalCity, String state, String zip5, String zip4) {
        super(postalCity, state, zip5, zip4);
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
