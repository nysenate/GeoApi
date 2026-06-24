package gov.nysenate.sage.model.district;

import java.util.List;

/**
 * DistrictMatchLevel is a simplified accuracy measure of the district assignment result.
 */
public enum DistrictMatchLevel {
    NOMATCH, REGION, STREET, HOUSE;

    public static DistrictMatchLevel getMin(List<DistrictMatchLevel> levels) {
        return levels.stream().min(DistrictMatchLevel::compareTo).orElse(null);
    }

    public DistrictMatchLevel getNextHighestLevel() {
        if (ordinal() == 0) {
            return null;
        }
        return values()[ordinal() - 1];
    }
}
