package gov.nysenate.sage.model.district;

import java.net.URI;

// We don't mind using names directly here: counties have not been changed for over 100 years.
public record County(int code, String name, URI link) {
    public County(int code, String name, String link) {
        this(code, name, URI.create(link));
    }

    public boolean inNYC() {
        return switch (name) {
            case "Bronx", "Kings", "New York", "Queens", "Richmond" -> true;
            default -> false;
        };
    }

    public String streetfileName() {
        return switch (name) {
            case "Kings" -> "Brooklyn";
            case "New York" -> "Manhattan";
            case "Staten Island" -> "Richmond";
            default -> name;
        };
    }
}
