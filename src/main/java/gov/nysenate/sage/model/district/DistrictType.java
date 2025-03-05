package gov.nysenate.sage.model.district;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum DistrictType {
    // Available as shape files
    ASSEMBLY("district", false), CONGRESSIONAL("district", false), SENATE("district", false),
    SCHOOL("tfcode", true), TOWN_CITY("abbrev", true),
    COUNTY("fips_code", true), ZIP("zip_code", false),
    // Available only in street files
    ELECTION, WARD, COUNTY_LEG, FIRE, VILLAGE, MUNICIPAL_COURT, CITY_COUNCIL;

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

    DistrictType(String code, boolean hasName) {
        this(hasName ? "NAME" : code, code);
    }

    DistrictType(String name, String code) {
        this.name = name;
        this.code = code;
    }

    /** Returns the DistrictType that matches the String representation */
    public static DistrictType resolveType(String type) {
        return type == null ? null : resolveMap.get(type.toUpperCase());
    }

    public static List<DistrictType> getStandardTypes() {
        return List.of(ASSEMBLY, CONGRESSIONAL, SENATE, SCHOOL, TOWN_CITY, COUNTY, ZIP);
    }

    public String getNameFromCode(String code) {
        return switch (this) {
            case SENATE -> "NY Senate District " + code;
            case ASSEMBLY ->  "NY Assembly District " + code;
            case CONGRESSIONAL ->   "NY Congressional District " + code;
            case ZIP ->  "Zipcode " + code;
            default -> null;
        };
    }

    public String nameColumn() {
        return name;
    }

    public String codeColumn() {
        return code;
    }

    public boolean hasShapefile() {
        return code != null;
    }
}
