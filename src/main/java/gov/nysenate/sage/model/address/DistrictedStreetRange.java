package gov.nysenate.sage.model.address;

import gov.nysenate.sage.model.district.DistrictInfo;

public class DistrictedStreetRange
{
    protected StreetAddressRange streetAddressRange;
    protected DistrictInfo districtInfo;

    public DistrictedStreetRange() {}

    public DistrictedStreetRange(StreetAddressRange streetAddressRange, DistrictInfo districtInfo) {
        this.streetAddressRange = streetAddressRange;
        this.districtInfo = districtInfo;
    }

    public StreetAddressRange getStreetAddressRange() {
        return streetAddressRange;
    }

    public void setStreetAddressRange(StreetAddressRange streetAddressRange) {
        this.streetAddressRange = streetAddressRange;
    }

    public DistrictInfo getDistrictInfo() {
        return districtInfo;
    }

    public void setDistrictInfo(DistrictInfo districtInfo) {
        this.districtInfo = districtInfo;
    }
}
