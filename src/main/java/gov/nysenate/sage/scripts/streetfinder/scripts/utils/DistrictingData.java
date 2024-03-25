package gov.nysenate.sage.scripts.streetfinder.scripts.utils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.scripts.streetfinder.model.StreetfileAddressRange;
import gov.nysenate.sage.util.Tuple;

import java.util.*;
import java.util.stream.Collectors;

public class DistrictingData {
    private final Map<StreetfileAddressRange, Multimap<CompactDistrictMap, CellId>> internalTable;

    public DistrictingData(int expectedRows, int expectedCellsPerRow) {
        this.internalTable = new HashMap<>(expectedRows);
    }

    public void copyFromAndClear(DistrictingData toCopy) {
        Queue<StreetfileAddressRange> rangeQueue = new LinkedList<>(toCopy.rows());
        while (!rangeQueue.isEmpty()) {
            getMultimap(rangeQueue.peek()).putAll(toCopy.getRow(rangeQueue.peek()));
            toCopy.remove(rangeQueue.poll());
        }
    }

    public void put(StreetfileLineData data) {
        getMultimap(data.addressRange()).put(data.districts(), data.cellId());
    }

    public Multimap<CompactDistrictMap, CellId> remove(StreetfileAddressRange range) {
        return internalTable.remove(range);
    }

    private Multimap<CompactDistrictMap, CellId> getMultimap(StreetfileAddressRange address) {
        var currMap = internalTable.get(address);
        if (currMap == null) {
            currMap = ArrayListMultimap.create(1, 2);
            internalTable.put(address, currMap);
        }
        return currMap;
    }

    public Set<StreetfileAddressRange> rows() {
        return internalTable.keySet();
    }

    public Multimap<CompactDistrictMap, CellId> getRow(StreetfileAddressRange range) {
        return internalTable.get(range);
    }

    public Tuple<DistrictingData, Map<StreetfileAddressRange, CompactDistrictMap>> consolidate() {
        var conflicts = new DistrictingData((int) (internalTable.size() * .05), 4);
        var consolidatedMap = new HashMap<StreetfileAddressRange, CompactDistrictMap>(rows().size());
        for (StreetfileAddressRange addr : rows()) {
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
