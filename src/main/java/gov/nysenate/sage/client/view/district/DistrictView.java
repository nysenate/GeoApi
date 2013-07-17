package gov.nysenate.sage.client.view.district;

import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.util.FormatUtil;

import java.util.ArrayList;
import java.util.List;

public class DistrictView
{
    protected String name;
    protected String district;

    public DistrictView(DistrictType districtType, DistrictInfo districtInfo)
    {
        if (districtInfo != null) {
            DistrictMap map = districtInfo.getDistMap(districtType);
            this.name = (map != null && map.getDistrictName() != null) ? map.getDistrictName() : districtInfo.getDistName(districtType);
            this.district = districtInfo.getDistCode(districtType);
        }
    }

    public String getName() {
        return name;
    }

    public String getDistrict() {
        if (district != null && (district.isEmpty() || FormatUtil.trimLeadingZeroes(district).equals("0"))) {
            return null;
        }
        return district;
    }
}
