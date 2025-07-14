package gov.nysenate.sage.model.district;

import gov.nysenate.sage.model.geo.Polygon;

import java.util.List;

public class IntersectMap extends DistrictMap {
    private List<Polygon> fullMapPolygons;

    public IntersectMap() {
        super();
    }

    public List<Polygon> getFullMapPolygons() {
        return fullMapPolygons;
    }

    public void setFullMapPolygons(List<Polygon> fullMapPolygons) {
        this.fullMapPolygons = fullMapPolygons;
    }
}
