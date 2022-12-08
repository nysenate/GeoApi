package gov.nysenate.sage.scripts.streetfinder.model;

public enum StreetFileField {
    STREET(false), TOWN(false), STATE(false), ZIP(false),
    ELECTION_CODE, COUNTY_CODE, ASSEMBLY, SENATE, CONGRESSIONAL, BOE_TOWN_CODE, TOWN_CODE, WARD,
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
