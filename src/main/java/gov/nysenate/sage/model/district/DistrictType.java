package gov.nysenate.sage.model.district;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum DistrictType {
    // Available as shape files
    ASSEMBLY("district"), CONGRESSIONAL("district"), SENATE("district"),
    SCHOOL("TFCODE"), TOWN_CITY("ABBREV"),
    COUNTY("fips_code"), ZIP("zip_code", "zip_code"),
    // Available only in street files
    ELECTION("district", "election_district"), WARD, COUNTY_LEG, FIRE, VILLAGE, MUNICIPAL_COURT, CITY_COUNCIL;

    /** A Map container is used to associate type names with the enum type */
    private static final Map<String, DistrictType> resolveMap = new HashMap<>();
    static {
        for (DistrictType dt : values()) {
            resolveMap.put(dt.name().toUpperCase(), dt);
        }
    }
    // Column names in the database
    private final String name, code;

    // For types that don't currently have shapefiles.
    DistrictType() {
        this(null, null);
    }

    DistrictType(String code) {
        this(code.equalsIgnoreCase("district") ? code : "NAME", code);
    }

    DistrictType(String name, String code) {
        this.name = name;
        this.code = code;
    }

    /** Returns the DistrictType that matches the String representation */
    public static DistrictType resolveType(String type) {
        return (type == null ? null : resolveMap.get(type.toUpperCase()));
    }

    public static List<DistrictType> getStandardTypes() {
        return List.of(ASSEMBLY, CONGRESSIONAL, SENATE, SCHOOL, TOWN_CITY, COUNTY, ZIP);
    }

    public String nameColumn() {
        return name;
    }

    public String codeColumn() {
        return code;
    }
}
