package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictMatchLevel;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.BaseResult;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.provider.district.LocalSource;

import java.util.List;
import java.util.Optional;

public final class DistrictUtil {
    private DistrictUtil() {}

    public static DistrictResult consolidateResultsWithoutConflicts(List<DistrictResult> districtResults) {
        DistrictInfo consolidatedInfo = DistrictUtil.getDistrictInfoWithoutConflicts(
                districtResults.stream().map(DistrictResult::getDistrictInfo).toList());
        DistrictMatchLevel consolidatedMatchLevel = DistrictMatchLevel.getMin(
                districtResults.stream().map(result -> result.getDistrictInfo().getMatchLevel()).toList()
        );
        consolidatedInfo.setMatchLevel(consolidatedMatchLevel);
        List<LocalSource> sources  = districtResults.stream().map(BaseResult::getSource).toList();
        LocalSource source = sources.size() == 1 ? sources.get(0) : LocalSource.STREETFILE_AND_SHAPEFILE;
        GeocodedAddress geoAddr = districtResults.size() == 1 ? districtResults.get(0).getGeoAddress() : null;
        return new DistrictResult(source, geoAddr, consolidatedInfo);
    }

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
     * Returns a DistrictResult, filling in as many codes as possible.
     * The first valid code in the List is used, if it exists.
     */
    public static DistrictResult consolidateResults(List<DistrictResult> results) {
        DistrictResult first = results.get(0);
        if (results.stream().noneMatch(BaseResult::isSuccess) || results.size() == 1) {
            return first;
        }
        DistrictInfo finalDistInfo = first.getDistrictInfo();
        boolean usedFallback = false;
        for (DistrictType distType : DistrictType.values()) {
            String baseCode = finalDistInfo.getDistCode(distType);
            if (baseCode != null) {
                continue;
            }
            Optional<String> distCode = results.stream().skip(1)
                    .map(result -> result.getDistrictInfo().getDistCode(distType))
                    .filter(DistrictUtil::isValidDistCode).findFirst();
            if (distCode.isPresent()) {
                usedFallback = true;
                finalDistInfo.setDistCode(distType, distCode.get());
            }
        }

        LocalSource finalSource = usedFallback ? LocalSource.STREETFILE_AND_SHAPEFILE : first.getSource();
        return new DistrictResult(finalSource, first.getGeoAddress(), finalDistInfo);
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
