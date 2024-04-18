package gov.nysenate.sage.scripts.streetfinder.model;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.Intern;
import gov.nysenate.sage.util.StreetAddressParser;

import java.util.Objects;

/**
 * Represents a street address, as parsed from a streetfile.
 */
// TODO: rename to "addressRange"
public class StreetfileAddressRange {
    private static final Intern<AddressWithoutNum> addressWithoutNumCache = new Intern<>();
    private final BuildingRange primaryBuilding;
    private final AddressWithoutNum addressWithoutNum;

    public StreetfileAddressRange(BuildingRange buildingRange, AddressWithoutNum addressWithoutNum) {
        this.primaryBuilding = buildingRange;
        this.addressWithoutNum = addressWithoutNum;
    }

    public StreetfileAddressRange(Address low, Address high) {
        StreetAddress lowStreetAddr = StreetAddressParser.parseAddress(low);
        StreetAddress highStreetAddr = StreetAddressParser.parseAddress(high);
        var addressWithoutNum = new AddressWithoutNum(lowStreetAddr.getStreet(), lowStreetAddr.getLocation(), lowStreetAddr.getZip5());
        this.addressWithoutNum = addressWithoutNumCache.get(addressWithoutNum);
        String lowBldg = lowStreetAddr.getBldgNum() + lowStreetAddr.getBldgChar();
        String highBldg = highStreetAddr.getBldgNum() + highStreetAddr.getBldgChar();
        this.primaryBuilding = new BuildingRange(lowBldg, highBldg);
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
}
