package gov.nysenate.sage.model.address;

public class SuffolkStreetAddress extends StreetFinderAddress {
    public void setSecondaryBuilding(boolean isLow, String data) {
        if (isLow) {
            secondaryBuilding.setLow(data);
        }
        else {
            secondaryBuilding.setHigh(data);
        }
    }

    public void setSecondaryBuildingParity(String parity) {
        secondaryBuilding.setParity(parity);
    }
}
