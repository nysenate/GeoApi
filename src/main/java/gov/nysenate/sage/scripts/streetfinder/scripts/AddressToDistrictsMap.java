package gov.nysenate.sage.scripts.streetfinder.scripts;

import java.util.*;
import java.util.stream.Collectors;

public class AddressToDistrictsMap {
    // address -> (district -> count)
    public static final Map<String, Map<String, Integer>> badSenateMap = new HashMap<>();
    private static final List<Integer> addressFields = List.of(0, 2, 3, 4, 8, 9),
        districtFields = List.of(17);
    private final Map<String, String> dataMap = new HashMap<>();
    public static final Set<String> cityFields = new HashSet<>();

    public boolean putData(String[] fields) {
        cityFields.add(fields[8]);
        var addressBuilder = new StringBuilder();
        for (int fieldNum : addressFields) {
            addressBuilder.append(fields[fieldNum].trim()).append("\t");
        }
        String address = addressBuilder.toString();

        var districtsBuilder = new StringBuilder();
        for (int fieldNum : districtFields) {
            districtsBuilder.append(fields[fieldNum].trim()).append("\t");
        }
        String districts = districtsBuilder.toString().trim();

        String currDistricts = dataMap.get(address);
        if (currDistricts == null) {
            dataMap.put(address, districts);
            return true;
        }
        if (!currDistricts.equals(districts)) {
            if (!badSenateMap.containsKey(address)) {
                badSenateMap.put(address, new HashMap<>());
            }
            var districtCount = badSenateMap.get(address);
            districtCount.put(districts, districtCount.getOrDefault(districts, 0) + 1);
            districtCount.put(currDistricts, districtCount.getOrDefault(currDistricts, 0) + 1);
        }
        return currDistricts.equals(districts);
    }

    public List<String> getEntries() {
        return dataMap.entrySet().stream().map(entry -> entry.getKey() + entry.getValue()).collect(Collectors.toList());
    }
}
