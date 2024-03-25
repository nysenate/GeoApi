package gov.nysenate.sage.scripts.streetfinder.scripts.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple memory-saving helper class. Uses the same logic as String's intern() method:
 * Objects.equals(x, y) == (get(x) == get(y))
 * @param <T> the type of data to store.
 */
public class Intern<T> {
    private final Map<T, T> interned = new HashMap<>();

    public T get(T data) {
        if (!interned.containsKey(data)) {
            interned.put(data, data);
        }
        return interned.get(data);
    }
}
