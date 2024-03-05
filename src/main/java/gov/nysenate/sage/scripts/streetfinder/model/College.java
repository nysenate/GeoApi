package gov.nysenate.sage.scripts.streetfinder.model;

// Contains some code to check if a NonStandardAddress is from an enumerated college.
// TODO: utilize this class
public enum College {
    ALFRED_TECH("1 Alfred AG Tech", 14802),
    CORNELL(".*(House|Ujammaa|Alice Cook|Anna Comstock|Baker Tower|Carl Becker|(High|Low) Rise)", 14853),
    HARTWICK(".*Hartwick Col", 13820), LE_MOYNE("(0 )?Lemoyne Col", 13214),
    SKIDMORE("Skidmore College", 12866), SUNY_ALBANY("", 12222),
    SUNY_BINGHAMTON("", 13902), SUNY_ONEONTA(".*SUNY", 13820),
    SYRACUSE("", 13210), VASSAR(".*Vassar College", 12604);


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
