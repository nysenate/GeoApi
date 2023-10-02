package gov.nysenate.sage.scripts.streetfinder;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class SortedStringMultiMap {
    private final SortedMap<String, List<String>> internalMap = new TreeMap<>();

    public void put(String key, String value) {
        List<String> currValues = internalMap.get(key);
        if (currValues == null) {
            currValues = new ArrayList<>();
        }
        currValues.add(value);
        internalMap.put(key, currValues);
    }

    public boolean isEmpty() {
        return internalMap.isEmpty();
    }

    @Override
    public String toString() {
        var strBuilder = new StringBuilder();
        for (String street : internalMap.keySet()) {
            strBuilder.append(street).append('\n');
            for (String line : internalMap.get(street)) {
                strBuilder.append('\t').append(line).append('\n');
            }
        }
        return strBuilder.toString();
    }
}
