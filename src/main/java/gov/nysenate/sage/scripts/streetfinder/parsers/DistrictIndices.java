package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.model.address.StreetFileField;
import gov.nysenate.sage.model.address.StreetFinderAddress;
import gov.nysenate.sage.util.Pair;

import java.util.*;
import java.util.regex.Pattern;

import static gov.nysenate.sage.model.address.StreetFileField.*;
import static gov.nysenate.sage.model.address.StreetFileField.CITY;

public class DistrictIndices {
    // BOE_SCHOOL, "Schl"
    private static final LinkedHashMap<StreetFileField, String> fieldToPattern = new LinkedHashMap<>();
    static {
        fieldToPattern.put(SCHOOL, "Schl");
        fieldToPattern.put(VILLAGE, "Vill");
        fieldToPattern.put(CLEG, "Cleg|CLE|CLEG|Leg|LEG");
        fieldToPattern.put(FIRE, "Fire|FD|FIRE");
        fieldToPattern.put(CITY_COUNCIL, "CC");
        fieldToPattern.put(CITY, "City");
    }
    private static final Pair<Integer> defaultRange = new Pair<>(2, 4);
    private final Map<StreetFileField, Pair<Integer>> fieldToAdjMap = new HashMap<>();
    private final Map<StreetFileField, Pair<Integer>> fieldToRangeMap = new HashMap<>();

    public DistrictIndices(boolean isGreene, String data) {
        fieldToAdjMap.put(CLEG, isGreene ? new Pair<>(2, 6) : defaultRange);
        Pair<Integer> schoolRange = new Pair<>(1, 4);
        fieldToAdjMap.put(SCHOOL, schoolRange);
        // TODO: seems like this is a version of SCHOOL with letters,
        //  except many in the database have numbers, and BOE_TOWN_CODE is digits
//        fieldToAdjMap.put(BOE_SCHOOL, schoolRange);
        fieldToAdjMap.put(VILLAGE, new Pair<>(3, 4));
        fieldToAdjMap.put(FIRE, defaultRange);
        fieldToAdjMap.put(CITY_COUNCIL, defaultRange);
        fieldToAdjMap.put(CITY, defaultRange);

        for (StreetFileField field : fieldToAdjMap.keySet()) {
            var matcher = Pattern.compile(fieldToPattern.get(field)).matcher(data);
            if (matcher.find()) {
                Pair<Integer> adjustments = fieldToAdjMap.get(field);
                Pair<Integer> valueRange = new Pair<>(matcher.start() - adjustments.first(),
                        matcher.start() + adjustments.second());
                fieldToRangeMap.put(field, valueRange);
            }
        }
    }

    public List<String> getPostAsmData(String line) {
        List<String> values = new ArrayList<>();
        for (var field : fieldToPattern.keySet()) {
            Pair<Integer> range = fieldToRangeMap.get(field);
            values.add(substringHelper(line, range.first(), range.second()));
        }
        return values;
    }

    private static String substringHelper(String str, int start, int end) {
        return str.substring(Math.min(start, str.length()), Math.min(end, str.length()));
    }
}
