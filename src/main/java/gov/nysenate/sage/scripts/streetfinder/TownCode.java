package gov.nysenate.sage.scripts.streetfinder;

public class TownCode {

    private String name;
    private String abbrev;

    public TownCode() {}

    public TownCode(String name, String abbrev) {
        this.name = name;
        this.abbrev = abbrev;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbbrev() {
        return abbrev;
    }

    public void setAbbrev(String abbrev) {
        this.abbrev = abbrev;
    }

    public String toString() {
        return name.toUpperCase() + "\t" + abbrev + "\n";
    }
}
