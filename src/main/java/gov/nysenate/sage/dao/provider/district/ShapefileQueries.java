package gov.nysenate.sage.dao.provider.district;

import gov.nysenate.sage.dao.base.BasicSqlQuery;

public enum ShapefileQueries implements BasicSqlQuery {
    GET_DISTRICT_MAP("""
            SELECT *, ST_AsGeoJson(full_geom) AS map, area_in_sq_km(full_geom) AS area
            FROM (
                SELECT ${nameColumn} AS name, ${codeColumn} AS code, ST_Union(geom) AS full_geom
                FROM ${schema}.${type}
                GROUP BY ${nameColumn}, ${codeColumn}
            ) AS temp"""),

    GET_DISTRICT_FROM_POINT("""
            SELECT ${codeColumn}::text AS code
            FROM ${schema}.${type}
            WHERE ST_Contains(geom, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326));
            """),

    GET_INTERSECTION("""
            SELECT *, ST_AsGeoJson(ST_CollectionExtract(intersection_geom, 3)) AS intersect_geo_json,
                area_in_sq_km(intersection_geom) AS area
            FROM (
                SELECT ${intersectType}.${codeColumn} AS code,
                    ST_Intersection(${baseType}.geom, ${intersectType}.geom) AS intersection_geom
                FROM ${schema}.${baseType}, ${schema}.${intersectType}
                WHERE ${baseType}.${baseCodeColumn}::varchar = :districtCode AND ST_Intersects(${baseType}.geom, ${intersectType}.geom)
            ) as temp ORDER BY area DESC"""),

    CLEAN_CODES("""
            UPDATE ${schema}.${type}
            SET ${codeColumn} = TRIM(TRIM(LEADING '0' FROM ${codeColumn}))
            """),

    GET_CODES("""
            SELECT ${codeColumn} AS code, COUNT(*) AS code_count, MIN(gid) AS main_gid
            FROM ${schema}.${type}
            GROUP BY ${codeColumn}
            """),

    SET_UNION("""
            UPDATE ${schema}.${type}
            SET geom = (
                SELECT St_Union(geom) FROM ${schema}.${type} WHERE ${codeColumn} = :code GROUP BY ${codeColumn}
            )
            WHERE gid = :mainGid
            """),

    DELETE_REDUNDANT_MAPS("""
            DELETE FROM ${schema}.${type}
            WHERE ${codeColumn} = :code AND gid != :mainGid
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
