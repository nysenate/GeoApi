package gov.nysenate.sage.scripts.streetfinder.model;

import java.util.Map;

import static gov.nysenate.sage.scripts.streetfinder.model.StreetFileField.*;

public class SchoharieStreetFileAddressRange extends StreetFileAddressRange {
    private static final Map<StreetFileField, String> illegalValues = Map.of(
            TOWN, "Town", ELECTION_CODE, "Dist", CONGRESSIONAL, "Cong", STREET, "street name",
            SENATE, "Sen", ASSEMBLY, "Asm", BOE_SCHOOL, "School", VILLAGE, "Village");

    @Override
    public void put(StreetFileField type, String value) {
        String illegalValue = illegalValues.get(type);
        if (illegalValue == null || !illegalValue.equalsIgnoreCase(value)) {
            super.put(type, value);
        }
    }

    public boolean hasSenateDistrict() {
        return fieldMap.containsKey(SENATE);
    }
}
