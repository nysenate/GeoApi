package gov.nysenate.sage.scripts.streetfinder.model;

import gov.nysenate.sage.util.Pair;

import java.util.List;

import static gov.nysenate.sage.scripts.streetfinder.model.StreetFileAddress.DEFAULT;

public class StreetFinderBuilding {

    private Pair<String> low = new Pair<>(DEFAULT, DEFAULT);
    private Pair<String> high = new Pair<>(DEFAULT, DEFAULT);
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
        String first = low.first().equals(DEFAULT) ? "" : low.first();
        String second = low.second().equals(DEFAULT) ? "" : low.second();
        return first + second;
    }

    public List<String> getData() {
        return List.of(low.first(), low.second(), high.first(), high.second(), parity == null ? DEFAULT : parity.name());
    }

    private static Pair<String> getPair(String data) {
        if (data == null || data.isEmpty()) {
            return new Pair<>(DEFAULT, DEFAULT);
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
        if (digits.isEmpty()) {
            digits = new StringBuilder(DEFAULT);
        }
        if (characters.isEmpty()) {
            characters = new StringBuilder(DEFAULT);
        }
        return new Pair<>(digits.toString(), characters.toString());
    }
}
