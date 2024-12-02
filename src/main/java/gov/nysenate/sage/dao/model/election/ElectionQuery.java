package gov.nysenate.sage.dao.model.election;

import gov.nysenate.sage.dao.base.BasicSqlQuery;

public enum ElectionQuery implements BasicSqlQuery {
    CLEAR_TABLE("TRUNCATE TABLE districts.election"),

    // TODO: Senate district 26 and Assembly district 61 should be included, but they are currently split into 3 parts
    SELECT_ELECTION_DISTRICTS("""
            SELECT election_district, town_city_gid, assembly_district, senate_district, county_fips_code, congressional_district FROM public.streetfile
            WHERE (election_district + town_city_gid + assembly_district + senate_district + county_fips_code + congressional_district) IS NOT NULL AND
            senate_district != 26 AND assembly_district != 61
            GROUP BY election_district, town_city_gid, assembly_district, senate_district, county_fips_code, congressional_district;"""),

    INSERT_MAPS("""
            INSERT INTO districts.election(election_district, town_city_gid, assembly_district, senate_district, county_fips, congressional_district, geom)
            VALUES (:electionId, :gid, :assemblyId, :senateId, :countyFips, :congressionalId,
                    (SELECT st_intersection(districts.town_city.geom, st_intersection(districts.assembly.geom, st_intersection(districts.senate.geom,
                            st_intersection(districts.county.geom, districts.congressional.geom)))) AS inter
                     FROM districts.town_city, districts.assembly, districts.senate,
                          districts.county, districts.congressional
                     WHERE districts.assembly.district = :assemblyId AND districts.senate.district = :senateId AND
                         districts.county.fips_code = :countyFips AND districts.congressional.district = :congressionalId AND
                         districts.town_city.gid = :gid));"""),

    REMOVE_EMPTY_MAPS("DELETE FROM districts.election WHERE st_area(geom) = 0");

    private final String sql;

    ElectionQuery(String sql) {
        this.sql = sql;
    }

    @Override
    public String getSql() {
        return sql;
    }
}
