package gov.nysenate.sage.model.address;

import gov.nysenate.sage.model.address.StreetAddressRange;
import gov.nysenate.sage.model.district.DistrictInfo;

public class DistrictStreetRange
{
    protected StreetAddressRange streetAddressRange;
    protected DistrictInfo districtInfo;

    public DistrictStreetRange() {}

    public DistrictStreetRange(StreetAddressRange streetAddressRange, DistrictInfo districtInfo) {
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
