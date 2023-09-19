package gov.nysenate.sage.scripts.streetfinder.scripts.utils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Mainly a data structure mapping VoterFileFields to Shorts of district numbers.
 * However, it contains memory saving logic: since there are only several thousand possible sets of districts,
 * we do not need to create a new VoterDistrictMap for every single voter file entry.
 */
public class VoterDistrictMap {
    public static final Set<VoterFileField> districtFields = Arrays.stream(VoterFileField.values())
            .filter(field -> field.getType() == VoterFileFieldType.DISTRICT && field.isStreetfileData())
            .collect(Collectors.toSet());
    // A few special wards have ward numbers, but data is given in 3-letter codes.
    private static final Map<String, Integer> wardCorrectionMap = Map.of(
            "DEL", 10, "ELL", 20,
            "FIL", 30, "LOV", 40,
            "MAS", 50, "NIA", 60,
            "NOR", 70, "SOU", 80,
            "UNI", 90);
    // We need to return previously-created objects to save memory.
    private static final Map<VoterDistrictMap, VoterDistrictMap> interned = new HashMap<>();

    private final EnumMap<VoterFileField, Short> internalMap = new EnumMap<>(VoterFileField.class);

    /**
     * Constructor is private to force memory-saving.
     */
    private VoterDistrictMap(VoterFileLineMap lineMap) {
        for (VoterFileField field : districtFields) {
            String districtString = lineMap.get(field);
            Short s;
            try {
                s = Short.parseShort(districtString);
                if (s < 0) {
                    throw new IllegalArgumentException("Districts must be >= 0.");
                }
                // 0 is regularly used to represent having no district.
                if (s == 0) {
                    s = null;
                }
            }
            catch (NumberFormatException ignored) {
                if (districtString.isBlank()) {
                    s = null;
                } else if (wardCorrectionMap.containsKey(districtString)) {
                    s = wardCorrectionMap.get(districtString).shortValue();
                }
                else {
                    throw new IllegalArgumentException("District" + districtString + " is neither numeric nor empty!");
                }
            }
            internalMap.put(field, s);
        }
    }

    /**
     * Not actually needed, just cool to know.
     */
    public static int internedSize() {
        return interned.size();
    }

    public static VoterDistrictMap getDistrictMap(VoterFileLineMap lineMap) {
        var currMap = new VoterDistrictMap(lineMap);
        if (!interned.containsKey(currMap)) {
            interned.put(currMap, currMap);
        }
        return interned.get(currMap);
    }

    public Short get(VoterFileField field) {
        return internalMap.get(field);
    }

    public String getString(VoterFileField field) {
        Short value = internalMap.get(field);
        if (value == null) {
            return "";
        }
        return value.toString();
    }

    public String stringValues() {
        return internalMap.keySet().stream().map(this::getString)
                .collect(Collectors.joining("\t"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VoterDistrictMap that = (VoterDistrictMap) o;
        return internalMap.equals(that.internalMap);
    }

    @Override
    public int hashCode() {
        return internalMap.hashCode();
    }
}
