package gov.nysenate.sage.scripts.streetfinder.model;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.util.StreetAddressParser;

import java.util.regex.Pattern;

/**
 * Represents a street address, as parsed from a streetfile.
 */
// TODO: rename to "addressRange"
public class StreetfileAddressRange {
    private static final Pattern digitPattern = Pattern.compile("-?\\d+(\\.\\d+)?");
    private final BuildingRange primaryBuilding = new BuildingRange();
    private String street, city, town, village;
    private int zip5;

    public StreetfileAddressRange() {}

    public StreetfileAddressRange(Address low, Address high) {
        // TODO: check for street, city, town, village, zip matches
        StreetAddress lowStreetAddr = StreetAddressParser.parseAddress(low);
        StreetAddress highStreetAddr = StreetAddressParser.parseAddress(high);
        this.street = lowStreetAddr.getStreet();
        this.city = lowStreetAddr.getLocation();
        setZip5(lowStreetAddr.getZip5());
        primaryBuilding.setData(true, lowStreetAddr.getBldgNum() + lowStreetAddr.getBldgChar());
        primaryBuilding.setData(false, highStreetAddr.getBldgNum() + highStreetAddr.getBldgChar());
    }

    public void setStreet(String street) {
        this.street = street.intern();
    }

    public void setZip5(String zip5) {
        this.zip5 = Integer.parseInt(zip5.intern());
    }

    public void setCity(String city) {
        this.city = city.intern();
    }

    public void setTown(String town) {
        this.town = town.intern();
    }

    public void setVillage(String village) {
        this.village = village.intern();
    }

    public void setBuildingRange(String... buildingData) {
        if (buildingData.length == 1) {
            setBuilding(buildingData[0], buildingData[0], StreetParity.ALL.name());
        }
        else if (buildingData.length == 2) {
            primaryBuilding.setData(true, buildingData[0]);
            primaryBuilding.setData(false, buildingData[1]);
        }
        else if (buildingData.length == 3) {
            setBuilding(buildingData[0], buildingData[1], buildingData[2]);
        }
        else {
            throw new RuntimeException("Can't parse " + buildingData.length + " building fields.");
        }
    }

    public void setBuilding(String low, String high, String parity) {
        primaryBuilding.setData(true, low);
        primaryBuilding.setData(false, high);
        primaryBuilding.setParity(parity);
    }

    public void setSingletonBuilding(String bldg) {
        setBuilding(bldg, bldg, StreetParity.ALL.name());
    }

    public String getStreet() {
        return street;
    }

    public int getZip5() {
        return zip5;
    }

    public void addToStreet(String toAdd) {
        // TODO: remove multiple whitespace
        this.street = String.join(" ", street, toAdd);
    }

    public BuildingRange getBuildingRange() {
        return primaryBuilding;
    }

    public void setBuilding(boolean isLow, String data) {
        primaryBuilding.setData(isLow, data);
    }

    public void setBldgParity(String parity) {
        primaryBuilding.setParity(parity);
    }

    public Address getAddress(boolean isLow) {
        var addr = new Address(primaryBuilding.getBuilding(isLow) + " " + street);
        addr.setZip5(String.valueOf(zip5));
        addr.setState("NY");
        return addr;
    }
}
