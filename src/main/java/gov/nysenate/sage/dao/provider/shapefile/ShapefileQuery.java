package gov.nysenate.sage.dao.provider.shapefile;

import gov.nysenate.sage.dao.base.BasicSqlQuery;

public enum ShapefileQuery implements BasicSqlQuery {
    // These maps are only ever displayed, so the geometry is simplified to cut the payload.
    // ST_CoverageSimplify, not ST_Simplify: it treats a shared border once, where simplifying
    // neighbours independently pulls their common edge two different ways and tears visible gaps
    // along it. ST_CoverageSimplify throws outright on an invalid polygon, so the repair guards
    // the display path against a layer that failed the validity check cleanMaps reports on. The
    // area still comes off the full geometry.
    GET_DISTRICT_MAPS("""
            SELECT *, ST_AsGeoJson(ST_CoverageSimplify(ST_MakeValid(full_geom), tolerance) OVER ()) AS map,
                area_in_sq_km(full_geom) AS area
            FROM (
                SELECT ${idColumn} AS id, ST_Multi(ST_Union(geom)) AS full_geom
                FROM ${schema}.${type}
                GROUP BY ${idColumn}
            ) AS temp
            CROSS JOIN (
                -- A hundredth of the smallest district's width, 111320m being a degree of
                -- latitude. One fixed tolerance can't serve both a county and a city election
                -- district, so it's derived per type rather than configured.
                SELECT LEAST(0.0005, GREATEST(0.00002,
                    MIN(SQRT(ST_Area(geom::geography)))/100/111320)) AS tolerance
                FROM ${schema}.${type}
            ) AS tol"""),

    GET_DISTRICT_FROM_POINT("""
            SELECT ${idColumn}::text AS id
            FROM ${schema}.${type}
            WHERE ST_Contains(geom, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326));
            """),

    GET_INTERSECTION("""
            SELECT *, ST_AsGeoJson(ST_CollectionExtract(intersection_geom, 3)) AS intersect_geo_json,
                area_in_sq_km(intersection_geom) AS area
            FROM (
                SELECT ${intersectType}.${idColumn} AS id,
                    ST_Intersection(${baseType}.geom, ${intersectType}.geom) AS intersection_geom
                FROM ${schema}.${baseType}, ${schema}.${intersectType}
                WHERE ${baseType}.${baseIdColumn}::varchar = :districtId AND ST_Intersects(${baseType}.geom, ${intersectType}.geom)
            ) as temp ORDER BY area DESC"""),

    GET_IDS("""
            SELECT ${idColumn} AS id, COUNT(*) AS id_count, MIN(gid) AS main_gid
            FROM ${schema}.${type}
            GROUP BY ${idColumn}
            """),

    SET_UNION("""
            UPDATE ${schema}.${type}
            SET geom = (
                SELECT ST_Multi(St_Union(geom)) FROM ${schema}.${type} WHERE ${idColumn}::text = :id GROUP BY ${idColumn}
            )
            WHERE gid = :mainGid
            """),

    DELETE_REDUNDANT_MAPS("""
            DELETE FROM ${schema}.${type}
            WHERE ${idColumn}::text = :id AND gid != :mainGid
            """),

    // Ensures that IDs are unique identifiers.
    ADD_UNIQUE_ID_INDEX("""
            CREATE UNIQUE INDEX IF NOT EXISTS ${type}_${idColumn}_unique_idx
            ON ${schema}.${type} (${idColumn})
            """),

    IS_TYPE_VALID("""
            SELECT bool_and(ST_IsValid(geom)) AS valid
            FROM ${schema}.${type}
            """);


    private final String query;

    ShapefileQuery(String query) {
        this.query = query;
    }

    @Override
    public String getSql() {
        return query;
    }
}
