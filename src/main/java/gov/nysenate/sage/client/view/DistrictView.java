package gov.nysenate.sage.client.view;

import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictType;

public class DistrictView
{
    protected String name;
    protected String district;

    public DistrictView(DistrictType district, DistrictInfo districtInfo)
    {
        if (districtInfo != null) {
            this.name = districtInfo.getDistName(district);
            this.district = districtInfo.getDistCode(district);
        }
    }

    public String getName() {
        return name;
    }

    public String getDistrict() {
        return district;
    }
}
