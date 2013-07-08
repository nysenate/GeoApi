package gov.nysenate.sage.model.geo;

public enum GeometryTypes
{
    LINESTRING("LineString"), MULTILINESTRING("MultiLineString"), POLYGON("Polygon"), MULTIPOLYGON("MultiPolygon");
    public String type;
    GeometryTypes(String type) {
        this.type = type;
    }
}
