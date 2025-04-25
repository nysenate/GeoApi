package gov.nysenate.sage.provider;

import com.google.common.collect.ArrayListMultimap;
import gov.nysenate.sage.model.PostOfficeCache;
import gov.nysenate.sage.model.PostOfficeData;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.address.GeocodedPostOfficeBox;
import gov.nysenate.sage.model.result.BaseResult;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.provider.district.LocalSource;
import gov.nysenate.sage.provider.geocode.Geocoder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static gov.nysenate.sage.controller.api.DistrictUtil.consolidateResultsWithoutConflicts;
import static gov.nysenate.sage.model.result.ResultStatus.*;

public final class PostOfficeCacheManager {
    private static final List<PostOfficeCache<?, ?>> caches = new ArrayList<>();

    private PostOfficeCacheManager() {}

    public static PostOfficeCache<Geocoder, GeocodeResult> getGeocodeCache() {
        var tempCache = new PostOfficeCache<>(new GeocodeResult(null, MISSING_GEOCODED_ADDRESS),
                PostOfficeCacheManager::getGeocodeData);
        caches.add(tempCache);
        return tempCache;
    }

    public static PostOfficeCache<LocalSource, DistrictResult> getDistrictCache() {
        var tempCache = new PostOfficeCache<>(new DistrictResult(null, null), PostOfficeCacheManager::getDistrictData);
        caches.add(tempCache);
        return tempCache;
    }

    public static void clearCaches() {
        for (var cache : caches) {
            cache.clear();
        }
    }

    private static PostOfficeData<GeocodeResult> getGeocodeData(List<GeocodeResult> possibleResults) {
        var tempMap = new HashMap<String, List<GeocodedAddress>>();
        List<GeocodedAddress> allGeocodedAddresses = possibleResults.stream().filter(BaseResult::isSuccess)
                .map(GeocodeResult::getGeocodedAddress).toList();
        for (GeocodedAddress geoAddr : allGeocodedAddresses) {
            String postalCity = geoAddr.getAddress().getPostalCity();
            List<GeocodedAddress> currGeoAddrs = tempMap.computeIfAbsent(postalCity, k -> new ArrayList<>());
            currGeoAddrs.add(geoAddr);
        }

        var postalCityMap = new HashMap<String, GeocodeResult>();
        for (var entry : tempMap.entrySet()) {
            postalCityMap.put(entry.getKey(), toGeocodeResult(entry.getValue()));
        }

        return new PostOfficeData<>(postalCityMap, toGeocodeResult(allGeocodedAddresses));
    }

    private static GeocodeResult toGeocodeResult(List<GeocodedAddress> postOffices) {
        if (postOffices.isEmpty()) {
            return new GeocodeResult(null, NON_NY_STATE);
        }
        final Geocoder firstGeocoder = postOffices.get(0).getGeocode().originalGeocoder();
        boolean hasCommonGeocoder = postOffices.stream().map(geoAddr -> geoAddr.getGeocode().originalGeocoder())
                .allMatch(firstGeocoder::equals);
        var postalGeoAddr = new GeocodedPostOfficeBox(postOffices);
        return new GeocodeResult(hasCommonGeocoder ? firstGeocoder : null, SUCCESS, postalGeoAddr);
    }

    private static PostOfficeData<DistrictResult> getDistrictData(List<DistrictResult> possibleResults) {
        Map<String, DistrictResult> dataMap = new HashMap<>();
        // A town may have multiple Post Offices.
        ArrayListMultimap<String, DistrictResult> postalCityToResultMultimap = ArrayListMultimap.create();
        for (DistrictResult result : possibleResults) {
            postalCityToResultMultimap.put(result.getAddress().getPostalCity(), result);
        }
        for (String postalCity : postalCityToResultMultimap.keySet()) {
            dataMap.put(postalCity, consolidateResultsWithoutConflicts(postalCityToResultMultimap.get(postalCity)));
        }

        return new PostOfficeData<>(dataMap, consolidateResultsWithoutConflicts(possibleResults));
    }
}
