package gov.nysenate.sage.scripts.streetfinder.scripts.utils;

import java.util.EnumMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static gov.nysenate.sage.scripts.streetfinder.scripts.utils.VoterFileField.*;
import static gov.nysenate.sage.scripts.streetfinder.scripts.utils.VoterFileLineType.*;

/**
 * Handles a single line of voter file data.
 */
public class VoterFileLineMap extends EnumMap<VoterFileField, String> {
    private static final Pattern BOEIDpattern = Pattern.compile("NY(\\d{18})");
    private final VoterFileLineType type;
    private long id;

    public VoterFileLineMap(String line) {
        super(VoterFileField.class);
        line = line.replaceAll(" {2,}", " ");
        // We'll be making a TSV, so the initial file can't have tabs.
        if (line.indexOf('\t') != -1) {
            this.type = HAS_TABS;
            return;
        }
        String[] parts = line.split("\\s*\",\"\\s*", 0);
        if (parts.length != VoterFileField.values().length) {
            this.type = WRONG_FIELD_LENGTH;
            return;
        }
        for (VoterFileField field : VoterFileField.values()) {
            put(field, parts[field.ordinal()]);
        }
        Matcher matcher = BOEIDpattern.matcher(get(SBOEID));
        if (!matcher.matches()) {
            this.type = BAD_ID;
            return;
        }
        this.id = Long.parseLong(matcher.group(1));
        if (get(RZIP5).isEmpty()) {
            this.type = MISSING_ZIP_5;
            return;
        }
        boolean emptyStandardAddress = standardAddressFields.stream().map(this::get).allMatch(String::isEmpty);
        if (emptyStandardAddress) {
            this.type = get(RADDRNONSTD).isEmpty() ? NO_ADDRESS : NON_STANDARD_ADDRESS;
        }
        else {
            this.type = get(RADDRNONSTD).isEmpty() ? VALID : TWO_ADDRESS_FORMATS;
        }
    }

    @Override
    public String toString() {
        return String.join(",", values()).toUpperCase();
    }

    public VoterFileLineType getType() {
        return type;
    }

    public long getId() {
        return id;
    }
}
