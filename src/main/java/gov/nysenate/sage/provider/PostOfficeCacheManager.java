package gov.nysenate.sage.provider;

import com.google.common.collect.Multimap;
import gov.nysenate.sage.model.PostOfficeCache;
import gov.nysenate.sage.model.PostOfficeData;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.address.GeocodedPostOfficeBox;
import gov.nysenate.sage.model.result.BaseResult;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.provider.district.LocalSource;
import gov.nysenate.sage.provider.geocode.Geocoder;

import java.util.*;

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
        var tempCache = new PostOfficeCache<>(new DistrictResult(null, INVALID_ADDRESS), PostOfficeCacheManager::getDistrictData);
        caches.add(tempCache);
        return tempCache;
    }

    public static void clearCaches() {
        for (var cache : caches) {
            cache.clear();
        }
    }

    private static PostOfficeData<GeocodeResult> getGeocodeData(Multimap<String, GeocodeResult> postalCityToResults) {
        var postalCityMap = new HashMap<String, GeocodeResult>();
        for (String postalCity : postalCityToResults.keySet()) {
            postalCityMap.put(postalCity, toGeocodeResult(postalCityToResults.get(postalCity)));
        }

        return new PostOfficeData<>(postalCityMap, toGeocodeResult(postalCityToResults.values()));
    }

    private static GeocodeResult toGeocodeResult(Collection<GeocodeResult> geocodeResults) {
        List<GeocodedAddress> postOffices = geocodeResults.stream().filter(BaseResult::isSuccess)
                .map(GeocodeResult::getGeocodedAddress).toList();
        if (postOffices.isEmpty()) {
            return new GeocodeResult(null, NON_NY_STATE);
        }
        final Geocoder firstGeocoder = postOffices.get(0).getGeocode().originalGeocoder();
        boolean hasCommonGeocoder = postOffices.stream().map(geoAddr -> geoAddr.getGeocode().originalGeocoder())
                .allMatch(firstGeocoder::equals);
        var postalGeoAddr = new GeocodedPostOfficeBox(postOffices);
        return new GeocodeResult(hasCommonGeocoder ? firstGeocoder : null, SUCCESS, postalGeoAddr);
    }

    private static PostOfficeData<DistrictResult> getDistrictData(Multimap<String, DistrictResult> postalCityToResults) {
        Map<String, DistrictResult> dataMap = new HashMap<>();
        for (String postalCity : postalCityToResults.keySet()) {
            dataMap.put(postalCity, consolidateResultsWithoutConflicts(postalCityToResults.get(postalCity)));
        }

        return new PostOfficeData<>(dataMap, consolidateResultsWithoutConflicts(postalCityToResults.values()));
    }
}
