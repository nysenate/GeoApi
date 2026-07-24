package gov.nysenate.sage.model.district;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public record DistrictTableInfo(DistrictType type, String codeColumn, String nameColumn) {
    public DistrictTableInfo {
        if (StringUtils.isBlank(codeColumn)) {
            throw new IllegalArgumentException("codeColumn cannot be empty");
        }
        if (nameColumn != null && nameColumn.isBlank()) {
            throw new IllegalArgumentException("nameColumn should never be blank: use null instead");
        }
    }

    public Map<String, String> getReplacements(String typeReplacementName) {
        var tempMap = new HashMap<String, String>();
        tempMap.put(typeReplacementName, type().name().toLowerCase());
        tempMap.put("codeColumn", codeColumn);
        if (nameColumn != null) {
            tempMap.put("nameColumn", nameColumn);
        }
        return tempMap;
    }
}
