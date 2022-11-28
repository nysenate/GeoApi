package gov.nysenate.sage.model.address;

import gov.nysenate.sage.util.Pair;

import java.util.List;

public class StreetFinderBuilding {
    private static final String DEFAULT = "\\N";
    private Pair<String> low = new Pair<>(DEFAULT, DEFAULT);
    private Pair<String> high = new Pair<>(DEFAULT, DEFAULT);
    private String parity = DEFAULT;

    public void setHigh(String highData) {
        this.high = getPair(highData);
    }

    public void setLow(String lowData) {
        this.low = getPair(lowData);
    }

    public boolean hasParity() {
        return DEFAULT.equals(parity);
    }

    public void setParity(String parity) {
        if (!parity.isEmpty()) {
            this.parity = parity;
        }
    }

    public List<String> getData() {
        return List.of(low.getOne(), low.getTwo(), high.getOne(), high.getTwo(), parity);
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
