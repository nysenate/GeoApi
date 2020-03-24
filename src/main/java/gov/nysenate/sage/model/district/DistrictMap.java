package gov.nysenate.sage.model.district;

import gov.nysenate.sage.model.geo.Polygon;

import java.util.ArrayList;
import java.util.List;

/**
 * Extends DistrictMetadata with district map geometry information.
 */
public class DistrictMap extends DistrictMetadata
{
    private List<Polygon> polygons = new ArrayList<>();
    private String geometryType = "";

    public DistrictMap() {}

    public DistrictMap(List<Polygon> polygons) {
        this.polygons = polygons;
    }

    public void setDistrictMetadata(DistrictMetadata dm) {
        this.setDistrictCode(dm.districtCode);
        this.setDistrictName(dm.districtName);
        this.setDistrictType(dm.districtType);
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

    public String getGeometryType() {
        return geometryType;
    }

    public void setGeometryType(String geometryType) {
        this.geometryType = geometryType;
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

    public void setLink(String link) {
        super.setLink(link);
    }
}