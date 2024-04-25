package gov.nysenate.sage.scripts.streetfinder.model;

import java.util.Objects;

/**
 * Represents a street address, as parsed from a streetfile.
 */
// TODO: rename to "addressRange"
public class StreetfileAddressRange {
    private final BuildingRange primaryBuilding;
    private final AddressWithoutNum addressWithoutNum;

    public StreetfileAddressRange(BuildingRange buildingRange, AddressWithoutNum addressWithoutNum) {
        this.primaryBuilding = buildingRange;
        this.addressWithoutNum = addressWithoutNum;
    }

    public String getStreet() {
        return addressWithoutNum.street();
    }

    public BuildingRange getBuildingRange() {
        return primaryBuilding;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StreetfileAddressRange that = (StreetfileAddressRange) o;
        return Objects.equals(primaryBuilding, that.primaryBuilding) &&
                Objects.equals(addressWithoutNum, that.addressWithoutNum);
    }

    @Override
    public int hashCode() {
        return Objects.hash(primaryBuilding, addressWithoutNum);
    }

    @Override
    public String toString() {
        return String.join(", ", Integer.toString(primaryBuilding.low()), Integer.toString(primaryBuilding.high()),
                primaryBuilding.parity().name(), addressWithoutNum.street(), addressWithoutNum.postalCity(),
                Integer.toString(addressWithoutNum.zip5()));
    }
}
