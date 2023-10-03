package gov.nysenate.sage.scripts.streetfinder.model;

public class SuffolkStreetFileAddressRange extends StreetFileAddressRange {
    // Really a range for apartment buildings.
    protected final BuildingRange secondaryBuilding = new BuildingRange();
    public SuffolkStreetFileAddressRange() {
        super();
    }

    public void setSecondaryBuilding(boolean isLow, String data) {
        secondaryBuilding.setData(isLow, data);
    }

    public void setSecondaryBuildingParity(String parity) {
        secondaryBuilding.setParity(parity);
    }
}
