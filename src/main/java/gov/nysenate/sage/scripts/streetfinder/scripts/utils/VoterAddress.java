package gov.nysenate.sage.scripts.streetfinder.scripts.utils;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

/**
 * Mainly a data structure mapping VoterFileFields to Strings of address data.
 * However, it contains memory saving logic: since we know the number of address fields,
 * we don't need to waste space allocating a full Map, and can just use an array instead.
 */
public class VoterAddress {
    // Maps fields to their index in the internal array.
    private static final Map<VoterFileField, Integer> indexMap = new EnumMap<>(VoterFileField.class);
    static {
        int index = 0;
        for (VoterFileField field : VoterFileField.values()) {
            if (field.getType() == VoterFileFieldType.ADDRESS && field.isStreetfileData()) {
                indexMap.put(field, index++);
            }
        }
    }

    private final String[] internalArray = new String[indexMap.size()];

    public VoterAddress(VoterFileLineMap lineMap) {
        for (var entry : lineMap.entrySet()) {
            VoterFileField field = entry.getKey();
            if (indexMap.containsKey(field)) {
                internalArray[indexMap.get(field)] = entry.getValue().intern();
            }
        }
    }

    public String get(VoterFileField field) {
        return internalArray[indexMap.get(field)];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VoterAddress that = (VoterAddress) o;
        return Arrays.equals(internalArray, that.internalArray);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(internalArray);
    }

    public String stringValues() {
        return String.join("\t", internalArray);
    }
}
