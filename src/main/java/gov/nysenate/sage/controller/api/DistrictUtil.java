package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictType;

import java.util.Collection;
import java.util.List;

public final class DistrictUtil {
    private DistrictUtil() {}

    /**
     * Iterates over a list of DistrictInfo
     */
    // TODO: fill in, or combine?
    public static DistrictInfo consolidateDistrictInfo(Collection<DistrictInfo> districtInfoList) {
        if (districtInfoList.isEmpty()) {
            return new DistrictInfo();
        }
        DistrictInfo baseDistInfo = districtInfoList.iterator().next();
        if (baseDistInfo == null) {
            return new DistrictInfo();
        }
        for (DistrictType distType : DistrictType.values()) {
            String baseCode = baseDistInfo.getDistCode(distType);
            List<String> codes = districtInfoList.stream().map(info -> info.getDistCode(distType)).toList();
            if (codes.stream().anyMatch(code -> !isValidDistCode(code) || !baseCode.equals(code))) {
                baseDistInfo.setDistCode(distType, null);
            }
        }
        return baseDistInfo;
    }

    /**
     * Determines if code is valid or not by ensuring that the trimmed code does not equal '', 0, or null.
     */
    public static boolean isValidDistCode(String code) {
        if (code == null) {
            return false;
        }
        return !code.trim().matches("(?i)(^$|null|0+)");
    }
}
