package gov.nysenate.sage.scripts.streetfinder.parsers;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

// TODO: there are two classes with this name
public enum County {
    // TODO: Staten needs fixing
    BRONX, BROOKLYN, MANHATTAN, QUEENS, STATEN_ISLAND, ALLEGANY, COLUMBIA, SARATOGA, ERIE, ESSEX,
    NASSAU, WESTCHESTER, SUFFOLK, WYOMING, SCHOHARIE, DEFAULT_COUNTY;

    public static County getCounty(String filename) {
        String finalFilename = filename.toUpperCase();
        var matches = Arrays.stream(values()).filter(county ->
                        finalFilename.contains(county.toString())).collect(Collectors.toList());
        if (matches.isEmpty()) {
            return DEFAULT_COUNTY;
        }
        if (matches.size() > 1) {
            throw new IllegalArgumentException("Filename %s may match multiple counties.".formatted(filename));
        }
        return matches.get(0);
    }

    @Override
    public String toString() {
        return StringUtils.capitalize(name().replaceAll("_", " ").toLowerCase());
    }
}
