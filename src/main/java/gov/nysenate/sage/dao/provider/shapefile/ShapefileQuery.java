package gov.nysenate.sage.dao.provider.shapefile;

import gov.nysenate.sage.dao.base.BasicSqlQuery;

public enum ShapefileQuery implements BasicSqlQuery {
    // Map geometry is simplified to cut the payload.
    // The 0.0001-degree tolerance (~11m) and 6 coordinate digits (~10cm) are invisible at
    // display zooms: this causes no district collapses, the median border barely shifts,
    // and no type has even 0.1% of its area displaced. In return, the largest layers shrink ~3x.
    GET_DISTRICT_MAPS("""
            SELECT *, ST_AsGeoJson(ST_CoverageSimplify(ST_MakeValid(full_geom), 0.0001) OVER (), 6) AS map,
                area_in_sq_km(full_geom) AS area
            FROM (
                SELECT ${idColumn} AS id, ST_Multi(ST_Union(geom)) AS full_geom
                FROM ${schema}.${type}
                GROUP BY ${idColumn}
            ) AS temp"""),

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
