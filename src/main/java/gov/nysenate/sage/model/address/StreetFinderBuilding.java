package gov.nysenate.sage.model.address;

import gov.nysenate.sage.scripts.streetfinder.parsers.StreetParity;
import gov.nysenate.sage.util.Pair;

import java.util.List;

public class StreetFinderBuilding {
    private static final String DEFAULT = "\\N";
    private Pair<String> low = new Pair<>(DEFAULT, DEFAULT);
    private Pair<String> high = new Pair<>(DEFAULT, DEFAULT);
    private StreetParity parity = null;

    public void setHigh(String highData) {
        this.high = getPair(highData);
    }

    public void setLow(String lowData) {
        this.low = getPair(lowData);
    }

    public boolean hasParity() {
        return parity != null;
    }

    public void setParity(String parity) {
        if (!parity.isEmpty()) {
            this.parity = StreetParity.getParity(parity);
        }
    }

    public List<String> getData() {
        return List.of(low.first(), low.second(), high.first(), high.second(), parity == null ? DEFAULT : parity.name());
    }

    private static Pair<String> getPair(String data) {
        if (data == null) {
            return new Pair<>(DEFAULT, DEFAULT);
        }
        String digits = DEFAULT;
        String characters = DEFAULT;
        for (char c : data.toCharArray()) {
            if (Character.isDigit(c)) {
                digits += c;
            } else if (Character.isLetter(c)) {
                characters += c;
            }
        }
        return new Pair<>(digits, characters);
    }
}
