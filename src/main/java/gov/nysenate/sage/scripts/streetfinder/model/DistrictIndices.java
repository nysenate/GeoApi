package gov.nysenate.sage.scripts.streetfinder.model;

import gov.nysenate.sage.scripts.streetfinder.parsers.NTSParser;
import gov.nysenate.sage.util.Pair;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Stores data about which indices to look for data at.
 */
public class DistrictIndices {
    // Matches StreetFileFields to their shortened Strings.
    private static final LinkedHashMap<StreetFileField, String> fieldToPattern = new LinkedHashMap<>();
    static {
//        fieldToPattern.put(SCHOOL, "Schl");
//        fieldToPattern.put(VILLAGE, "Vill");
//        fieldToPattern.put(CLEG, "Cleg|CLE|CLEG|Leg|LEG");
//        fieldToPattern.put(FIRE, "Fire|FD|FIRE");
//        fieldToPattern.put(CITY_COUNCIL, "CC");
//        fieldToPattern.put(CITY, "City");
    }
    private static final Pair<Integer> defaultRange = new Pair<>(2, 4);
    // Data for each field will be at [index - first, index + second].
    private static final Map<StreetFileField, Pair<Integer>> fieldToAdjMap = new HashMap<>();
//            Map.of(SCHOOL, new Pair<>(1, 4), VILLAGE, new Pair<>(3, 4));
    private final Map<StreetFileField, Pair<Integer>> fieldToRangeMap = new HashMap<>();

    public DistrictIndices(boolean isGreene, String data) {
        for (StreetFileField field : fieldToPattern.keySet()) {
            var matcher = Pattern.compile(fieldToPattern.get(field)).matcher(data);
            if (matcher.find()) {
//                Pair<Integer> adjustments = isGreene && field == CLEG ?
//                        new Pair<>(2, 6) : fieldToAdjMap.getOrDefault(field, defaultRange);
//                Pair<Integer> valueRange = new Pair<>(matcher.start() - adjustments.first(),
//                        matcher.start() + adjustments.second());
//                fieldToRangeMap.put(field, valueRange);
            }
        }
    }

    /**
     * Parses out data through stored ranges.
     */
    public List<String> getPostAsmData(String line) {
        List<String> values = new ArrayList<>();
        for (var field : fieldToPattern.keySet()) {
            Pair<Integer> range = fieldToRangeMap.getOrDefault(field, defaultRange);
            values.add(NTSParser.substringHelper(line, range.first(), range.second()));
        }
        return values;
    }
}
