package gov.nysenate.sage.scripts.streetfinder.scripts.utils;

import com.google.common.collect.ArrayListMultimap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static gov.nysenate.sage.scripts.streetfinder.scripts.utils.VoterFileField.STATUS;
import static gov.nysenate.sage.scripts.streetfinder.scripts.utils.VoterFileField.streetFileFields;
import static gov.nysenate.sage.scripts.streetfinder.scripts.utils.VoterFileLineType.*;

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
    private final ArrayListMultimap<String, String> multipleDistrictMatch = ArrayListMultimap.create();

    public AddressToDistrictsMap(int initialSize) {
        this.dataMap = new HashMap<>(initialSize);
    }

    /**
     * Stores a line of data into the maps.
     */
    public void putData(String line) {
        var fieldMap = new VoterFileLineMap(line);
        statusCountMap.merge(fieldMap.get(STATUS), 1, Integer::sum);
        // Inactive and purged voters do not have updated address information.
        if (fieldMap.getType() != WRONG_FIELD_LENGTH && fieldMap.get(STATUS).matches("[IP]")) {
            return;
        }
        if (fieldMap.getType() != VALID) {
            invalidLines.put(fieldMap.getType(), fieldMap);
            return;
        }
        // Data is good to be inserted.
        var addressList = new ArrayList<String>();
        var districtList = new ArrayList<String>();
        for (VoterFileField field : streetFileFields) {
            switch (field.getType()) {
                case ADDRESS -> addressList.add(fieldMap.get(field));
                case DISTRICT -> districtList.add(fieldMap.get(field));
            }
        }
        var address = String.join("\t", addressList);
        var currDistricts = String.join("\t", districtList);

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

    public void printSummary() {
        System.out.println("\nStatus data: ");
        for (var entry : statusCountMap.entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }
        System.out.println();
        System.out.println(dataMap.size() + " standard entries");
        System.out.println(invalidLines.get(NON_STANDARD_ADDRESS).size() + " alternate addresses");
        System.out.println(duplicates + " duplicate addresses");
    }
}
