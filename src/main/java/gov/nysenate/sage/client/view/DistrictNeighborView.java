package gov.nysenate.sage.client.view;

import gov.nysenate.sage.model.district.DistrictMap;

public class DistrictNeighborView
{
    protected String name;
    protected String district;
    protected MapView map;

    public DistrictNeighborView(DistrictMap neighborMap) {
        if (neighborMap != null) {
            this.name = neighborMap.getDistrictName();
            this.district = neighborMap.getDistrictCode();
            this.map = new MapView(neighborMap);
        }
    }

    public String getName() {
        return name;
    }

    public String getDistrict() {
        return district;
    }

    public MapView getMap() {
        return map;
    }
}
