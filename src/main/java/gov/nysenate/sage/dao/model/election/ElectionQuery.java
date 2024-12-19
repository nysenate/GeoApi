package gov.nysenate.sage.dao.model.election;

import gov.nysenate.sage.dao.base.BasicSqlQuery;

public enum ElectionQuery implements BasicSqlQuery {
    CLEAR_TABLE("TRUNCATE TABLE districts.election"),

    SELECT_ELECTION_DISTRICTS("""
            SELECT election_district, town_city_gid, assembly_district, senate_district, county_fips_code, congressional_district FROM public.streetfile
            WHERE (election_district + town_city_gid + assembly_district + senate_district + county_fips_code + congressional_district) IS NOT NULL
            GROUP BY election_district, town_city_gid, assembly_district, senate_district, county_fips_code, congressional_district;"""),

    INSERT_MAPS("""
            INSERT INTO districts.election(election_district, town_city_gid, assembly_district, senate_district, county_fips, congressional_district, geom)
            VALUES (:electionId, :townCityId, :assemblyId, :senateId, :countyFips, :congressionalId, ST_CollectionExtract(
                    (SELECT st_intersection(ST_UNION(districts.town_city.geom), st_intersection(ST_UNION(districts.assembly.geom), st_intersection(ST_UNION(districts.senate.geom),
                            st_intersection(ST_UNION(districts.county.geom), ST_UNION(districts.congressional.geom))))) AS inter
                     FROM districts.town_city, districts.assembly, districts.senate,
                          districts.county, districts.congressional
                     WHERE districts.assembly.district = :assemblyId AND districts.senate.district = :senateId AND
                         districts.county.fips_code = :countyFips AND districts.congressional.district = :congressionalId AND
                         districts.town_city.gid = :townCityId
                    ))
            );"""),

    REMOVE_EMPTY_MAPS("DELETE FROM districts.election WHERE st_area(geom) = 0"),

    SEPARATE_ELECTION_DISTRICTS("""
            UPDATE districts.election
            SET geom = (
                SELECT ST_Union(town_city_polygons.geom) FROM public.town_city_polygons
                WHERE town_city_id = :town_city_id AND ST_Intersects(town_city_polygons.geom, ST_MPointFromText('MULTIPOINT(:points)'))
            ) WHERE election_district = :electionId AND town_city_gid = :townCityId AND
                    assembly_district = :assemblyId AND election.county_fips = :countyFips AND
                    senate_district = :senateId AND congressional_district = :congresiionalId;
            """),
    ;

    private final String sql;

    ElectionQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return sql;
    }
}
