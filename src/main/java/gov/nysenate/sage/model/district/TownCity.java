package gov.nysenate.sage.model.district;

import java.util.regex.Pattern;

public class TownCity {
    private static final String newYork = "New York", nyc = newYork + " City";
    private final String baseName, code, voterFileCode;
    private final boolean isCity;
    private final Pattern pattern;

    public TownCity(String fullName, String code, String voterFileCode) {
        this.baseName = getBaseName(fullName);
        this.code = code;
        this.voterFileCode = voterFileCode;
        this.isCity = fullName.contains("City");
        this.pattern = getPattern(isCity, baseName);
    }

    public static String getBaseName(String fullName) {
        if (nyc.equals(fullName)) {
            return newYork;
        }
        return fullName.replaceFirst("(Town|City) of ", "");
    }

    public static String getFullName(String baseName, boolean isCity) {
        if (newYork.equals(baseName)) {
            return nyc;
        }
        return (isCity ? "City" : "Town") + " of " + baseName;
    }

    public String baseName() {
        return baseName;
    }

    public String code() {
        return code;
    }

    public String voterFileCode() {
        return voterFileCode;
    }

    public boolean isTown() {
        return !isCity;
    }

    public Pattern pattern() {
        return pattern;
    }

    private static Pattern getPattern(boolean isCity, String baseName) {
        String patternBase = isCity ?
                "(C |City?( of)? )?%s([ /]City)?" : "(T |Town( of)? )?%s([ /][(]?Town[)]?)?";
        String[] split = baseName.split("[ .]", 2);
        if (split[0].matches("(?i)North|South|East|West")) {
            split[0] = "(" + split[0].charAt(0) + "|" + split[0] + ")";
        }
        else if ("Mount".equalsIgnoreCase(split[0])) {
            split[0] = "(MT|" + split[0] + ")";
        } else if ("Fort".equalsIgnoreCase(split[0])) {
            split[0] = "(FT|" + split[0] + ")";
        }
        return Pattern.compile(patternBase.formatted(String.join("[. ]{0,2}", split)), Pattern.CASE_INSENSITIVE);
    }

    @Override
    public String toString() {
        return "TownCity{" + "baseName=" + baseName + ", code=" + code + '}';
    }
}
