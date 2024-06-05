package gov.nysenate.sage.model.district;

/**
 * DistrictMatchLevel is a simplified accuracy measure of the district assignment result. The
 * numerical values are chosen somewhat arbitrarily to instill a ranking.
 */
public enum DistrictMatchLevel {
    NOMATCH(0), STATE(10), CITY(50), ZIP5(70), STREET(80), HOUSE(100);

    private final int value;

    DistrictMatchLevel(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public DistrictMatchLevel getNextHighestLevel() {
        return ordinal() == 0 ? null : values()[ordinal() - 1];
    }
}
