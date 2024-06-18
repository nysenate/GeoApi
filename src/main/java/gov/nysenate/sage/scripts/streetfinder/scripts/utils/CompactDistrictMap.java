package gov.nysenate.sage.scripts.streetfinder.scripts.utils;

import com.google.common.collect.ImmutableMap;
import gov.nysenate.sage.model.district.DistrictType;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static gov.nysenate.sage.model.district.DistrictType.VILLAGE;
import static gov.nysenate.sage.model.district.DistrictType.ZIP;

/**
 * A data structure mapping DistrictTypes to shorts of district numbers.
 * Due to lots of repetition, interning is enforced.
 */
public class CompactDistrictMap {
    // Some DistrictTypes can't be stored in a short
    private static final Set<DistrictType> allTypes = Set.of(DistrictType.values());
    // A few special wards have ward numbers, but data is given in 3-letter codes.
    private static final Map<String, Integer> wardCorrectionMap = Map.of(
            "DEL", 10, "ELL", 20,
            "FIL", 30, "LOV", 40,
            "MAS", 50, "NIA", 60,
            "NOR", 70, "SOU", 80,
            "UNI", 90);
    private static final Intern<CompactDistrictMap> maps = new Intern<>();
    private static final Map<DistrictType, Integer> typeToIndexMap;
    static {
        var tempMap = new HashMap<DistrictType, Integer>();
        int i = 0;
        for (DistrictType type : DistrictType.values()) {
            if (type != VILLAGE && type != ZIP) {
                tempMap.put(type, i++);
            }
        }
        typeToIndexMap = ImmutableMap.copyOf(tempMap);
    }
    private final short[] data;

    public static CompactDistrictMap getMap(Function<DistrictType, String> getValue) {
       return maps.get(new CompactDistrictMap(allTypes, type -> convert(getValue.apply(type))));
    }

    public static CompactDistrictMap getMap(Map<DistrictType, Short> typeMap) {
        return maps.get(new CompactDistrictMap(typeMap.keySet(), typeMap::get));
    }

    private CompactDistrictMap(Set<DistrictType> types, Function<DistrictType, Short> getShort) {
        this.data = new short[typeToIndexMap.size()];
        for (DistrictType type : types) {
            if (typeToIndexMap.containsKey(type)) {
                var temp = getShort.apply(type);
                data[typeToIndexMap.get(type)] = temp;
            }
        }
    }

    private static short convert(@Nonnull String districtString) {
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
            if (wardCorrectionMap.containsKey(districtString)) {
                return wardCorrectionMap.get(districtString).shortValue();
            }
            if (!districtString.isBlank()) {
                System.err.println("District " + districtString + " is not a short!");
            }
            return 0;
        }
    }

    // 0 represents no value.
    public short get(DistrictType field) {
        Integer index = typeToIndexMap.get(field);
        if (index == null || index >= data.length) {
            return 0;
        }
        return data[index];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompactDistrictMap that = (CompactDistrictMap) o;
        return Arrays.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

    @Override
    public String toString() {
        var strArray = new String[data.length];
        for (int i = 0; i < data.length; i++) {
            strArray[i] = String.valueOf(data[i]);
        }
        return String.join(", ", strArray);
    }
}
