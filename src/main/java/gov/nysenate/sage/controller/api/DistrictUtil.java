package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.DistrictedAddress;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.api.BatchDistrictRequest;
import gov.nysenate.sage.model.api.SingleDistrictRequest;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictMatchLevel;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.provider.district.DistrictSource;
import gov.nysenate.sage.provider.geocode.Geocoder;

import java.util.Collection;
import java.util.List;

public final class DistrictUtil {
    private DistrictUtil() {}

    public static BatchDistrictRequest createBatchAssignDistrictRequest(Geocoder geocoder, boolean uspsValidate,
                                                                        boolean usePunct, boolean skipGeocode, List<DistrictSource> providers) {
        var districtRequest = new BatchDistrictRequest();
        districtRequest.setProviders(providers);
        districtRequest.setGeocoder(geocoder);
        districtRequest.setUsePunct(usePunct);
        districtRequest.setUspsValidate(uspsValidate);
        // TODO: does this make sense, given the providers?
        districtRequest.setSkipGeocode(skipGeocode);
        return districtRequest;
    }

    public static SingleDistrictRequest createFullDistrictRequest(Address address, Point point,
                                                                  Geocoder geocoder, boolean uspsValidate,
                                                                  boolean usePunct, boolean skipGeocode, List<DistrictSource> providers) {
        var districtRequest = new SingleDistrictRequest();
        districtRequest.setAddress(address);
        districtRequest.setPoint(point);
        districtRequest.setProviders(providers);
        districtRequest.setGeocoder(geocoder);
        districtRequest.setUspsValidate(uspsValidate);
        districtRequest.setUsePunct(usePunct);
        districtRequest.setSkipGeocode(skipGeocode);
        return districtRequest;
    }

    public static DistrictedAddress consolidateDistrictedAddress(Collection<DistrictedAddress> results) {
        if (results.isEmpty()) {
            return new DistrictedAddress();
        }
        GeocodedAddress geoAddr = results.size() == 1 ? results.iterator().next().getGeocodedAddress() : null;
        DistrictInfo districtInfo = consolidateDistrictInfo(results.stream().map(DistrictedAddress::getDistrictInfo).toList());
        DistrictMatchLevel matchLevel = results.stream().map(DistrictedAddress::getDistrictMatchLevel)
                .min(DistrictMatchLevel::compareTo).orElse(DistrictMatchLevel.NOMATCH);
        return new DistrictedAddress(geoAddr, districtInfo, matchLevel);
    }

    /**
     * Iterates over a list of DistrictInfo and returns a single DistrictInfo that represents the districts
     * that were common amongst every entry.
     * @return DistrictInfo containing the districts that were common.
     *         If the senate code is not common, the return value will be null.
     */
    public static DistrictInfo consolidateDistrictInfo(Collection<DistrictInfo> districtInfoList) {
        if (districtInfoList.isEmpty()) {
            return null;
        }
        DistrictInfo baseDistInfo = districtInfoList.iterator().next();
        if (baseDistInfo == null) {
            return null;
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
