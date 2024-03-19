package gov.nysenate.sage.scripts.streetfinder.scripts.utils;

import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.scripts.streetfinder.model.StreetFileAddressRange;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

// TODO: "Builder" model instead, with some defaults?
public class StreetfileDataExtractor {
    private static final int[] emptyIntArray = {};
    private final String sourceName;
    private Map<DistrictType, Integer> typeToIndexMap = Map.of();
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

    public StreetfileDataExtractor addTypeToIndexMap(Map<DistrictType, Integer> typeToIndexMap) {
        this.typeToIndexMap = typeToIndexMap;
        return this;
    }

    public StreetfileDataExtractor addType(DistrictType type, int index) {
        typeToIndexMap.put(type, index);
        return this;
    }

    public StreetfileDataExtractor addTypesInOrder(DistrictType... types) {
        int maxIndex = getMax(buildingIndices, streetIndices, new int[]{precinctIndex});
        maxIndex = Math.max(maxIndex, typeToIndexMap.values().stream().max(Integer::compareTo).orElse(0));
        for (DistrictType type : types) {
            typeToIndexMap.put(type, ++maxIndex);
        }
        return this;
    }

    public StreetfileDataExtractor addIdFunction(BiFunction<List<String>, Integer, Long> getIdFunc) {
        this.getIdFunc = getIdFunc;
        return this;
    }

    public StreetfileLineData getData(int lineNum, String[] lineFields) {
        return getData(lineNum, List.of(lineFields));
    }

    public StreetfileLineData getData(int lineNum, List<String> lineFields) {
        var address = new StreetFileAddressRange();
        address.setBuildingRange(Arrays.stream(buildingIndices).mapToObj(lineFields::get).collect(Collectors.joining(" ")));
        for (int i : streetIndices) {
            address.addToStreet(lineFields.get(i));
        }
        var districts = CompactDistrictMap.getMap(typeToIndexMap.keySet(), type -> lineFields.get(typeToIndexMap.get(type)));
        return new StreetfileLineData(address, districts, new CellId(sourceName, getIdFunc.apply(lineFields, lineNum)));
    }

    private static int getMax(int[]... arrays) {
        return Arrays.stream(arrays).flatMapToInt(Arrays::stream).max().orElse(0);
    }
}
