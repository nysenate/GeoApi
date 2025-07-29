package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictMatchLevel;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.district.SingleDistrict;
import gov.nysenate.sage.model.result.BaseResult;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.provider.district.LocalSource;

import java.util.*;

public final class DistrictUtil {
    private DistrictUtil() {}

    public static DistrictResult consolidateResultsWithoutConflicts(Collection<DistrictResult> districtResults) {
        districtResults = districtResults.stream().filter(BaseResult::isSuccess).toList();
        DistrictMatchLevel consolidatedMatchLevel = DistrictMatchLevel.getMin(
                districtResults.stream().map(result -> result.getDistrictInfo().matchLevel()).toList()
        );
        DistrictInfo consolidatedInfo = DistrictUtil.getDistrictInfoWithoutConflicts(
                districtResults.stream().map(DistrictResult::getDistrictInfo).toList(), consolidatedMatchLevel);
        List<LocalSource> sources  = districtResults.stream().map(BaseResult::getSources)
                .flatMap(Collection::stream).toList();
        return new DistrictResult(new LinkedHashSet<>(sources), consolidatedInfo);
    }

    /**
     * Returns a DistrictInfo without conflicts between codes.
     */
    public static DistrictInfo getDistrictInfoWithoutConflicts(List<DistrictInfo> districtInfoList,
                                                               DistrictMatchLevel matchLevel) {
        Map<DistrictType, SingleDistrict> typeToDistrictMap = new HashMap<>();
        for (DistrictType distType : DistrictType.values()) {
            List<SingleDistrict> singleDistricts = districtInfoList.stream()
                    .map(info -> info.getDistrict(distType)).filter(Objects::nonNull).distinct().toList();
            if (singleDistricts.size() == 1) {
                typeToDistrictMap.put(distType, singleDistricts.get(0));
            }
        }
        return new DistrictInfo(typeToDistrictMap, matchLevel);
    }

    /**
     * Returns a DistrictResult, filling in as many codes as possible.
     * The first valid code in the List is used, if it exists.
     */
    public static DistrictResult consolidateResults(List<DistrictResult> results) {
        DistrictResult first = results.get(0);
        results = results.stream().filter(BaseResult::isSuccess).toList();
        if (results.size() <= 1) {
            return first;
        }
        var typeToDistrictMap = new HashMap<DistrictType, SingleDistrict>();
        var sourcesUsed = new LinkedHashSet<LocalSource>();
        for (DistrictType distType : DistrictType.values()) {
            for (DistrictResult result : results) {
                SingleDistrict singleDistrict = result.getDistrictInfo().getDistrict(distType);
                if (singleDistrict != null) {
                    typeToDistrictMap.put(distType, singleDistrict);
                    sourcesUsed.addAll(result.getSources());
                    break;
                }
            }
        }

        var finalDistInfo = new DistrictInfo(typeToDistrictMap, first.getDistrictInfo().matchLevel());
        return new DistrictResult(sourcesUsed, finalDistInfo);
    }
}
