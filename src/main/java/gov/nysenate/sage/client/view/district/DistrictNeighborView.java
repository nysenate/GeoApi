package gov.nysenate.sage.client.view.district;

import gov.nysenate.sage.client.view.map.PolygonMapView;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictType;

public class DistrictNeighborView
{
    protected String name;
    protected String district;
    protected Object member;
    protected PolygonMapView map;

    public DistrictNeighborView(DistrictMap neighborMap) {
        if (neighborMap != null) {
            this.name = neighborMap.getDistrictName();
            this.district = neighborMap.getDistrictCode();
            this.map = new PolygonMapView(neighborMap);
            this.member = (neighborMap.getDistrictType().equals(DistrictType.SENATE)) ? neighborMap.getSenator() :
                                                                                        neighborMap.getMember();
        }
    }

    public String getName() {
        return name;
    }

    public String getDistrict() {
        return district;
    }

    public PolygonMapView getMap() {
        return map;
    }

    public Object getMember() {
        return member;
    }
}
