package gov.nysenate.sage.model.district;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum DistrictType {
    // Available as shape files
    ASSEMBLY("DISTRICT"), CONGRESSIONAL("DISTRICT"), SENATE("DISTRICT"), SCHOOL("TFCODE"), TOWN("ABBREV"),
    COUNTY("namelsad", "COUNTYFP", 4326), ZIP("zip_code", "zip_code", 4326),
    // Available only in street files
    ELECTION, WARD, CLEG, CITY, FIRE, VILLAGE, CITY_COUNCIL;

    /** A Map container is used to associate type names with the enum type */
    private static final Map<String, DistrictType> resolveMap = new HashMap<>();
    static {
        for (DistrictType dt : values()){
            resolveMap.put(dt.name().toUpperCase(), dt);
        }
    }
    // Column names in the database
    private final String name, code;
    // Specifies the format of shapefile data.
    private final Integer srid;

    // For types that don't currently have shapefiles.
    DistrictType() {
        this(null, null, null);
    }

    DistrictType(String code) {
        this("NAME", code, 4326);
    }

    DistrictType(String name, String code, Integer srid) {
        this.name = name;
        this.code = code;
        this.srid = srid;
    }

    /** Returns the DistrictType that matches the String representation */
    public static DistrictType resolveType(String type) {
        return (type == null? null : resolveMap.get(type.toUpperCase()));
    }

    public static List<DistrictType> getStandardTypes() {
        return List.of(ASSEMBLY, CONGRESSIONAL, SENATE, SCHOOL, TOWN, COUNTY, ZIP);
    }

    public static List<DistrictType> getStateBasedTypes() {
        return List.of(ASSEMBLY, CONGRESSIONAL, SENATE, COUNTY);
    }

    public boolean isCountyBased() {
        return this == SCHOOL || this == TOWN || this == CLEG;
    }

    public boolean isTownBased() {
        return this == WARD || this == ELECTION;
    }

    public String nameColumn() {
        return name;
    }

    public String codeColumn() {
        return code;
    }

    public String sridColumn() {
        return srid == null ? null : Integer.toString(srid);
    }
}
