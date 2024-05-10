package gov.nysenate.sage.scripts.streetfinder.scripts.utils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import gov.nysenate.sage.scripts.streetfinder.model.AddressWithoutNum;
import gov.nysenate.sage.scripts.streetfinder.model.BuildingRange;
import gov.nysenate.sage.scripts.streetfinder.model.StreetfileAddressRange;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Stream;

/**
 * Contains data from a streetfile, stored in a compact form. There are conflicts within and between data sources:
 * the same address could map to different sets of districts in different places.
 */
public class DistrictingData {
    private final Map<AddressWithoutNum, Map<BuildingRange, RangeDistrictData>> internalTable;

    public DistrictingData(int expectedRows) {
        this.internalTable = new HashMap<>(expectedRows);
    }

    public void put(StreetfileLineData data) {
        put(data.addressWithoutNum().intern(), data.range().intern(), data.cell());
    }

    /**
     * Consolidates the final result, and prints conflicts between ranges.
     * @param conflictFilePath for printing conflicts.
     * @return Unconnected ranges mapped to one set of districts.
     */
    public Map<StreetfileAddressRange, CompactDistrictMap> consolidate(Path conflictFilePath) throws IOException {
        var consolidatedMap = new HashMap<StreetfileAddressRange, CompactDistrictMap>();
        Table<AddressWithoutNum, Integer, String> conflictTable = HashBasedTable.create();

        for (AddressWithoutNum row : internalTable.keySet()) {
            final var conflictMap = new HashMap<Integer, String>();
            Map<BuildingRange, CompactDistrictMap> consolidatedRangeMap =
                    consolidateCells(internalTable.get(row), conflictMap);
            for (var entry : consolidatedRangeMap.entrySet()) {
                consolidatedMap.put(new StreetfileAddressRange(entry.getKey(), row), entry.getValue());
            }
        }

        var strBuilder = new StringBuilder();
        for (AddressWithoutNum row : conflictTable.rowKeySet()) {
            strBuilder.append(row).append('\n');
            for (var entry : conflictTable.row(row).entrySet()) {
                strBuilder.append(entry.getKey()).append('\t').append(entry.getValue());
            }
        }
        Files.writeString(conflictFilePath, strBuilder.toString(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        return consolidatedMap;
    }

    public DistrictingData removeInvalidAddresses(final Map<AddressWithoutNum, AddressWithoutNum> correctionMap) {
        var invalidData = new DistrictingData(internalTable.size() - correctionMap.size());

        Queue<AddressWithoutNum> uncorrectedAwns = new LinkedList<>(internalTable.keySet());
        while (!uncorrectedAwns.isEmpty()) {
            AddressWithoutNum currAwn = uncorrectedAwns.remove();
            Map<BuildingRange, RangeDistrictData> rowValue = internalTable.remove(currAwn);
            AddressWithoutNum finalCurrAwn = correctionMap.getOrDefault(currAwn, currAwn);
            DistrictingData tableToUpdate = correctionMap.containsKey(currAwn) ? this : invalidData;
            rowValue.forEach((key, value) -> tableToUpdate.put(finalCurrAwn, key, value));
        }
        return invalidData;
    }

    public Map<AddressWithoutNum, Queue<Integer>> rowToNumMap() {
        var map = new HashMap<AddressWithoutNum, Queue<Integer>>();
        for (AddressWithoutNum rowId : internalTable.keySet()) {
            List<Integer> tempList = internalTable.get(rowId).keySet().stream()
                    .flatMap(range -> Stream.of(range.low(), range.high()).distinct()).toList();
            map.put(rowId, new LinkedList<>(tempList));
        }
        return map;
    }

    private void put(AddressWithoutNum row, BuildingRange col, RangeDistrictData cell) {
        Map<BuildingRange, RangeDistrictData> columnData = internalTable.get(row);
        if (columnData == null) {
            columnData = new CompactMap<>();
            internalTable.put(row, columnData);
        }
        RangeDistrictData currCell = columnData.get(col);
        if (currCell == null) {
            columnData.put(col, cell);
        }
        else {
            currCell.add(cell);
        }
    }

    /**
     * Consolidates DistrictCell data and stores conflicts.
     * @param conflicts stores pretty print Strings about conflicts.
     */
    private static Map<BuildingRange, CompactDistrictMap> consolidateCells(
            Map<BuildingRange, RangeDistrictData> input, final Map<Integer, String> conflicts) {
        // The algorithm becomes much easier when just dealing with numbers.
        var bldgNumToCells = new HashMap<Integer, RangeDistrictData>();
        input.forEach((key, value) -> {
            for (int bldgNum : key.allInRange()) {
                RangeDistrictData currCell = bldgNumToCells.get(bldgNum);
                if (currCell == null) {
                    currCell = new RangeDistrictData(value);
                }
                else {
                    currCell.add(value);
                }
                bldgNumToCells.put(bldgNum, currCell);
            }
        });
        // Collect the building numbers with common districts.
        Multimap<CompactDistrictMap, Integer> districtsToBldgNums = ArrayListMultimap.create();
        for (var entry : bldgNumToCells.entrySet()) {
            var consolidationResult = entry.getValue().consolidationResult();
            if (!consolidationResult.second().isEmpty()) {
                conflicts.put(entry.getKey(), consolidationResult.second());
            }
            districtsToBldgNums.put(consolidationResult.first(), entry.getKey());
        }
        // Combine ranges of building numbers.
        var rangeToDistrictMap = new HashMap<BuildingRange, CompactDistrictMap>();
        for (var entry : districtsToBldgNums.asMap().entrySet()) {
            BuildingRange.combineRanges(entry.getValue())
                    .forEach(range -> rangeToDistrictMap.put(range, entry.getKey()));
        }
        return rangeToDistrictMap;
    }
}
