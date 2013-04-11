package gov.nysenate.sage.client.view;

import gov.nysenate.sage.model.district.DistrictMap;

public class DistrictMapView
{
    protected String type;
    protected String district;
    protected MapView map;

    public DistrictMapView(DistrictMap districtMap)
    {
        if (districtMap != null) {
            if (districtMap.getDistrictType() != null) {
                this.type = districtMap.getDistrictType().name();
            }
            this.district = districtMap.getDistrictCode();
            this.map = new MapView(districtMap);
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
}
