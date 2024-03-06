package gov.nysenate.sage.client.view.map;

import gov.nysenate.sage.client.view.district.MemberView;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictType;

public class DistrictMapView
{
    protected String type;
    protected String district;
    protected String name;
    protected PolygonMapView map;
    protected Object member;
    protected String link;

    public DistrictMapView(DistrictMap districtMap)
    {
        this(districtMap, true);
    }

    public DistrictMapView(DistrictMap districtMap, boolean showMaps)
    {
        if (districtMap != null) {
            DistrictType districtType = districtMap.getDistrictType();
            if (districtType != null) {
                this.type = districtType.name();
            }
            this.district = districtMap.getDistrictCode();
            this.name = districtMap.getDistrictName();
            this.map = (showMaps) ? new PolygonMapView(districtMap) : null;
            if (districtType.equals(DistrictType.SENATE)) {
                this.member = districtMap.getSenator();
            }
            else if (districtType.equals(DistrictType.CONGRESSIONAL) || districtType.equals(DistrictType.ASSEMBLY)) {
                this.member = new MemberView(districtMap.getMember());
            }
            else if (districtType.equals(DistrictType.COUNTY)) {
                this.link = districtMap.getLink();
            }
        }
    }

    public String getType() {
        return type;
    }

    public String getDistrict() {
        return district;
    }

    public PolygonMapView getMap() {
        return map;
    }

    public String getName() {
        return name;
    }

    public Object getMember() {
        return member;
    }

    public String getLink() {
        return link;
    }
}
