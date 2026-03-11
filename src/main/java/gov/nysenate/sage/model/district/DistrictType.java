package gov.nysenate.sage.model.district;

public enum DistrictType {
    // Available as shape files
    ASSEMBLY("district", false), CONGRESSIONAL("district", false), SENATE("district", false),
    SCHOOL("tfcode", true), TOWN_CITY("abbrev", true),
    COUNTY("senate_code", true), ZIP("zip_code", false), ELECTRIC_UTILITY("gid", true),
    // Available only in street files
    ELECTION, WARD, COUNTY_LEG, FIRE, VILLAGE, MUNICIPAL_COURT, CITY_COUNCIL;

    // Column names in the database
    private final String name, code;

    // For types that don't currently have shapefiles.
    DistrictType() {
        this(null, null);
    }

    DistrictType(String code, boolean hasName) {
        this(hasName ? "name" : code, code);
    }

    DistrictType(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public String nameColumn() {
        return name;
    }

    public String codeColumn() {
        return code;
    }

    public boolean lacksShapefile() {
        return code == null;
    }
}
