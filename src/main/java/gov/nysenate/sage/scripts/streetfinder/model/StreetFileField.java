package gov.nysenate.sage.scripts.streetfinder.model;

public enum StreetFileField {
    // In the SQL, COUNTY_ID is county_code, SENATE_TOWN_ABBREV is town_code, and CITY is city_code
    STREET, TOWN, STATE, ZIP, BUILDING,
    ELECTION_CODE, COUNTY_ID, ASSEMBLY, SENATE, CONGRESSIONAL, BOE_TOWN_CODE, SENATE_TOWN_ABBREV, WARD,
    BOE_SCHOOL, SCHOOL, CLEG, CITY_COUNCIL, FIRE, CITY, VILLAGE;
}
