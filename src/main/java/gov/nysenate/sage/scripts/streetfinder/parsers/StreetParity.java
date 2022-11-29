package gov.nysenate.sage.scripts.streetfinder.parsers;

public enum StreetParity {
    ODDS, EVENS, ALL;

    public static StreetParity getParity(String input) {
        input = input.toUpperCase().trim();
        if (input.matches("O|ODDS?")) {
            return ODDS;
        }
        if (input.matches("E|EVENS?")) {
            return EVENS;
        }
        return ALL;
    }
}
