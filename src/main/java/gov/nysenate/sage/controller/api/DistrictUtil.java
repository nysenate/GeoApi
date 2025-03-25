package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.model.address.GeocodedAddress;
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

    public static DistrictResult consolidateResultsWithoutConflicts(List<DistrictResult> districtResults) {
        DistrictMatchLevel consolidatedMatchLevel = DistrictMatchLevel.getMin(
                districtResults.stream().map(result -> result.getDistrictInfo().matchLevel()).toList()
        );
        DistrictInfo consolidatedInfo = DistrictUtil.getDistrictInfoWithoutConflicts(
                districtResults.stream().map(DistrictResult::getDistrictInfo).toList(), consolidatedMatchLevel);
        List<LocalSource> sources  = districtResults.stream().map(BaseResult::getSource).toList();
        LocalSource source = sources.size() == 1 ? sources.get(0) : LocalSource.STREETFILE_AND_SHAPEFILE;
        GeocodedAddress geoAddr = districtResults.size() == 1 ? districtResults.get(0).getGeoAddress() : null;
        return new DistrictResult(source, geoAddr, consolidatedInfo);
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
        if (results.stream().noneMatch(BaseResult::isSuccess) || results.size() == 1) {
            return first;
        }
        DistrictInfo firstDistInfo = first.getDistrictInfo();
        var typeToDistrictMap = new HashMap<DistrictType, SingleDistrict>();
        boolean usedFallback = false;
        for (DistrictType distType : DistrictType.values()) {
            SingleDistrict singleDistrict = firstDistInfo.getDistrict(distType);
            if (singleDistrict != null) {
                typeToDistrictMap.put(distType, singleDistrict);
                continue;
            }
            Optional<SingleDistrict> distOpt = results.stream().skip(1)
                    .map(result -> result.getDistrictInfo().getDistrict(distType))
                    .filter(Objects::nonNull).findFirst();
            if (distOpt.isPresent()) {
                usedFallback = true;
                typeToDistrictMap.put(distType, distOpt.get());
            }
        }

        LocalSource finalSource = usedFallback ? LocalSource.STREETFILE_AND_SHAPEFILE : first.getSource();
        DistrictInfo finalDistInfo = new DistrictInfo(typeToDistrictMap, firstDistInfo.matchLevel());
        return new DistrictResult(finalSource, first.getGeoAddress(), finalDistInfo);
    }
}
