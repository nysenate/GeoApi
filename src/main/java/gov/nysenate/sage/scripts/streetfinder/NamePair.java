package gov.nysenate.sage.scripts.streetfinder;

public class NamePair {
    private final String name;
    private final String data;

    public NamePair(String name, String data) {
        this.name = name;
        this.data = data;
    }

    @Override
    public String toString() {
        return name.toUpperCase() + "," + data;
    }
}
