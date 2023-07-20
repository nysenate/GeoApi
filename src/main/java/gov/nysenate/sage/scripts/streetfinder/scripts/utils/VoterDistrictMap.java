package gov.nysenate.sage.scripts.streetfinder.scripts.utils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Mainly a data structure mapping VoterFileFields to Strings of district data.
 * However, it contains memory saving logic: since there are only several thousand possible sets of districts,
 * we do not need to create a new VoterDistrictMap for every single voter file entry.
 */
public class VoterDistrictMap {
    // Maps hash codes to instances of this class.
    // This may seem redundant given the existence of Set, but we need to return previously-created objects.
    private static final Map<Integer, VoterDistrictMap> interned = new HashMap<>(5000);
    public static final Set<VoterFileField> districtFields = Arrays.stream(VoterFileField.values())
            .filter(field -> field.getType() == VoterFileFieldType.DISTRICT && field.isStreetfileData())
            .collect(Collectors.toSet());

    private final EnumMap<VoterFileField, String> internalMap = new EnumMap<>(VoterFileField.class);
    private final int hashCode;

    /**
     * Constructor is private to force memory-saving.
     */
    private VoterDistrictMap(VoterFileLineMap lineMap) {
        for (var entry : lineMap.entrySet()) {
            VoterFileField field = entry.getKey();
            if (districtFields.contains(field)) {
                internalMap.put(field, entry.getValue().intern());
            }
        }
        this.hashCode = internalMap.hashCode();
    }

    /**
     * Not actually needed, just cool to know.
     */
    public static int size() {
        return interned.size();
    }

    public static VoterDistrictMap getDistrictMap(VoterFileLineMap lineMap) {
        var currMap = new VoterDistrictMap(lineMap);
        if (interned.containsKey(currMap.hashCode)) {
            return interned.get(currMap.hashCode);
        }
        interned.put(currMap.hashCode, currMap);
        return currMap;
    }

    public String get(VoterFileField field) {
        return internalMap.get(field);
    }

    @Override
    public String toString() {
        return String.join("\t", internalMap.values());
    }
}
