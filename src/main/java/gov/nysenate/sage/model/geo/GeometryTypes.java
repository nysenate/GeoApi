package gov.nysenate.sage.model.geo;

public enum GeometryTypes {
    LINESTRING("LineString"), MULTILINESTRING("MultiLineString"), POLYGON("Polygon"), MULTIPOLYGON("MultiPolygon");
    private final String type;

    GeometryTypes(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
