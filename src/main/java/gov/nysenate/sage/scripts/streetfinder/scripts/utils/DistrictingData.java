package gov.nysenate.sage.scripts.streetfinder.scripts.utils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.scripts.streetfinder.model.StreetFileAddressRange;
import gov.nysenate.sage.util.Tuple;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DistrictingData {
    private final Map<StreetFileAddressRange, Multimap<CompactDistrictMap, CellId>> internalTable;

    public DistrictingData(int expectedRows, int expectedCellsPerRow) {
        this.internalTable = new HashMap<>(expectedRows);
    }

    public void combine(DistrictingData data) {
        data.internalTable.forEach((addr, multimap) -> getMultimap(addr).putAll(multimap));
    }

    public void put(StreetfileLineData data) {
        getMultimap(data.addressRange()).put(data.districts(), data.cellId());
    }

    private Multimap<CompactDistrictMap, CellId> getMultimap(StreetFileAddressRange address) {
        var currMap = internalTable.get(address);
        if (currMap == null) {
            currMap = ArrayListMultimap.create(1, 2);
            internalTable.put(address, currMap);
        }
        return currMap;
    }

    public Set<StreetFileAddressRange> rows() {
        return internalTable.keySet();
    }

    public Multimap<CompactDistrictMap, CellId> getRow(StreetFileAddressRange range) {
        return internalTable.get(range);
    }

    public Tuple<DistrictingData, Map<StreetFileAddressRange, CompactDistrictMap>> consolidate() {
        var conflicts = new DistrictingData((int) (internalTable.size() * .05), 4);
        var consolidatedMap = new HashMap<StreetFileAddressRange, CompactDistrictMap>(rows().size());
        for (StreetFileAddressRange addr : rows()) {
            Multimap<CompactDistrictMap, CellId> currRow = internalTable.get(addr);
            CompactDistrictMap first = currRow.keySet().iterator().next();
            if (currRow.size() > 1) {
                Set<DistrictType> commonTypes = Arrays.stream(DistrictType.values())
                        .filter(type -> currRow.keySet().stream().allMatch(map -> map.get(type) == first.get(type)))
                        .collect(Collectors.toSet());
                Set<DistrictType> conflictingTypes = Arrays.stream(DistrictType.values())
                        .filter(type -> !commonTypes.contains(type))
                        .collect(Collectors.toSet());
                // Zeros are missing values: we don't need them.
                commonTypes.removeIf(type -> first.get(type) == 0);
                consolidatedMap.put(addr, first.withTypes(commonTypes));

                Multimap<CompactDistrictMap, CellId> currConflictMap = ArrayListMultimap.create(currRow.size(), 2);
                currRow.forEach((key, value) -> currConflictMap.put(key.withTypes(conflictingTypes), value));
                conflicts.internalTable.put(addr, currConflictMap);
            }
            else {
                consolidatedMap.put(addr, first);
            }
        }
        return new Tuple<>(conflicts, consolidatedMap);
    }
}
