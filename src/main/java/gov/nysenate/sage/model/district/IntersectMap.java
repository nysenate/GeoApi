package gov.nysenate.sage.model.district;

import gov.nysenate.sage.model.geo.Polygon;

import java.util.List;

public class IntersectMap extends DistrictMap {
    private List<Polygon> fullMapPolygons;

    public IntersectMap(DistrictType type, String name, String code) {
        super(type, name, code);
    }

    public List<Polygon> getFullMapPolygons() {
        return fullMapPolygons;
    }

    public void setFullMapPolygons(List<Polygon> fullMapPolygons) {
        this.fullMapPolygons = fullMapPolygons;
    }
}
