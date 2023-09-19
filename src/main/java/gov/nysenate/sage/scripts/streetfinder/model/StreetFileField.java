package gov.nysenate.sage.scripts.streetfinder.model;

public enum StreetFileField {
    // In the SQL, COUNTY_ID is county_code, SENATE_TOWN_ABBREV is town_code, and CITY is city_code
    STREET(false), TOWN(false), STATE(false), ZIP(false, true),
    BUILDING(false), ELECTION_CODE(true, true), COUNTY_ID(true, true),
    ASSEMBLY(true, true), SENATE(true, true),
    CONGRESSIONAL(true, true), BOE_TOWN_CODE(true), SENATE_TOWN_ABBREV(true),
    WARD(true), BOE_SCHOOL(false), SCHOOL(false), CLEG(true),
    CITY_COUNCIL(true, true), FIRE(true), CITY(false), VILLAGE(true);

    private final boolean isNonEmpty, isNumeric;

    StreetFileField(boolean isNonEmpty) {
        this(isNonEmpty, false);
    }

    StreetFileField(boolean isNonEmpty, boolean isNumeric) {
        this.isNonEmpty = isNonEmpty;
        this.isNumeric = isNumeric;
    }

    public boolean isNonEmpty() {
        return isNonEmpty;
    }

    public boolean isNumeric() {
        return isNumeric;
    }
}
