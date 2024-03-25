package gov.nysenate.sage.scripts.streetfinder.scripts.utils;

import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.scripts.streetfinder.model.StreetfileAddressRange;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StreetfileDataExtractor {
    private static final int[] emptyIntArray = {};
    private final String sourceName;
    private final Map<DistrictType, Integer> typeToDistrictIndexMap = new HashMap<>(),
            typeToStringIndexMap = new HashMap<>();
    private final Map<DistrictType, Function<String, String>> typeCorrectionMap = new HashMap<>();
    private int[] buildingIndices = emptyIntArray, streetIndices = emptyIntArray;
    private int precinctIndex = -1;
    private BiFunction<List<String>, Integer, Long> getIdFunc = (lineParts, lineNum) -> Long.valueOf(lineNum);

    public StreetfileDataExtractor(String sourceName) {
        this.sourceName = sourceName;
    }

    public StreetfileDataExtractor addBuildingIndices(int... indices) {
        this.buildingIndices = indices;
        return this;
    }

    public StreetfileDataExtractor addStreetIndices(int... indices) {
        this.streetIndices = indices;
        return this;
    }

    public StreetfileDataExtractor addPrecinctIndex(int index) {
        this.precinctIndex = index;
        return this;
    }

    public StreetfileDataExtractor addType(DistrictType type, int index) {
        addToMap(type, index);
        return this;
    }

    public StreetfileDataExtractor addTypeWithCorrection(DistrictType type, int index, Function<String, String> correctionFunc) {
        typeCorrectionMap.put(type, correctionFunc);
        return addType(type, index);
    }

    public StreetfileDataExtractor addTypesInOrder(DistrictType... types) {
        int maxIndex = getMax(buildingIndices, streetIndices, new int[]{precinctIndex});
        maxIndex = Math.max(maxIndex, typeToDistrictIndexMap.values().stream().max(Integer::compareTo).orElse(0));
        maxIndex = Math.max(maxIndex, typeToStringIndexMap.values().stream().max(Integer::compareTo).orElse(0));
        for (DistrictType type : types) {
            addToMap(type, ++maxIndex);
        }
        return this;
    }

    public StreetfileDataExtractor addIdFunction(BiFunction<List<String>, Integer, Long> getIdFunc) {
        this.getIdFunc = getIdFunc;
        return this;
    }

    public StreetfileLineData getData(int lineNum, List<String> lineFields) {
        for (var entry : typeCorrectionMap.entrySet()) {
            int index = typeToStringIndexMap.get(entry.getKey());
            index = typeToDistrictIndexMap.getOrDefault(entry.getKey(), index);
            String current = lineFields.get(index);
            lineFields.set(index, entry.getValue().apply(current));
        }
        var address = new StreetfileAddressRange();
        address.setBuildingRange(Arrays.stream(buildingIndices).mapToObj(lineFields::get)
                .collect(Collectors.joining(" ")));
        for (int i : streetIndices) {
            address.addToStreet(lineFields.get(i));
        }
        var districts = CompactDistrictMap.getMap(typeToDistrictIndexMap.keySet(),
                type -> lineFields.get(typeToDistrictIndexMap.get(type)));
        return new StreetfileLineData(address, districts, new CellId(sourceName, getIdFunc.apply(lineFields, lineNum)));
    }

    private static int getMax(int[]... arrays) {
        return Arrays.stream(arrays).flatMapToInt(Arrays::stream).max().orElse(0);
    }

    private void addToMap(DistrictType type, int index) {
        var currMap = switch (type) {
            case TOWN, ZIP, CITY, VILLAGE -> typeToStringIndexMap;
            default -> typeToDistrictIndexMap;
        };
        currMap.put(type, index);
    }
}
