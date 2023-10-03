package gov.nysenate.sage.scripts.streetfinder.model;

public enum StreetParity {
    ODDS, EVENS, ALL;

    public static StreetParity getParityFromWord(String input) {
        input = input.toUpperCase().trim();
        if (input.matches("O|ODDS?")) {
            return ODDS;
        }
        if (input.matches("E|EVENS?")) {
            return EVENS;
        }
        return ALL;
    }

    public static StreetParity getParityFromRange(Integer lowNum, Integer highNum) {
        try {
            if (lowNum % 2 == highNum % 2) {
                return lowNum % 2 == 0 ? EVENS : ODDS;
            }
        } catch (Exception ignored) {}
        return ALL;
    }
}
