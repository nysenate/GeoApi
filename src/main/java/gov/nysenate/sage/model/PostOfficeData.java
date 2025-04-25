package gov.nysenate.sage.model;

import org.apache.tomcat.util.collections.CaseInsensitiveKeyMap;

import java.util.Map;

/**
 * Stores data about Post Office addresses in a single zipcode.
 */
public class PostOfficeData<T> {
    private final CaseInsensitiveKeyMap<T> townToDataMap = new CaseInsensitiveKeyMap<>();
    private final T consolidatedData;

    public PostOfficeData(Map<String, T> townToDataMap, T consolidatedData) {
        // The internal Map does not allow null keys, which have no meaning anyway.
        for (Map.Entry<String, T> entry : townToDataMap.entrySet()) {
            if (entry.getKey() != null) {
                this.townToDataMap.put(entry.getKey(), entry.getValue());
            }
        }
        this.consolidatedData = consolidatedData;
    }

    /**
     * Attempts to match the postal city. Otherwise, just returns the consolidated data.
     */
    public T getData(String postalCity) {
        if (postalCity == null) {
            return consolidatedData;
        }
        return townToDataMap.getOrDefault(postalCity, consolidatedData);
    }
}
