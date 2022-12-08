package gov.nysenate.sage.scripts.streetfinder.model;

public class SuffolkStreetFileAddress extends StreetFileAddress {
    public SuffolkStreetFileAddress() {
        super();
    }

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
