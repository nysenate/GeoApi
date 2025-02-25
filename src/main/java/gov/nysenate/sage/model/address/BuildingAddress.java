package gov.nysenate.sage.model.address;

import gov.nysenate.sage.scripts.streetfinder.model.AddressWithoutNum;
import gov.nysenate.sage.util.FormatUtil;
import gov.nysenate.sage.util.Pair;
import org.apache.commons.lang3.StringUtils;

public final class BuildingAddress extends Address {
    private static final String bldgNumPattern = "^[0-9]+-?[0-9]*[a-zA-Z]?";
    private String bldgId;
    private String street;
    private String internal = "";

    public BuildingAddress(int bldgNum, AddressWithoutNum awn) {
        super(awn);
        this.street = awn.street();
        this.bldgId = String.valueOf(bldgNum);
    }

    public BuildingAddress(String streetWithNum, String postalCity, String zip5) {
        this(streetWithNum, postalCity, "NY", zip5);
    }

    public BuildingAddress(String addr1, String postalCity, String state, String zip5) {
        this(addr1, postalCity, state, zip5, null);
    }

    public BuildingAddress(String addr1, String postalCity, String state, String zip5, String zip4) {
        super(postalCity, state, zip5, zip4);
        setStreetWithNum(addr1);
    }

    public BuildingAddress(String bldgId, String street, String postalCity, String state, String zip5, String zip4) {
        super(postalCity, state, zip5, zip4);
        this.street = street;
        this.bldgId = bldgId;
    }

    @Override
    public String getAddr1() {
        return bldgId + " " + street;
    }

    @Override
    public String getAddr2() {
        return internal;
    }

    public void setStreetWithNum(String streetWithNum) {
        Pair<String> parts = splitBldgId(streetWithNum);
        this.bldgId = parts.first();
        // The following line would remove all numerical suffixes and special characters.
        // This causes problems when matching the street file table. This may adversely affect the geocache table
        this.street = parts.second().replaceAll("[#:;.,']", "").replaceAll("[ -]+", " ").toUpperCase();
    }

    public String getBldgId() {
        return bldgId;
    }

    public String getStreet() {
        return street;
    }

    public String getStreetWithNum() {
        return bldgId + " " + street;
    }

    public String getInternal() {
        return internal;
    }

    public void setInternal(String addr2) {
        if (addr2 != null) {
            this.internal = FormatUtil.cleanString(addr2);
        }
    }

    @Override
    public boolean isValid() {
        return super.isValid() && !StringUtils.isBlank(bldgId) && !StringUtils.isBlank(street);
    }

    @Override
    public String toString() {
        return bldgId + " " + street + (internal.isEmpty() ? "" : " " + internal) + super.toString();
    }

    private static Pair<String> splitBldgId(String toSplit) {
        // TODO: may need to remove "#"
        String[] parts = toSplit.trim().split(" ", 2);
        if (parts.length != 2 || !parts[0].matches(bldgNumPattern)) {
            throw new IllegalArgumentException("Cannot parse bldg ID from: " + toSplit);
        }
        return new Pair<>(parts[0], parts[1]);
    }
}
