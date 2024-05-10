package gov.nysenate.sage.scripts.streetfinder.scripts.utils;

import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.util.Tuple;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * Utility class to compactly store district information and its source.
 */
public class RangeDistrictData {
    // Always at least one of each of these, but there may be more.
    private CompactDistrictMap[] districtMaps;
    private String[] sourceParsers;
    private long[] ids;

    public RangeDistrictData(CompactDistrictMap districtMap, String sourceParser, long id) {
        this.districtMaps = new CompactDistrictMap[]{districtMap};
        this.sourceParsers = new String[]{sourceParser};
        this.ids = new long[]{id};
    }

    public RangeDistrictData(RangeDistrictData toCopy) {
        this.districtMaps = Arrays.copyOf(toCopy.districtMaps, toCopy.districtMaps.length);
        this.sourceParsers = Arrays.copyOf(toCopy.sourceParsers, toCopy.sourceParsers.length);
        this.ids = Arrays.copyOf(toCopy.ids, toCopy.ids.length);
    }

    public void add(RangeDistrictData toAdd) {
        this.districtMaps = arrayAdd(districtMaps, toAdd.districtMaps);
        this.sourceParsers = arrayAdd(sourceParsers, toAdd.sourceParsers);
        final int originalLength = ids.length;
        this.ids = Arrays.copyOf(ids, ids.length + toAdd.ids.length);
        System.arraycopy(toAdd.ids, 0, ids, originalLength, toAdd.ids.length);
    }

    private static <T> T[] arrayAdd(T[] inputArray, T[] toAdd) {
        final int originalLength = inputArray.length;
        T[] outputArray = Arrays.copyOf(inputArray, inputArray.length + toAdd.length);
        System.arraycopy(toAdd, 0, outputArray, originalLength, toAdd.length);
        return outputArray;
    }

    /**
     * Districts with a value of 0 are ignored entirely.
     * Otherwise, if there is a conflict for a DistrictType, that type is ignored as well.
     * @return the consolidated district map, and a display String for conflicts.
     */
    public Tuple<CompactDistrictMap, String> consolidationResult() {
        final var typeMap = new HashMap<DistrictType, Short>();
        final var conflictTypes = new HashSet<DistrictType>();
        for (DistrictType type : DistrictType.values()) {
            for (CompactDistrictMap districtMap : districtMaps) {
                short districtMapValue = districtMap.get(type);
                if (districtMapValue == 0) {
                    continue;
                }
                short typeMapValue = typeMap.getOrDefault(type, ((short) 0));
                if (typeMapValue == 0) {
                    typeMap.put(type, districtMapValue);
                } else if (typeMapValue != districtMapValue) {
                    typeMap.remove(type);
                    conflictTypes.add(type);
                    break;
                }
            }
        }
        return new Tuple<>(CompactDistrictMap.getMap(typeMap), conflictString(conflictTypes));
    }

    @Nonnull
    private String conflictString(final Set<DistrictType> conflictTypes) {
        if (conflictTypes.isEmpty()) {
            return "";
        }
        var strBuilder = new StringBuilder();
        List<String> line = new ArrayList<>();
        line.add("SOURCE");
        for (DistrictType type : conflictTypes) {
            line.add(type.name());
        }
        line.add("ID");
        strBuilder.append(String.join("\t", line)).append('\n');

        for (int i = 0; i < districtMaps.length; i++) {
            line.clear();
            line.add(sourceParsers[i]);
            for (DistrictType type : conflictTypes) {
                line.add(Short.toString(districtMaps[i].get(type)));
            }
            line.add(Long.toString(ids[i]));
            strBuilder.append(String.join("\t", line)).append('\n');
        }
        return strBuilder.toString();
    }
}
