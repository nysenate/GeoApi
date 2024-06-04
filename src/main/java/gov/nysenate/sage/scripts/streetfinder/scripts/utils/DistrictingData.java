package gov.nysenate.sage.scripts.streetfinder.scripts.utils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import gov.nysenate.sage.scripts.streetfinder.model.*;
import gov.nysenate.sage.util.Tuple;

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
    private static final int NUM_STREETS = 100000;
    private final Map<AddressWithoutNum, Map<BuildingRange, RangeDistrictData>> internalTable =
            new HashMap<>(NUM_STREETS);
    private final Map<String, StreetfileType> sourceToTypeMap = new HashMap<>();
    private final ResolveConflictConfiguration config;

    public DistrictingData(ResolveConflictConfiguration config) {
        this.config = config;
    }

    public void putSource(String sourceName, StreetfileType type) {
        sourceToTypeMap.put(sourceName, type);
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
        Table<AddressWithoutNum, BuildingRange, String> conflictTable = HashBasedTable.create();

        for (AddressWithoutNum row : internalTable.keySet()) {
            var data = consolidateCells(internalTable.get(row));
            for (var entry : data.first().entrySet()) {
                consolidatedMap.put(new StreetfileAddressRange(entry.getKey(), row), entry.getValue());
            }
            data.second().forEach((num, str) -> conflictTable.put(row, num, str));
        }

        var strBuilder = new StringBuilder();
        for (AddressWithoutNum row : conflictTable.rowKeySet()) {
            strBuilder.append(row).append('\n');
            for (var entry : conflictTable.row(row).entrySet()) {
                strBuilder.append(entry.getKey()).append('\n').append(entry.getValue());
            }
            strBuilder.append('\n');
        }
        Files.writeString(conflictFilePath, strBuilder.toString(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        return consolidatedMap;
    }

    public Multimap<String, String> removeInvalidAddresses(final Map<AddressWithoutNum, AddressWithoutNum> correctionMap) {
        Multimap<String, String> invalidData = ArrayListMultimap.create();

        Queue<AddressWithoutNum> uncorrectedAwns = new LinkedList<>(internalTable.keySet());
        while (!uncorrectedAwns.isEmpty()) {
            AddressWithoutNum currAwn = uncorrectedAwns.remove();
            Map<BuildingRange, RangeDistrictData> rowValue = internalTable.remove(currAwn);
            AddressWithoutNum correctedAwn = correctionMap.get(currAwn);
            if (correctedAwn != null) {
                rowValue.forEach((key, value) -> put(correctedAwn, key, value));
            }
            else {
                rowValue.forEach((key, value) -> {
                    for (String sourceParser : value.getSourceNames()) {
                        invalidData.put(sourceParser, key.rangeString() + " " + currAwn);
                    }
                });
            }
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
     * Consolidates CompactDistrictMap data and returns (consolidated data, conflict Strings).
     */
    private Tuple<Map<BuildingRange, CompactDistrictMap>, Map<BuildingRange, String>> consolidateCells(
            Map<BuildingRange, RangeDistrictData> input) {
        // The algorithm becomes much easier when just dealing with numbers.
        var bldgNumToCells = new HashMap<Integer, RangeDistrictData>();
        input.forEach((key, value) -> {
            for (int bldgNum : key.allInRange()) {
                bldgNumToCells.computeIfAbsent(bldgNum, k -> new RangeDistrictData()).add(value);
            }
        });
        // Collect the building numbers with common districts.
        Multimap<CompactDistrictMap, Integer> districtsToBldgNums = ArrayListMultimap.create();
        Multimap<String, Integer> conflictStringToBldgNums = ArrayListMultimap.create();
        for (var entry : bldgNumToCells.entrySet()) {
            var consolidationResult = config.consolidationResult(entry.getValue(), sourceToTypeMap);
            if (!consolidationResult.second().isEmpty()) {
                conflictStringToBldgNums.put(consolidationResult.second(), entry.getKey());
            }
            districtsToBldgNums.put(consolidationResult.first(), entry.getKey());
        }
        // Combine ranges of building numbers.
        return new Tuple<>(combineBuildingNums(districtsToBldgNums), combineBuildingNums(conflictStringToBldgNums));
    }

    private static <T> Map<BuildingRange, T> combineBuildingNums(Multimap<T, Integer> inputMap) {
        var outputMap = new HashMap<BuildingRange, T>();
        for (var entry : inputMap.asMap().entrySet()) {
            BuildingRange.combineRanges(entry.getValue())
                    .forEach(range -> outputMap.put(range, entry.getKey()));
        }
        return outputMap;
    }
}
