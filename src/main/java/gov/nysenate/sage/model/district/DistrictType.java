package gov.nysenate.sage.model.district;

import org.apache.commons.lang.WordUtils;

public enum DistrictType {
    // Available as shape files
    SENATE("district", false), ASSEMBLY("district", false), CONGRESSIONAL("district", false),
    ZIP("zip_code", false), COUNTY("senate_code", true), TOWN_CITY("abbrev", true),
    SCHOOL("tfcode", true), ELECTRIC_UTILITY("gid", true),
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

    public String getDisplayName() {
        if (this == TOWN_CITY) {
            return "Town/City";
        }
        return WordUtils.capitalizeFully(name().replace('_', ' '));
    }
}
