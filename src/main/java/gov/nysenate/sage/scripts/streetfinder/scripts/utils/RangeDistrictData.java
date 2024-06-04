package gov.nysenate.sage.scripts.streetfinder.scripts.utils;

import gov.nysenate.sage.model.district.DistrictType;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Utility class to compactly store district information and its source.
 */
public class RangeDistrictData {
    private CompactDistrictMap[] districtMaps;
    private String[] sourceParsers;
    private long[] ids;

    public RangeDistrictData() {
        this.districtMaps = new CompactDistrictMap[]{};
        this.sourceParsers = new String[]{};
        this.ids = new long[]{};
    }

    public RangeDistrictData(CompactDistrictMap districtMap, String sourceParser, long id) {
        this.districtMaps = new CompactDistrictMap[]{districtMap};
        this.sourceParsers = new String[]{sourceParser};
        this.ids = new long[]{id};
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

    public String[] getSourceNames() {
        return sourceParsers;
    }

    public CompactDistrictMap[] getDistrictMaps() {
        return districtMaps;
    }

    @Nonnull
    public String conflictString(final Set<DistrictType> conflictTypes) {
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
