package gov.nysenate.sage.scripts.streetfinder.model;

public class StreetfileAddressRangeWithApt extends StreetfileAddressRange {
    private String aptType, aptNum;

    public StreetfileAddressRangeWithApt(BuildingRange buildingRange, AddressWithoutNum addressWithoutNum) {
        super(buildingRange, addressWithoutNum);
    }

    public String getAptType() {
        return aptType;
    }

    public void setAptType(String aptType) {
        this.aptType = aptType;
    }

    public String getAptNum() {
        return aptNum;
    }

    public void setAptNum(String aptNum) {
        this.aptNum = aptNum;
    }
}
