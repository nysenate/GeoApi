package gov.nysenate.sage.scripts.streetfinder.scripts.utils;

import gov.nysenate.sage.model.district.County;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.scripts.streetfinder.model.AddressWithoutNum;
import gov.nysenate.sage.scripts.streetfinder.model.BuildingRange;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class StreetfileDataExtractor {
    private static final int[] emptyIntArray = {};
    private final String sourceName;
    private final Function<String, List<String>> lineParser;
    private final Map<DistrictType, Integer> typeToDistrictIndexMap = new HashMap<>(),
            typeToStringIndexMap = new HashMap<>();
    private int[] buildingIndices = emptyIntArray, streetIndices = emptyIntArray;
    private int postalCityIndex = -1, precinctIndex = -1;
    private Function<List<String>, String> countyFipsFunction;
    private final List<LineTest<String>> lineTests = new ArrayList<>();
    private final List<LineTest<List<String>>> splitLineTests = new ArrayList<>();
    private BiFunction<List<String>, Integer, Long> getIdFunc = (lineParts, lineNum) -> Long.valueOf(lineNum);

    public StreetfileDataExtractor(String sourceName, Function<String, List<String>> lineParser) {
        this.sourceName = sourceName;
        this.lineParser = lineParser;
    }

    public StreetfileDataExtractor addBuildingIndices(int... indices) {
        this.buildingIndices = indices;
        return this;
    }

    public StreetfileDataExtractor addStreetIndices(int... indices) {
        this.streetIndices = indices;
        return this;
    }

    public StreetfileDataExtractor addPostalCityIndex(int index) {
        this.postalCityIndex = index;
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

    public StreetfileDataExtractor addTypesInOrder(DistrictType... types) {
        int maxIndex = getMax(buildingIndices, streetIndices, new int[]{precinctIndex});
        maxIndex = Math.max(maxIndex, typeToDistrictIndexMap.values().stream().max(Integer::compareTo).orElse(0));
        maxIndex = Math.max(maxIndex, typeToStringIndexMap.values().stream().max(Integer::compareTo).orElse(0));
        for (DistrictType type : types) {
            addToMap(type, ++maxIndex);
        }
        return this;
    }

    public StreetfileDataExtractor addCountyFunction(int index, Function<String, Integer> fipsCodeMap) {
        this.countyFipsFunction = list -> String.valueOf(fipsCodeMap.apply(list.get(index)));
        return this;
    }

    public StreetfileDataExtractor addCountyFunction(County county) {
        this.countyFipsFunction = list -> String.valueOf(county.fipsCode());
        return this;
    }

    public StreetfileDataExtractor addIdFunction(BiFunction<List<String>, Integer, Long> getIdFunc) {
        this.getIdFunc = getIdFunc;
        return this;
    }

    public StreetfileDataExtractor addIsProperLengthFunction(int length) {
        return addSplitTest(lineParts -> lineParts.size() != length, StreetfileLineType.WRONG_LENGTH);
    }

    public StreetfileDataExtractor addTest(Predicate<String> predicate, StreetfileLineType errorType) {
        this.lineTests.add(new LineTest<>(predicate, errorType));
        return this;
    }

    public StreetfileDataExtractor addSplitTest(Predicate<List<String>> predicate, StreetfileLineType errorType) {
        this.splitLineTests.add(new LineTest<>(predicate, errorType));
        return this;
    }

    public StreetfileLineData getData(int lineNum, String line) {
        for (LineTest<String> lineTest : lineTests) {
            if (lineTest.test().test(line)) {
                return new StreetfileLineData(lineTest.errorType());
            }
        }
        List<String> lineFields = lineParser.apply(line);
        for (LineTest<List<String>> lineTest : splitLineTests) {
            if (lineTest.test().test(lineFields)) {
                return new StreetfileLineData(lineTest.errorType());
            }
        }
        BuildingRange buildingRange;
        try {
            buildingRange = BuildingRange.getBuildingRange(getStrings(lineFields, buildingIndices));
        } catch (NumberFormatException ex) {
            return new StreetfileLineData(StreetfileLineType.BAD_BUILDING_NUMBER);
        }
        String street = String.join(" ", getStrings(lineFields, streetIndices));
        CompactDistrictMap districts = CompactDistrictMap.getMap(typeToDistrictIndexMap.keySet(), type -> getValue(lineFields, type));
        var addressWithoutNum = new AddressWithoutNum(street, lineFields.get(postalCityIndex), lineFields.get(typeToStringIndexMap.get(DistrictType.ZIP)));
        var cellId = new CellId(sourceName, getIdFunc.apply(lineFields, lineNum));
        return new StreetfileLineData(buildingRange, addressWithoutNum, districts, cellId, StreetfileLineType.PROPER);
    }

    private static List<String> getStrings(List<String> lineFields, int[] indices) {
        return Arrays.stream(indices).mapToObj(lineFields::get)
                .filter(str -> !str.isBlank()).toList();
    }

    private static int getMax(int[]... arrays) {
        return Arrays.stream(arrays).flatMapToInt(Arrays::stream).max().orElse(0);
    }

    private void addToMap(DistrictType type, int index) {
        var currMap = switch (type) {
            case TOWN_CITY, ZIP, VILLAGE -> typeToStringIndexMap;
            default -> typeToDistrictIndexMap;
        };
        currMap.put(type, index);
    }

    private String getValue(List<String> lineFields, DistrictType type) {
        if (type == DistrictType.COUNTY) {
            return countyFipsFunction.apply(lineFields);
        }
        Integer index = typeToDistrictIndexMap.get(type);
        return index == null ? "" : lineFields.get(index);
    }
}
