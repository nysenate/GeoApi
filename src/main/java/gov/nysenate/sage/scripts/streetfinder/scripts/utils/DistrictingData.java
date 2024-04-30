package gov.nysenate.sage.scripts.streetfinder.scripts.utils;

import com.google.common.collect.*;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.scripts.streetfinder.model.AddressWithoutNum;
import gov.nysenate.sage.scripts.streetfinder.model.BuildingRange;
import gov.nysenate.sage.scripts.streetfinder.model.StreetfileAddressRange;
import gov.nysenate.sage.util.Tuple;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Contains data from a streetfile, stored in a compact form. There are conflicts within and between data sources:
 * the same address could map to different sets of districts in different places.
 */
public class DistrictingData {
    // We need this complicated data structure so we can combine ranges later.
    private final Table<AddressWithoutNum, BuildingRange, Multimap<CompactDistrictMap, CellId>> internalTable;

    public DistrictingData(int expectedRows, int expectedCellsPerRow) {
        this.internalTable = HashBasedTable.create(expectedRows, expectedCellsPerRow);
    }

    public void copyFromAndClear(DistrictingData toCopy) {
        for (var rowCol : toCopy.rowColSet()) {
            Multimap<CompactDistrictMap, CellId> cell = toCopy.internalTable.remove(rowCol.first(), rowCol.second());
            if (cell != null) {
                getMultimap(rowCol.first(), rowCol.second()).putAll(cell);
            }
        }
    }

    public void put(StreetfileLineData data) {
        getMultimap(data.addressWithoutNum(), data.range()).put(data.districts(), data.cellId());
    }

    /**
     * Consolidates the final result, and prints conflicts between ranges.
     * @param conflictFilePath for printing conflicts.
     * @return Unconnected ranges mapped to one set of districts.
     */
    public Map<StreetfileAddressRange, CompactDistrictMap> consolidate(Path conflictFilePath) throws IOException {
        Table<AddressWithoutNum, BuildingRange, CompactDistrictMap> singleTable =
                consolidateToSingleTable(conflictFilePath);
        var consolidatedMap = new HashMap<StreetfileAddressRange, CompactDistrictMap>();
        for (AddressWithoutNum row : singleTable.rowKeySet()) {
            Multimap<CompactDistrictMap, BuildingRange> districtToRangeMap = ArrayListMultimap.create();
            for (var entry : singleTable.row(row).entrySet()) {
                districtToRangeMap.put(entry.getValue(), entry.getKey());
            }
            for (var entry : districtToRangeMap.asMap().entrySet()) {
                Set<BuildingRange> combinedRanges = BuildingRange.combineRanges(entry.getValue());
                districtToRangeMap.replaceValues(entry.getKey(), combinedRanges);
            }
            // Conflicts can only occur with the same AddressWithoutNum
            var conflictRanges = new HashSet<BuildingRange>();
            for (BuildingRange bldgRange1 : districtToRangeMap.values()) {
                for (BuildingRange bldgRange2 : districtToRangeMap.values()) {
                    if (!bldgRange1.equals(bldgRange2) && bldgRange1.overlaps(bldgRange2)) {
                        conflictRanges.add(bldgRange1);
                        conflictRanges.add(bldgRange2);
                    }
                }
            }
            // Simple printing and removing on range conflicts, e.g. [1, 4, ALL] and [2, 4, E] mapping to different districts.
            for (BuildingRange conflictRange : conflictRanges) {
                Files.writeString(conflictFilePath, "Conflict in: " + conflictRange);
            }
            districtToRangeMap.values().removeIf(conflictRanges::contains);
            districtToRangeMap.forEach((districtMap, range) -> consolidatedMap.put(new StreetfileAddressRange(range, row), districtMap));
        }
        return consolidatedMap;
    }

    public DistrictingData removeInvalidAddresses(Map<AddressWithoutNum, AddressWithoutNum> correctionMap) {
        Set<AddressWithoutNum> currRows = internalTable.rowKeySet();
        Sets.SetView<AddressWithoutNum> toRemove = Sets.difference(currRows, correctionMap.keySet());
        var invalidData = new DistrictingData(toRemove.size(), 2);

        for (AddressWithoutNum oldAwn : new HashSet<>(currRows)) {
            var currRow = internalTable.row(oldAwn);
            for (BuildingRange bldgRange : new HashSet<>(currRow.keySet())) {
                var cellData = internalTable.remove(oldAwn, bldgRange);
                if (correctionMap.containsKey(oldAwn)) {
                    internalTable.put(correctionMap.get(oldAwn), bldgRange, cellData);
                }
                else {
                    invalidData.getMultimap(oldAwn, bldgRange).putAll(cellData);
                }
            }
        }
        return invalidData;
    }

    @Nonnull
    private Multimap<CompactDistrictMap, CellId> getMultimap(AddressWithoutNum addressWithoutNum, BuildingRange range) {
        var currMap = internalTable.get(addressWithoutNum, range);
        if (currMap == null) {
            currMap = ArrayListMultimap.create(1, 2);
            internalTable.put(addressWithoutNum, range, currMap);
        }
        return currMap;
    }

    /**
     * Helper method for iteration over the table.
     */
    private Set<Tuple<AddressWithoutNum, BuildingRange>> rowColSet() {
        var set = new HashSet<Tuple<AddressWithoutNum, BuildingRange>>();
        for (AddressWithoutNum row : internalTable.rowKeySet()) {
            for (BuildingRange col : internalTable.row(row).keySet()) {
                set.add(new Tuple<>(row, col));
            }
        }
        return set;
    }

    /**
     * Helper method for iteration over the table.
     */
    public Map<AddressWithoutNum, List<Integer>> rowToNumMap() {
        var map = new HashMap<AddressWithoutNum, List<Integer>>();
        for (AddressWithoutNum row : internalTable.rowKeySet()) {
            map.put(row, internalTable.row(row).keySet().stream()
                    .flatMap(range -> Stream.of(range.low(), range.high()).distinct()).toList());
        }
        return map;
    }

    /**
     * Consolidates the Multimap<CompactDistrictMap, CellId> of each cell into a single CompactDistrictMap.
     * Also, prints data about conflicts between different sources.
     */
    private Table<AddressWithoutNum, BuildingRange, CompactDistrictMap> consolidateToSingleTable(Path conflictFilePath) throws IOException {
        var conflicts = new DistrictingData((int) (internalTable.size() * .05), 4);
        Table<AddressWithoutNum, BuildingRange, CompactDistrictMap> consolidatedTable =
                HashBasedTable.create(internalTable.rowKeySet().size(), 4);
        for (var rowCol : rowColSet()) {
            Multimap<CompactDistrictMap, CellId> currCell = internalTable.remove(rowCol.first(), rowCol.second());
            if (currCell == null) {
                continue;
            }
            CompactDistrictMap first = currCell.keySet().iterator().next();
            Set<DistrictType> conflictingTypes = Arrays.stream(DistrictType.values())
                    .filter(type -> currCell.keySet().stream().anyMatch(map -> map.get(type) != first.get(type)))
                    .collect(Collectors.toSet());
            if (!conflictingTypes.isEmpty()) {
                Multimap<CompactDistrictMap, CellId> currConflictCell = ArrayListMultimap.create(currCell.size(), 2);
                currCell.forEach((key, value) -> currConflictCell.put(key.withTypes(conflictingTypes), value));
                conflicts.internalTable.put(rowCol.first(), rowCol.second(), currConflictCell);
            }
            consolidatedTable.put(rowCol.first(), rowCol.second(), consolidateMaps(currCell.keySet()));
        }
        Files.writeString(conflictFilePath, conflicts.internalTable.toString());
        return consolidatedTable;
    }

    @Override
    public String toString() {
        var strBuilder = new StringBuilder();
        for (AddressWithoutNum awn : internalTable.rowKeySet()) {
            strBuilder.append(awn).append('\n');
            var currRow = internalTable.row(awn);
            for (BuildingRange bldgRange : currRow.keySet()) {
                strBuilder.append('\t').append(bldgRange).append('\n');
                var currCellMap = currRow.get(bldgRange).asMap();
                for (CompactDistrictMap districtMap : currCellMap.keySet()) {
                    strBuilder.append('\t').append('\t').append(districtMap).append(": ");
                    List<String> cellIdStrings = currCellMap.get(districtMap).stream().map(Record::toString).toList();
                    strBuilder.append(String.join(", ", cellIdStrings)).append('\n');
                }
            }
            strBuilder.append('\n');
        }
        return strBuilder.toString();
    }

    /**
     * Districts with a value of 0 are ignored entirely.
     * Otherwise, if there is a conflict for a DistrictType, that type is ignored as well.
     * @param districtMaps to consolidate
     * @return a single CompactDistrictMap
     */
    private static CompactDistrictMap consolidateMaps(Collection<CompactDistrictMap> districtMaps) {
        var typeMap = new HashMap<DistrictType, Short>();
        for (DistrictType type : DistrictType.values()) {
            for (CompactDistrictMap districtMap : districtMaps) {
                short typeMapValue = typeMap.getOrDefault(type, ((short) 0));
                short districtMapValue = districtMap.get(type);
                if (typeMapValue == 0 && districtMapValue != 0) {
                    typeMap.put(type, districtMapValue);
                } else if (typeMapValue != districtMapValue) {
                    typeMap.remove(type);
                    break;
                }
            }
        }
        return CompactDistrictMap.getMap(typeMap);
    }
}
