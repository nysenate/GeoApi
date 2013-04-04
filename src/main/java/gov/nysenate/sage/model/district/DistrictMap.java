package gov.nysenate.sage.model.district;

import gov.nysenate.sage.model.geo.Polygon;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple model to hold the geometry for a district boundary map. Some maps
 * are represented as multiple polygons.
 */
public class DistrictMap
{
    private List<Polygon> polygons = new ArrayList<>();

    public DistrictMap() {}

    public DistrictMap(List<Polygon> polygons) {
        this.polygons = polygons;
    }

    public List<Polygon> getPolygons() {
        return polygons;
    }

    public void setPolygons(List<Polygon> polygons) {
        this.polygons = polygons;
    }

    public void addPolygon(Polygon polygon) {
        this.polygons.add(polygon);
    }

    public String toString()
    {
        String o = "";
        if (polygons != null) {
            for (Polygon polygon : polygons) {
                o += polygon.toString();
            }
        }
        return o;
    }
}
