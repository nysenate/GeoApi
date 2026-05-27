package gov.nysenate.sage.model.district;

import org.apache.commons.lang3.StringUtils;

public record DistrictTypeInfo(String codeColumn, String nameColumn) {
    public DistrictTypeInfo {
        if (StringUtils.isBlank(codeColumn)) {
            throw new IllegalArgumentException("codeColumn cannot be empty");
        }
        // Many DistrictTypes do not have separate names
        if (StringUtils.isBlank(nameColumn)) {
            nameColumn = codeColumn;
        }
    }
}
