package gov.nysenate.sage.model.district;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class DistrictInfo {
    private final Map<String, String> internalMap = new HashMap<>();

    public void put(String columnName, String value) {
        internalMap.put(columnName, value);
    }

    public String getName() {
        return internalMap.get("name");
    }

    public String getCode() {
        return internalMap.get("code");
    }
}
