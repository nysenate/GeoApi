package gov.nysenate.sage.model.district;

import java.util.List;

/**
 * DistrictMatchLevel is a simplified accuracy measure of the district assignment result.
 */
public enum DistrictMatchLevel {
    NOMATCH, CITY, ZIP5, STREET, HOUSE;

    public static DistrictMatchLevel getMin(List<DistrictMatchLevel> levels) {
        return levels.stream().min(DistrictMatchLevel::compareTo).orElse(DistrictMatchLevel.NOMATCH);
    }

    public DistrictMatchLevel getNextHighestLevel() {
        return values()[ordinal() - 1];
    }
}
