package gov.nysenate.sage.controller.api;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.DistrictedAddress;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.api.ApiRequest;
import gov.nysenate.sage.model.api.DistrictRequest;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictMatchLevel;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.geo.Point;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import static gov.nysenate.sage.model.district.DistrictType.SENATE;

public final class DistrictUtil {
    private DistrictUtil() {}

    public static DistrictRequest createBatchAssignDistrictRequest(ApiRequest apiRequest, String provider, String geoProvider,
                                                                   boolean uspsValidate, boolean showMembers, boolean usePunct,
                                                                   boolean skipGeocode, boolean showMaps,
                                                                   String districtStrategy) {
        DistrictRequest districtRequest = new DistrictRequest();
        districtRequest.setApiRequest(apiRequest);
        districtRequest.setProvider(provider);
        districtRequest.setGeoProvider(geoProvider);
        districtRequest.setUsePunct(usePunct);
        districtRequest.setUspsValidate(uspsValidate);
        districtRequest.setShowMembers(showMembers);
        districtRequest.setSkipGeocode(skipGeocode);
        districtRequest.setShowMaps(showMaps);
        districtRequest.setShowMembers(showMembers);
        districtRequest.setDistrictStrategy(districtStrategy);
        districtRequest.setRequestTime(new Timestamp(new Date().getTime()));
        return districtRequest;
    }

    public static DistrictRequest createBlueBirdDistrictRequest(ApiRequest apiRequest, String provider,
                                                                String geoProvider, boolean usePunct,
                                                                Address address, Point point) {
        DistrictRequest districtRequest = new DistrictRequest();
        districtRequest.setApiRequest(apiRequest);
        districtRequest.setProvider(provider);
        districtRequest.setGeoProvider(geoProvider);
        districtRequest.setPoint(point);
        districtRequest.setUsePunct(usePunct);
        districtRequest.setRequestTime(new Timestamp(new Date().getTime()));
        districtRequest.setAddress(address);
        districtRequest.setShowMaps(false);
        districtRequest.setShowMembers(false);
        districtRequest.setUspsValidate(true);
        districtRequest.setSkipGeocode(false);
        return districtRequest;
    }

    public static DistrictRequest createBatchBlueBirdDistrictRequest(ApiRequest apiRequest, String provider,
                                                                     String geoProvider, boolean usePunct) {
        DistrictRequest districtRequest = new DistrictRequest();
        districtRequest.setApiRequest(apiRequest);
        districtRequest.setProvider(provider);
        districtRequest.setGeoProvider(geoProvider);
        districtRequest.setUsePunct(usePunct);
        districtRequest.setRequestTime(new Timestamp(new Date().getTime()));
        districtRequest.setShowMaps(false);
        districtRequest.setShowMembers(false);
        districtRequest.setUspsValidate(true);
        districtRequest.setSkipGeocode(false);
        return districtRequest;
    }

    public static DistrictRequest createFullDistrictRequest(ApiRequest apiRequest, Address address, Point point,
                                                            String provider, String geoProvider, boolean uspsValidate,
                                                            boolean showMembers, boolean usePunct, boolean skipGeocode,
                                                            boolean showMaps, String districtStrategy) {

        DistrictRequest districtRequest = new DistrictRequest();
        districtRequest.setApiRequest(apiRequest);
        districtRequest.setAddress(address);
        districtRequest.setPoint(point);
        districtRequest.setProvider(provider);
        districtRequest.setGeoProvider(geoProvider);
        districtRequest.setShowMembers(showMembers);
        districtRequest.setShowMaps(showMaps);
        districtRequest.setUspsValidate(uspsValidate);
        districtRequest.setUsePunct(usePunct);
        districtRequest.setSkipGeocode(skipGeocode);
        districtRequest.setRequestTime(new Timestamp(new Date().getTime()));
        districtRequest.setDistrictStrategy(districtStrategy);

        return districtRequest;
    }

    public static DistrictRequest createFullIntersectRequest(ApiRequest apiRequest, DistrictType sourceType, String sourceId,
                                                             DistrictType intersectType) {

        DistrictRequest districtRequest = new DistrictRequest();
        districtRequest.setApiRequest(apiRequest);
        districtRequest.setRequestTime(new Timestamp(new Date().getTime()));
        districtRequest.setDistrictType(sourceType);
        districtRequest.setDistrictId(sourceId);
        districtRequest.setIntersectType(intersectType);
        districtRequest.setShowMembers(true);
        districtRequest.setShowMaps(true);
        return districtRequest;
    }

    public static DistrictedAddress consolidateDistrictedAddress(List<DistrictedAddress> results) {
        if (results.isEmpty()) {
            return new DistrictedAddress();
        }
        GeocodedAddress geoAddr = results.size() == 1 ? results.get(0).getGeocodedAddress() : null;
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
    public static DistrictInfo consolidateDistrictInfo(List<DistrictInfo> districtInfoList) {
        if (districtInfoList.isEmpty()) {
            return null;
        }
        DistrictInfo baseDist = districtInfoList.get(0);
        for (DistrictType distType : DistrictType.values()) {
            String baseCode = baseDist.getDistCode(distType);
            List<String> codes = districtInfoList.stream().map(info -> info.getDistCode(distType)).toList();
            if (codes.stream().anyMatch(code -> !isValidDistCode(code) || !baseCode.equals(code))) {
                baseDist.setDistCode(distType, null);
            }
        }

        if (baseDist.hasDistrictCode(SENATE)) {
            return baseDist;
        }
        else {
            return null;
        }
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
