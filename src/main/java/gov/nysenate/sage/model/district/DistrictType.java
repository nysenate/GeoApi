package gov.nysenate.sage.model.district;

import org.apache.commons.lang.WordUtils;

public enum DistrictType {
    SENATE, ASSEMBLY, CONGRESSIONAL, ZIP, COUNTY, TOWN_CITY, SCHOOL, ELECTRIC_UTILITY,
    ELECTION, WARD, CITY_COUNCIL, COUNTY_LEG, VILLAGE, MUNICIPAL_COURT, FIRE;

    public String getDisplayName() {
        if (this == TOWN_CITY) {
            return "Town/City";
        }
        return WordUtils.capitalizeFully(name().replace('_', ' '));
    }

    public boolean coversState() {
        return this != WARD && this != CITY_COUNCIL && this != COUNTY_LEG && this != VILLAGE;
    }
}
