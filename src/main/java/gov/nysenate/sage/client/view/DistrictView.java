package gov.nysenate.sage.client.view;

import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictType;

import java.util.ArrayList;
import java.util.List;

public class DistrictView
{
    protected String name;
    protected String district;
    protected MapView map;
    protected boolean nearBorder;
    public List<DistrictNeighborView> neighbors = new ArrayList<>();

    public DistrictView(DistrictType districtType, DistrictInfo districtInfo)
    {
        if (districtInfo != null) {
            this.name = districtInfo.getDistName(districtType);
            this.district = districtInfo.getDistCode(districtType);
            this.map = new MapView(districtInfo.getDistMap(districtType));
            this.nearBorder = districtInfo.getNearBorderDistricts().contains(districtType);
            for (DistrictMap neighborMap : districtInfo.getNeighborMaps(districtType)) {
                neighbors.add(new DistrictNeighborView(neighborMap));
            }
        }
    }

    public String getName() {
        return name;
    }

    public String getDistrict() {
        return district;
    }

    public MapView getMap() {
        return (map != null && map.geom != null) ? map : null;
    }

    public boolean isNearBorder() {
        return nearBorder;
    }

    public List<DistrictNeighborView> getNeighbors() {
        return neighbors;
    }
}
