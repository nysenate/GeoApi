package gov.nysenate.sage.scripts.streetfinder.scripts.utils;

import gov.nysenate.sage.scripts.streetfinder.model.StreetfileAddressRange;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Objects;

// TODO: get rid of
public class Addr1WithZip implements Serializable {
    private final String addr1, building, street;
    private final int zip5;

    public Addr1WithZip(int buildingNum, Addr1WithZip other) {
        this.building = String.valueOf(buildingNum);
        this.street = other.street;
        this.zip5 = other.zip5;
        this.addr1 = building + " " + street;
    }

    public Addr1WithZip(StreetfileAddressRange sfa, boolean isLow) {
        this("", 0);
//        this(sfa.getBuildingRange().getBuilding(isLow) + " " + sfa.get(STREET),
//                Integer.parseInt(sfa.get(ZIP)));
    }

    public Addr1WithZip(@Nonnull String addr1, int zip5) {
        this.addr1 = addr1;
        this.zip5 = zip5;
        String[] split = addr1.split(" ", 2);
        this.building = split[0];
        this.street = split[1];
    }

    public String addr1() {
        return addr1;
    }

    public int zip() {
        return zip5;
    }

    public String building() {
        return building;
    }

    public String street() {
        return street;
    }

    public boolean ignoreBuildingEquals(Addr1WithZip other) {
        if (other == null) {
            return false;
        }
        return zip5 == other.zip5 && street.equals(other.street);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Addr1WithZip that = (Addr1WithZip) o;
        return zip5 == that.zip5 && addr1.equals(that.addr1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(addr1, zip5);
    }

    @Override
    public String toString() {
        return "addr1=" + addr1 + ", "+ "zip5=" + zip5;
    }
}
