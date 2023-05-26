package gov.nysenate.sage.scripts.streetfinder.scripts.utils;

import java.util.ArrayList;
import java.util.TreeMap;

import static gov.nysenate.sage.scripts.streetfinder.scripts.utils.VoterFileField.*;
import static gov.nysenate.sage.scripts.streetfinder.scripts.utils.VoterFileLineType.*;

/**
 * Handles a single line of voter file data.
 */
public class VoterFileLineMap extends TreeMap<VoterFileField, String> {
    private final VoterFileLineType type;

    public VoterFileLineMap(String line) {
        line = line.replaceAll(" {2,}", " ");
        // We'll be making a TSV, so the initial file can't have tabs.
        if (line.indexOf('\t') != -1) {
            this.type = HAS_TABS;
            return;
        }
        String[] parts = line.split("\\s*\",\"\\s*", -1);
        parts[0] = parts[0].replaceFirst("\"", "");
        parts[parts.length - 1] = parts[0].replaceFirst("\"", "");
        if (parts.length != VoterFileField.values().length) {
            this.type = WRONG_FIELD_LENGTH;
            return;
        }
        for (VoterFileField field : VoterFileField.values()) {
            put(field, parts[field.ordinal()]);
        }
        if (get(RZIP5).isEmpty()) {
            this.type = MISSING_ZIP_5;
            return;
        }
        var streetParts = subMap(RADDNUMBER, RAPARTMENT);
        boolean hasStandardAddress = !streetParts.values().stream().allMatch(String::isEmpty);
        if (!get(RADDRNONSTD).isEmpty()) {
            this.type = hasStandardAddress ? TWO_ADDRESS_FORMATS : NON_STANDARD_ADDRESS;
        }
        else if (!hasStandardAddress) {
            this.type = NO_ADDRESS;
        }
        else {
            this.type = VALID;
        }
    }

    @Override
    public String toString() {
        return String.join("\t", values()).toUpperCase();
    }

    public String getAddress() {
        var addressList = new ArrayList<String>();
        for (VoterFileField field : streetFileFields) {
            if (field.getType() == VoterFileFieldType.ADDRESS) {
                addressList.add(get(field));
            }
        }
        return String.join("\t", addressList);
    }

    public String getDistricts() {
        var districtList = new ArrayList<String>();
        for (VoterFileField field : streetFileFields) {
            if (field.getType() == VoterFileFieldType.DISTRICT) {
                districtList.add(get(field));
            }
        }
        return String.join("\t", districtList);
    }

    public VoterFileLineType getType() {
        return type;
    }
}
