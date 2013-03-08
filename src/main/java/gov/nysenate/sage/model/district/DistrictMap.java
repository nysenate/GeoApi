package gov.nysenate.sage.model.district;

import gov.nysenate.sage.model.geo.Polygon;

/**
 * Simple model to hold the geometry for a district boundary map.
 */
public class DistrictMap
{
    private Polygon polygon;

    public DistrictMap() {
        this(null);
    }

    public DistrictMap(Polygon polygon) {
        this.polygon = polygon;
    }

    public Polygon getPolygon() {
        return polygon;
    }

    public void setPolygon(Polygon polygon) {
        this.polygon = polygon;
    }

    public String toString() {
        return this.polygon.toString();
    }
}
