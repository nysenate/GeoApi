package gov.nysenate.sage.client.view;

import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictType;

public class DistrictMapView
{
    protected String type;
    protected String district;
    protected String name;
    protected MapView map;
    protected Object member;

    public DistrictMapView(DistrictMap districtMap)
    {
        if (districtMap != null) {
            DistrictType districtType = districtMap.getDistrictType();
            if (districtType != null) {
                this.type = districtType.name();
            }
            this.district = districtMap.getDistrictCode();
            this.name = districtMap.getDistrictName();
            this.map = new MapView(districtMap);
            if (districtType.equals(DistrictType.SENATE)) {
                this.member = districtMap.getSenator();
            }
            else if (districtType.equals(DistrictType.CONGRESSIONAL) || districtType.equals(DistrictType.ASSEMBLY)) {
                this.member = new MemberView(districtMap.getMember());
            }
        }
    }

    public String getType() {
        return type;
    }

    public String getDistrict() {
        return district;
    }

    public MapView getMap() {
        return map;
    }

    public String getName() {
        return name;
    }

    public Object getMember() {
        return member;
    }
}
