package gov.nysenate.sage.model.address;

import org.apache.commons.lang3.StringUtils;

public final class BuildingAddress extends Address {
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
        super(bldgId + " " + street, "", postalCity, state, zip5, zip4);
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
}
