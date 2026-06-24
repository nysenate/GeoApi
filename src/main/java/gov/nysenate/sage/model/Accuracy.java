package gov.nysenate.sage.model;

import java.util.List;

public enum Accuracy {
    UNKNOWN, REGION, STREET, HOUSE;

    public static Accuracy getMin(List<Accuracy> levels) {
        return levels.stream().min(Accuracy::compareTo).orElse(null);
    }

    public Accuracy getNextHighestLevel() {
        if (ordinal() == 0) {
            return null;
        }
        return values()[ordinal() - 1];
    }

    public static Accuracy fromString(String quality) {
        try {
            return valueOf(quality.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
