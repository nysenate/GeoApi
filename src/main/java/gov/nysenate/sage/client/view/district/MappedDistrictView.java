package gov.nysenate.sage.client.view.district;

import gov.nysenate.sage.client.view.map.PolygonMapView;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictType;

import java.util.ArrayList;
import java.util.List;

public class MappedDistrictView extends DistrictView {
    protected PolygonMapView map;
    protected boolean nearBorder;
    public List<DistrictNeighborView> neighbors = new ArrayList<>();

    public MappedDistrictView(DistrictType districtType, DistrictInfo districtInfo) {
        super(districtType, districtInfo);
        if (districtInfo != null) {
            this.map = new PolygonMapView(districtInfo.getDistMap(districtType));
            this.nearBorder = districtInfo.getNearBorderDistricts().contains(districtType);
            for (DistrictMap neighborMap : districtInfo.getNeighborMaps(districtType)) {
                neighbors.add(new DistrictNeighborView(neighborMap));
            }
        }
    }

    public PolygonMapView getMap() {
        return (map != null && map.getGeom() != null && map.getGeom().isEmpty()) ? null : map;
    }

    public boolean isNearBorder() {
        return nearBorder;
    }

    public List<DistrictNeighborView> getNeighbors() {
        return neighbors;
    }
}
