package gov.nysenate.sage.model.district;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public record DistrictTableInfo(DistrictType type, String idColumn) {
    public DistrictTableInfo {
        if (StringUtils.isBlank(idColumn)) {
            throw new IllegalArgumentException("idColumn cannot be blank");
        }
    }

    public Map<String, String> getReplacements(String typeReplacementName) {
        var tempMap = new HashMap<String, String>();
        tempMap.put(typeReplacementName, type().name().toLowerCase());
        tempMap.put("idColumn", idColumn);
        return tempMap;
    }
}
