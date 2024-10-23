package gov.nysenate.sage.model.district;

/**
 * DistrictMatchLevel is a simplified accuracy measure of the district assignment result.
 */
public enum DistrictMatchLevel {
    // TODO: is NOMATCH really different from STATE?
    NOMATCH, STATE, CITY, ZIP5, STREET, HOUSE;

    public DistrictMatchLevel getNextHighestLevel() {
        return values()[ordinal() - 1];
    }
}
