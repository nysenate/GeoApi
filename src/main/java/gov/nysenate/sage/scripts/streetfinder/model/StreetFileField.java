package gov.nysenate.sage.scripts.streetfinder.model;

public enum StreetFileField {
    // In the SQL, COUNTY_ID is county_code, and SENATE_TOWN_ABBREV is town_code
    STREET(false), TOWN(false), STATE(false), ZIP(false),
    ELECTION_CODE, COUNTY_ID, ASSEMBLY, SENATE, CONGRESSIONAL, BOE_TOWN_CODE, SENATE_TOWN_ABBREV, WARD,
    BOE_SCHOOL, SCHOOL, CLEG, CITY_COUNCIL, FIRE, CITY, VILLAGE;

    private final boolean afterBuildings;

    StreetFileField(boolean afterBuildings) {
        this.afterBuildings = afterBuildings;
    }

    StreetFileField() {
        this(true);
    }

    public boolean isAfterBuildings() {
        return afterBuildings;
    }
}
