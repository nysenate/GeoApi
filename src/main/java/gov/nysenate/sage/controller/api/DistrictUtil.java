package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictType;

import java.util.List;

public final class DistrictUtil {
    private DistrictUtil() {}

    /**
     * Returns a DistrictInfo without conflicts between codes.
     */
    public static DistrictInfo getDistrictInfoWithoutConflicts(List<DistrictInfo> districtInfoList) {
        if (districtInfoList.isEmpty()) {
            return new DistrictInfo();
        }
        DistrictInfo baseDistInfo = districtInfoList.get(0);
        for (DistrictType distType : DistrictType.values()) {
            String baseCode = baseDistInfo.getDistCode(distType);
            if (districtInfoList.stream().map(info -> info.getDistCode(distType))
                    .anyMatch(code -> !isValidDistCode(code) || !baseCode.equals(code))) {
                baseDistInfo.setDistCode(distType, null);
            }
        }
        return baseDistInfo;
    }

    /**
     * Returns a DistrictInfo, filling in as many codes as possible.
     * The first valid code in the List is used, if it exists.
     */
    public static DistrictInfo consolidateDistrictInfo(List<DistrictInfo> districtInfoList) {
        if (districtInfoList.isEmpty()) {
            return new DistrictInfo();
        }
        DistrictInfo baseDistInfo = districtInfoList.get(0);
        for (DistrictType distType : DistrictType.values()) {
            String baseCode = baseDistInfo.getDistCode(distType);
            if (baseCode != null) {
                continue;
            }
            districtInfoList.stream().skip(1).map(info -> info.getDistCode(distType))
                    .filter(DistrictUtil::isValidDistCode).findFirst()
                    .ifPresent(newCode -> baseDistInfo.setDistCode(distType, newCode));
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
