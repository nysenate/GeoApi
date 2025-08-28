package gov.nysenate.sage.model.district;

public record TownCity(String fullName, String code, String voterfileCode) {
    public String baseName() {
        return getBaseName(fullName);
    }

    public boolean isCity() {
        return fullName.contains("City");
    }

    public static String getBaseName(String fullName) {
        return fullName.replaceFirst("(Town|City) of ", "");
    }

    public static String getFullName(String baseName, boolean isCity) {
        if ("New York".equals(baseName)) {
            return "New York City";
        }
        return (isCity ? "City" : "Town") + " of " + baseName;
    }
}
