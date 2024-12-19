package gov.nysenate.sage.dao.provider.district;

import gov.nysenate.sage.dao.base.BasicSqlQuery;

public enum ShapefileQueries implements BasicSqlQuery {
    GET_INTERSECTION("""
            SELECT *, ST_AsGeoJson(ST_CollectionExtract(intersection, 3)) AS intersect_geo_json, St_Area(intersection) AS area
            FROM (
                SELECT ${schema}.:intersectType.:nameField AS name, ${schema}.:intersectType.:codeField AS code
                    st_intersection(:baseType.geom, :intersectType.geom) AS intersection
                FROM :baseType, :intersectType
                WHERE :baseType.district = :districtCode AND st_intersects(:baseType.geom, :intersectType.geom)
            ) as temp;
        """);

    private final String query;

    ShapefileQueries(String query) {
        this.query = query;
    }

    @Override
    public String getSql() {
        return query;
    }
}
