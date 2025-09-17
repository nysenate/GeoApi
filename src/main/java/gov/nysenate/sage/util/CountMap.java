package gov.nysenate.sage.util;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple class to maintain and print a count of different T.
 */
public class CountMap<T> {
    private final Map<T, Integer> internalMap = new HashMap<>();

    public void put(T key) {
        put(key, 1);
    }

    public void put(T key, int count) {
        internalMap.merge(key, count, Integer::sum);
    }

    public void addAll(CountMap<T> otherMap) {
        for (var entry : otherMap.internalMap.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public boolean containsOnly(T key) {
        return internalMap.size() == 1 && internalMap.containsKey(key);
    }

    public String toString(int total, boolean sort) {
        var strBuilder = new StringBuilder();
        var entries = internalMap.entrySet().stream().toList();
        if (sort) {
            entries = entries.stream()
                    .sorted((o1, o2) -> o2.getValue() - o1.getValue()).toList();
        }
        for (var entry : entries) {
            strBuilder.append("\t%s: %.2f%%%n".formatted(entry.getKey(), 100.0 * entry.getValue()/total));
        }
        return strBuilder.toString();
    }
}
