package gov.nysenate.sage.model.district;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.Map;

public class DistrictInfo {
    public static final String NAME_COLUMN = "name";
    private final Map<String, String> internalMap = new HashMap<>();

    public void put(String columnName, String value) {
        internalMap.put(columnName, value);
    }

    /**
     * Serializes the columns directly onto this object, instead of nesting them.
     */
    @JsonAnyGetter
    public Map<String, String> getInternalMap() {
        return internalMap;
    }

    public String get(String columnName) {
        return internalMap.get(columnName);
    }

    @JsonIgnore
    public String getName() {
        return get(NAME_COLUMN);
    }
}
