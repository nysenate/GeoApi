package gov.nysenate.sage.dao.provider.district;

import gov.nysenate.sage.dao.base.BasicSqlQuery;

public enum ShapefileQueries implements BasicSqlQuery {
    // TODO: combining town and city maps
    GET_DISTRICT_MAP("""
            SELECT *, St_Area(map) AS area
            FROM (
                SELECT ${nameColumn} AS name, ${codeColumn} AS code, ST_AsGeoJson(ST_Union(geom)) AS map
                FROM ${schema}.${type}
                GROUP BY ${nameColumn}, ${codeColumn}
            ) AS temp"""),

    GET_DISTRICT_FROM_POINT("""
            SELECT ${nameColumn}::text AS name, ${nameColumn}::text AS code
            FROM ${schema}.${type}
            WHERE ST_Contains(geom, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326));
            """),

    GET_INTERSECTION("""
            SELECT *, ST_AsGeoJson(ST_CollectionExtract(intersection_geom, 3)) AS intersect_geo_json, ST_Area(intersection_geom) AS area
            FROM (
                SELECT ${intersectType}.${nameColumn} AS name, ${intersectType}.${codeColumn} AS code,
                    ST_Intersection(${baseType}.geom, ${intersectType}.geom) AS intersection_geom
                FROM ${schema}.${baseType}, ${schema}.${intersectType}
                WHERE ${baseType}.${baseCodeColumn} = :districtCode AND ST_Intersects(${baseType}.geom, ${intersectType}.geom)
            ) as temp ORDER BY area DESC""");

    private final String query;

    ShapefileQueries(String query) {
        this.query = query;
    }

    @Override
    public String getSql() {
        return query;
    }
}
