package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.model.Accuracy;
import gov.nysenate.sage.model.district.*;
import gov.nysenate.sage.model.result.BaseResult;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.provider.district.LocalSource;

import java.util.*;

public final class DistrictUtil {
    private DistrictUtil() {}

    public static DistrictResult consolidateResultsWithoutConflicts(Collection<DistrictResult> districtResults) {
        districtResults = districtResults.stream().filter(BaseResult::isSuccess).toList();
        Accuracy consolidatedAccuracy = Accuracy.getMin(
                districtResults.stream().map(result -> result.getDistrictInfo().accuracy()).toList()
        );
        DistrictInfo consolidatedInfo = DistrictUtil.getDistrictInfoWithoutConflicts(
                districtResults.stream().map(DistrictResult::getDistrictInfo).toList(), consolidatedAccuracy);
        List<LocalSource> sources  = districtResults.stream().map(BaseResult::getSources)
                .flatMap(Collection::stream).toList();
        return new DistrictResult(sources, consolidatedInfo);
    }

    /**
     * Returns a DistrictInfo without conflicts between codes.
     */
    public static DistrictInfo getDistrictInfoWithoutConflicts(List<DistrictInfo> districtInfoList,
                                                               Accuracy accuracy) {
        Map<DistrictType, SingleDistrict> typeToDistrictMap = new HashMap<>();
        for (DistrictType distType : DistrictType.values()) {
            List<SingleDistrict> singleDistricts = districtInfoList.stream()
                    .map(info -> info.getDistrict(distType)).filter(Objects::nonNull).distinct().toList();
            if (singleDistricts.size() == 1) {
                typeToDistrictMap.put(distType, singleDistricts.getFirst());
            }
        }
        return new DistrictInfo(typeToDistrictMap, accuracy);
    }

    /**
     * Returns a DistrictResult, filling in as many codes as possible.
     * The first valid code in the List is used, if it exists.
     */
    public static DistrictResult consolidateResults(List<DistrictResult> results) {
        DistrictResult first = results.getFirst();
        results = results.stream().filter(BaseResult::isSuccess).toList();
        if (results.size() <= 1) {
            return first;
        }
        var typeToDistrictMap = new HashMap<DistrictType, SingleDistrict>();
        var sourcesUsed = new ArrayList<LocalSource>();
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

        var finalDistInfo = new DistrictInfo(typeToDistrictMap, first.getDistrictInfo().accuracy());
        return new DistrictResult(sourcesUsed, finalDistInfo);
    }
}
