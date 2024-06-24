package gov.nysenate.sage.scripts.streetfinder.scripts.utils;

import gov.nysenate.sage.dao.provider.district.MunicipalityType;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.scripts.streetfinder.model.AddressWithoutNum;
import gov.nysenate.sage.scripts.streetfinder.model.BuildingRange;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Class containing information on how to parse a streetfile.
 * Has a builder pattern to clean up initialization.
 */
public class StreetfileDataExtractor {
    private static final int[] emptyIntArray = {};
    private static final Pattern townCityPattern = Pattern.compile("(TOWN ?|CITY ?)?(OF ?)?(.*?)([ /]?CITY|[ /]TOWN)?");
    private final String sourceName;
    private final Function<String, List<String>> lineParser;
    private final Map<DistrictType, Integer> typeToDistrictIndexMap = new HashMap<>();
    private Map<MunicipalityType, Map<String, Integer>> typeAndNameToIdMap;
    private int[] buildingIndices = emptyIntArray, streetIndices = emptyIntArray;
    private int postalCityIndex = -1, precinctIndex = -1;
    // Maps a line to a county FIPS code
    private Function<List<String>, String> countyFipsFunction;
    // Could test the line before or after parsing.
    private final List<LineTest<String>> lineTests = new ArrayList<>();
    private final List<LineTest<List<String>>> splitLineTests = new ArrayList<>();
    // Identifies lines for debugging and error reporting.
    private BiFunction<List<String>, Integer, Long> getIdFunc = (lineParts, lineNum) -> Long.valueOf(lineNum);

    public StreetfileDataExtractor(String sourceName, Function<String, List<String>> lineParser) {
        this.sourceName = sourceName;
        this.lineParser = lineParser;
    }

    public StreetfileDataExtractor setTable(Map<MunicipalityType, Map<String, Integer>> typeAndNameToIdMap) {
        this.typeAndNameToIdMap = typeAndNameToIdMap;
        return this;
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
        typeToDistrictIndexMap.put(type, index);
        return this;
    }

    /**
     * For example: if the highest index set for parsing is 4,
     * the next 3 types added with this function will have indices 5, 6, and 7.
     * @param types to add indices for.
     */
    public StreetfileDataExtractor addTypesInOrder(DistrictType... types) {
        int maxIndex = getMax(buildingIndices, streetIndices, new int[]{precinctIndex}, new int[]{postalCityIndex});
        maxIndex = Math.max(maxIndex, typeToDistrictIndexMap.values().stream().max(Integer::compareTo).orElse(0));
        for (DistrictType type : types) {
            typeToDistrictIndexMap.put(type, ++maxIndex);
        }
        return this;
    }

    public StreetfileDataExtractor addCountyFunction(Function<List<String>, Integer> lineToFipsFunction) {
        this.countyFipsFunction = lineParts -> String.valueOf(lineToFipsFunction.apply(lineParts));
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

    @Nonnull
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
            buildingRange = BuildingRange.getBuildingRange(getStrings(lineFields, buildingIndices, true));
        } catch (NumberFormatException ex) {
            return new StreetfileLineData(StreetfileLineType.BAD_BUILDING_NUMBER);
        }
        String street = String.join(" ", getStrings(lineFields, streetIndices, false));

        Integer townCityIndex = typeToDistrictIndexMap.get(DistrictType.TOWN_CITY);
        if (townCityIndex != null && typeAndNameToIdMap != null) {
            lineFields.set(townCityIndex, getTownCityId(lineFields.get(townCityIndex)));
        }
        CompactDistrictMap districts = CompactDistrictMap.getMap(type -> getValue(lineFields, type));
        String zip = lineFields.get(typeToDistrictIndexMap.get(DistrictType.ZIP));
        var addressWithoutNum = new AddressWithoutNum(street, lineFields.get(postalCityIndex), zip, true);
        var cell = new RangeDistrictData(districts, sourceName, getIdFunc.apply(lineFields, lineNum));
        return new StreetfileLineData(buildingRange, addressWithoutNum, cell, StreetfileLineType.PROPER);
    }

    public String getTownCityId(String input) {
        String[] split = input.toUpperCase().split(" ", 2);
        switch (split[0]) {
            case "N" -> split[0] = "NORTH";
            case "ST." -> split[0] = "ST";
            case "FT." -> split[0] = "FORT";
        }
        input = String.join(" ", split);
        var matcher = townCityPattern.matcher(input.toUpperCase());
        if (!matcher.matches()) {
            throw new RuntimeException();
        }

        Integer townCityId = null;
        String prefix = nullToEmpty(matcher.group(1));
        String townCity = matcher.group(3);
        String suffix = nullToEmpty(matcher.group(4));
        if (prefix.contains("TOWN") || suffix.contains("TOWN")) {
            townCityId = typeAndNameToIdMap.get(MunicipalityType.TOWN).get(townCity);
        }
        else if (prefix.contains("CITY") || suffix.contains("CITY")) {
            townCityId = typeAndNameToIdMap.get(MunicipalityType.CITY).get(townCity);
        }
        else {
            for (Map<String, Integer> idMap : typeAndNameToIdMap.values()) {
                Integer tempId = idMap.get(townCity);
                if (tempId != null) {
                    townCityId = tempId;
                }
            }
        }
        if (townCityId == null) {
            return "0";
        }
        return String.valueOf(townCityId).intern();
    }

    @Nonnull
    private static String nullToEmpty(String input) {
        return input == null ? "" : input;
    }

    private static List<String> getStrings(List<String> lineFields, int[] indices, boolean keepBlanks) {
        return Arrays.stream(indices).mapToObj(lineFields::get)
                .filter(str -> keepBlanks || !str.isBlank()).toList();
    }

    private static int getMax(int[]... arrays) {
        return Arrays.stream(arrays).flatMapToInt(Arrays::stream).max().orElse(0);
    }

    private String getValue(List<String> lineFields, DistrictType type) {
        if (type == DistrictType.COUNTY) {
            return countyFipsFunction.apply(lineFields);
        }
        Integer index = typeToDistrictIndexMap.get(type);
        return index == null ? "" : lineFields.get(index);
    }
}
