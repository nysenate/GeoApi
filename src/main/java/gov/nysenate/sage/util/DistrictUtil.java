package gov.nysenate.sage.util;

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
                districtResults.stream().map(result -> result.getAssignedDistricts().accuracy()).toList()
        );
        AssignedDistricts consolidatedInfo = DistrictUtil.getDistrictInfoWithoutConflicts(
                districtResults.stream().map(DistrictResult::getAssignedDistricts).toList(), consolidatedAccuracy);
        List<LocalSource> sources  = districtResults.stream().map(BaseResult::getSources)
                .flatMap(Collection::stream).toList();
        return new DistrictResult(sources, consolidatedInfo);
    }

    /**
     * Returns a DistrictInfo without conflicts between codes.
     */
    public static AssignedDistricts getDistrictInfoWithoutConflicts(List<AssignedDistricts> assignedDistrictsList,
                                                                    Accuracy accuracy) {
        Map<DistrictType, String> typeToDistrictMap = new HashMap<>();
        for (DistrictType distType : DistrictType.values()) {
            List<String> codes = assignedDistrictsList.stream()
                    .map(info -> info.getDistCode(distType)).filter(Objects::nonNull).distinct().toList();
            if (codes.size() == 1) {
                typeToDistrictMap.put(distType, codes.getFirst());
            }
        }
        return new AssignedDistricts(typeToDistrictMap, accuracy);
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
        var typeToDistrictMap = new HashMap<DistrictType, String>();
        var sourcesUsed = new ArrayList<LocalSource>();
        for (DistrictType distType : DistrictType.values()) {
            for (DistrictResult result : results) {
                String code = result.getAssignedDistricts().getDistCode(distType);
                if (code != null) {
                    typeToDistrictMap.put(distType, code);
                    sourcesUsed.addAll(result.getSources());
                    break;
                }
            }
        }

        var finalDistInfo = new AssignedDistricts(typeToDistrictMap, first.getAssignedDistricts().accuracy());
        return new DistrictResult(sourcesUsed, finalDistInfo);
    }
}
