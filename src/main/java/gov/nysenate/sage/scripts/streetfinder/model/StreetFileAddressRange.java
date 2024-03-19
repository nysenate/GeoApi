package gov.nysenate.sage.scripts.streetfinder.model;

import java.util.regex.Pattern;

/**
 * Represents a street address, as parsed from a streetfile.
 */
// TODO: rename to "addressRange"
public class StreetFileAddressRange {
    protected static final String DEFAULT = "\\N";
    private static final Pattern digitPattern = Pattern.compile("-?\\d+(\\.\\d+)?");
    private final BuildingRange primaryBuilding = new BuildingRange();
    private String street, zip5, city, town, village;

    public void setStreet(String street) {
        this.street = street.intern();
    }

    public void setZip5(String zip5) {
        this.zip5 = zip5.intern();
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

    public String getStreet() {
        return street;
    }

    public void addToStreet(String toAdd) {
        // TODO: remove multiple whitespace
        this.street += toAdd;
    }

    public String get(StreetFileField type) {
        return "fieldMap.get(type);";
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

    private static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        return digitPattern.matcher(strNum).matches();
    }
}
