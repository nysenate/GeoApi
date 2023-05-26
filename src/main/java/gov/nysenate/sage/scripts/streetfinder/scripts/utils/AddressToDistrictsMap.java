package gov.nysenate.sage.scripts.streetfinder.scripts.utils;

import com.google.common.collect.ArrayListMultimap;

import java.util.*;
import java.util.stream.Collectors;

import static gov.nysenate.sage.scripts.streetfinder.scripts.utils.VoterFileField.STATUS;
import static gov.nysenate.sage.scripts.streetfinder.scripts.utils.VoterFileLineType.NON_STANDARD_ADDRESS;
import static gov.nysenate.sage.scripts.streetfinder.scripts.utils.VoterFileLineType.VALID;

/**
 * Stores lines from a voter file as a map from address data -> district data.
 * Also stores information about addresses matched to different districts, and some summary information.
 */
public class AddressToDistrictsMap {
    private final ArrayListMultimap<VoterFileLineType, VoterFileLineMap> invalidLines = ArrayListMultimap.create();
    private int duplicates = 0;
    private final Map<String, Integer> statusCountMap = new HashMap<>();
    // Using Strings is more compact.
    private final Map<String, String> dataMap;
    public final ArrayListMultimap<String, String> multipleDistrictMatch = ArrayListMultimap.create();

    public AddressToDistrictsMap(int initialSize) {
        this.dataMap = new HashMap<>(initialSize);
    }

    /**
     * Stores a line of data into the maps.
     */
    public void putData(VoterFileLineMap fieldMap) {
        statusCountMap.merge(fieldMap.get(STATUS), 1, Integer::sum);
        if (fieldMap.getType() != VALID) {
            invalidLines.put(fieldMap.getType(), fieldMap);
            return;
        }
        // Inactive and purged voters do not have updated address information.
        if (fieldMap.get(STATUS).matches("[IP]")) {
            return;
        }
        // Data is good to be inserted.
        var address = fieldMap.getAddress();
        var currDistricts = fieldMap.getDistricts();

        // If there's already been a conflict at this address, it shouldn't be added to the normal data.
        if (multipleDistrictMatch.containsKey(address)) {
            multipleDistrictMatch.get(address).add(currDistricts);
            return;
        }

        String mappedDistricts = dataMap.get(address);
        if (mappedDistricts == null) {
            dataMap.put(address, currDistricts);
        }
        else if (!mappedDistricts.equals(currDistricts)) {
            // Conflict found! Put data into other Map.
            dataMap.remove(address);
            multipleDistrictMatch.put(address, mappedDistricts);
            multipleDistrictMatch.put(address, currDistricts);
        }
        else {
            duplicates++;
        }
    }

    public Set<Map.Entry<String, String>> entrySet() {
        return dataMap.entrySet();
    }

    public void printBadLines() {
        for (var type : invalidLines.keySet()) {
            if (type == NON_STANDARD_ADDRESS) {
                continue;
            }
            System.out.println(type + ":");
            var lines = invalidLines.get(type);
            List<VoterFileField> displayFields = new ArrayList<>(VoterFileField.defaultDisplayFields);
            for (var field : VoterFileField.defaultDisplayFields) {
                if (lines.stream().allMatch(line -> line.get(field).isEmpty())) {
                    displayFields.remove(field);
                }
            }
            System.out.println("\"" + displayFields.stream().map(Enum::name).collect(Collectors.joining("\",\"")) + "\"");
            for (var line : lines) {
                System.out.println("\t\"" + displayFields.stream().map(line::get).collect(Collectors.joining("\",\"")) + "\"");
            }
            System.out.println();
        }
    }

    public void printSummary() {
        System.out.println("\nStatus data: ");
        for (var entry : statusCountMap.entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }
        System.out.println();
        System.out.println(dataMap.size() + " standard entries");
        System.out.println(invalidLines.get(NON_STANDARD_ADDRESS).size() + " alternate addresses");
        System.out.println(duplicates + " duplicate addresses\n");
    }
}
