package gov.nysenate.sage.scripts.streetfinder.scripts.utils;

import gov.nysenate.sage.model.district.DistrictType;

import java.util.*;
import java.util.function.Function;

import static gov.nysenate.sage.model.district.DistrictType.*;

/**
 * Mainly a data structure mapping VoterFileFields to shorts of district numbers.
 * This is condensed into an array of shorts.
 */
public class CompactDistrictMap {
    // Only some DistrictTypes can be stored in a short
    private static final Set<DistrictType> validTypes = Set.of(ASSEMBLY, SENATE, CONGRESSIONAL, SCHOOL, COUNTY, ELECTION, WARD, CLEG, FIRE, CITY_COUNCIL);
    // A few special wards have ward numbers, but data is given in 3-letter codes.
    private static final Map<String, Integer> wardCorrectionMap = Map.of(
            "DEL", 10, "ELL", 20,
            "FIL", 30, "LOV", 40,
            "MAS", 50, "NIA", 60,
            "NOR", 70, "SOU", 80,
            "UNI", 90);
    private static final Intern<Map<DistrictType, Integer>> typeMaps = new Intern<>();
    private static final Intern<short[]> districtArrays = new Intern<>();
    private static final Intern<CompactDistrictMap> maps = new Intern<>();
    private final Map<DistrictType, Integer> typeToIndexMap;
    private final short[] data;

    public static CompactDistrictMap getMap(Set<DistrictType> types, Function<DistrictType, String> getValue) {
       return maps.get(new CompactDistrictMap(types, type -> convert(getValue.apply(type))));
    }

    private CompactDistrictMap(Set<DistrictType> types, Function<DistrictType, Short> getShort) {
        this.typeToIndexMap = getTypeToIndexMap(types);
        var tempData = new short[typeToIndexMap.size()];
        for (DistrictType type : types) {
            tempData[typeToIndexMap.get(type)] = getShort.apply(type);
        }
        this.data = districtArrays.get(tempData);
    }

    private static Map<DistrictType, Integer> getTypeToIndexMap(Set<DistrictType> types) {
        var map = new HashMap<DistrictType, Integer>();
        int i = 0;
        for (DistrictType type : types) {
            if (validTypes.contains(type)) {
                map.put(type, i++);
            }
        }
        return typeMaps.get(map);
    }

    private static short convert(String districtString) {
        // Some data values are preceded by a label (e.g. SE-2) that needs to be skipped.
        districtString = districtString.replaceFirst("$.*-", "");
        try {
            short s = Short.parseShort(districtString);
            if (s < 0) {
                throw new IllegalArgumentException("Districts must be >= 0.");
            }
            return s;
        }
        catch (NumberFormatException ignored) {
            if (districtString.isBlank()) {
                return 0;
            } else if (wardCorrectionMap.containsKey(districtString)) {
                return wardCorrectionMap.get(districtString).shortValue();
            }
            else {
                throw new IllegalArgumentException("District" + districtString + " is neither numeric nor empty!");
            }
        }
    }

    // 0 represents no value.
    public short get(DistrictType field) {
        int index = typeToIndexMap.get(field);
        if (index >= data.length) {
            return 0;
        }
        return data[index];
    }

    public String getString(DistrictType field) {
        short value = get(field);
        if (value == 0) {
            return "";
        }
        return Short.toString(value);
    }

    public CompactDistrictMap withTypes(Set<DistrictType> types) {
        return new CompactDistrictMap(types, this::get);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompactDistrictMap that = (CompactDistrictMap) o;
        return Objects.equals(typeToIndexMap, that.typeToIndexMap) && Arrays.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(typeToIndexMap);
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }


}
