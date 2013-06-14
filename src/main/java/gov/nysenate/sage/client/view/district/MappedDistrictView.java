package gov.nysenate.sage.client.view.district;

import gov.nysenate.sage.client.view.map.MapView;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictType;

import java.util.ArrayList;
import java.util.List;

public class MappedDistrictView extends DistrictView
{
    protected MapView map;
    protected boolean nearBorder;
    public List<DistrictNeighborView> neighbors = new ArrayList<>();

    public MappedDistrictView(DistrictType districtType, DistrictInfo districtInfo)
    {
        super(districtType, districtInfo);
        if (districtInfo != null) {
            this.map = new MapView(districtInfo.getDistMap(districtType));
            this.nearBorder = districtInfo.getNearBorderDistricts().contains(districtType);
            for (DistrictMap neighborMap : districtInfo.getNeighborMaps(districtType)) {
                neighbors.add(new DistrictNeighborView(neighborMap));
            }
        }
    }

    public MapView getMap() {
        return (map != null && map.getGeom() != null && map.getGeom().size() > 0) ? map : null;
    }

    public boolean isNearBorder() {
        return nearBorder;
    }

    public List<DistrictNeighborView> getNeighbors() {
        return neighbors;
    }
}
