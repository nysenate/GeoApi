package gov.nysenate.sage.model.district;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum DistrictType {
    // Available as shape files
    // TODO: town -> townCity
    ASSEMBLY("DISTRICT"), CONGRESSIONAL("DISTRICT"), SENATE("DISTRICT"), SCHOOL("TFCODE"), TOWN("ABBREV"),
    COUNTY("namelsad", "COUNTYFP"), ZIP("zip_code", "zip_code"),
    // Available only in street files
    ELECTION, WARD, CLEG, FIRE, VILLAGE, MUNICIPAL_COURT, CITY_COUNCIL;

    /** A Map container is used to associate type names with the enum type */
    private static final Map<String, DistrictType> resolveMap = new HashMap<>();
    static {
        for (DistrictType dt : values()) {
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
        this("NAME", code);
    }

    DistrictType(String name, String code) {
        this(name, code, 4326);
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
