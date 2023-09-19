package gov.nysenate.sage.scripts.streetfinder.model;

import gov.nysenate.sage.util.Pair;

import java.util.List;

import static gov.nysenate.sage.scripts.streetfinder.model.StreetFileAddress.DEFAULT;

public class StreetFinderBuilding {
    private static final Pair<String> defaultPair = new Pair<>("0", "");
    // Pairs are needed because addresses like "10A" are seperated into "10" and "A"
    private Pair<String> low = defaultPair;
    private Pair<String> high = defaultPair;
    private StreetParity parity = null;

    public void setHigh(String highData) {
        this.high = getPair(highData);
    }

    public void setLow(String lowData) {
        this.low = getPair(lowData);
    }

    public void setParity(String parity) {
        if (!parity.isEmpty()) {
            this.parity = StreetParity.getParity(parity);
        }
    }

    public String getLowString() {
        return low.first() + low.second();
    }

    public List<String> getData() {
        return List.of(low.first(), low.second(), high.first(), high.second(), parity == null ? DEFAULT : parity.name());
    }

    @Override
    public String toString() {
        return low.first() + low.second() + "-" + high.first() + high.second() + ", " + parity;
    }

    private static Pair<String> getPair(String data) {
        if (data == null) {
            return defaultPair;
        }
        StringBuilder digits = new StringBuilder();
        StringBuilder characters = new StringBuilder();
        for (char c : data.toCharArray()) {
            if (Character.isDigit(c)) {
                digits.append(c);
            } else if (Character.isLetter(c)) {
                characters.append(c);
            }
        }
        return new Pair<>(digits.isEmpty() ? "0" : digits.toString(), characters.toString());
    }
}
