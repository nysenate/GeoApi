package gov.nysenate.sage.scripts.streetfinder.model;

import com.google.common.collect.ImmutableMap;

import java.util.HashMap;

public enum College {
    SKIDMORE("Skidmore College", 12866), LE_MOYNE("(0 )?Lemoyne Col", 13214),
    SUNY_ONEONTA(".*SUNY", 13820), SUNY_BINGHAMTON("", 13902),
    HARTWICK(".*Hartwick Col", 13820), ALFRED_TECH("1 Alfred AG Tech", 14802),
    CORNELL(".*(House|Ujammaa|Alice Cook|Anna Comstock|Baker Tower|Carl Becker|(High|Low) Rise)", 14853),
    VASSAR(".*Vassar College", 12604);

    private final String regex;
    private final int zip;

    College(String partialRegex, int zip) {
        this.regex = "(?i)" + partialRegex + ".*";
        this.zip = zip;
    }

    public static College getCollege(String line, String zip) {
        int intZip = Integer.parseInt(zip);
        for (College college : values()) {
            if (intZip == college.zip && line.matches(college.regex)) {
                return college;
            }
        }
        return null;
    }
}
