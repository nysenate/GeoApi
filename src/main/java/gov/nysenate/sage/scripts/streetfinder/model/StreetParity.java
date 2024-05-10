package gov.nysenate.sage.scripts.streetfinder.model;

public enum StreetParity {
    ODDS, EVENS, ALL;

    public boolean matches(int num) {
        if (this == ALL) {
            return true;
        }
        final int remainder = this == EVENS ? 0 : 1;
        return num%2 == remainder;
    }

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

    public static StreetParity getParityFromRange(int lowNum, int highNum) {
        if (lowNum % 2 == highNum % 2) {
            return lowNum % 2 == 0 ? EVENS : ODDS;
        }
        return ALL;
    }
}
