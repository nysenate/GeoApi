package gov.nysenate.sage.model.district;

import gov.nysenate.sage.model.geo.Polygon;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Extends DistrictMetadata with district map geometry information.
 */
public class DistrictMap extends DistrictMetadata {
    private List<Polygon> polygons = new ArrayList<>();
    private String geometryType = "";
    private BigDecimal area;

    public DistrictMap() {}

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

    public BigDecimal getArea() {
        return area;
    }

    public void setArea(BigDecimal area) {
        this.area = area;
    }

    public String toString() {
        var o = new StringBuilder();
        if (polygons != null) {
            for (Polygon polygon : polygons) {
                o.append(polygon.toString());
            }
        }
        return o.toString();
    }

    public void setLink(String link) {
        super.setLink(link);
    }
}