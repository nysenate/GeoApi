package gov.nysenate.sage.model;

import com.google.common.collect.ArrayListMultimap;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.result.BaseResult;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.model.result.GeocodeResult;
import org.apache.tomcat.util.collections.CaseInsensitiveKeyMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static gov.nysenate.sage.controller.api.DistrictUtil.consolidateResultsWithoutConflicts;

/**
 * Stores data about Post Office addresses in a single zipcode.
 */
public class PostOfficeData<T> {
    private final CaseInsensitiveKeyMap<T> townToDataMap = new CaseInsensitiveKeyMap<>();
    private final T consolidatedData;

    private PostOfficeData(Map<String, T> townToDataMap, T consolidatedData) {
        // The internal Map does not allow null keys, which have no meaning anyway.
        for (Map.Entry<String, T> entry : townToDataMap.entrySet()) {
            if (entry.getKey() != null) {
                this.townToDataMap.put(entry.getKey(), entry.getValue());
            }
        }
        this.consolidatedData = consolidatedData;
    }

    public static PostOfficeData<List<GeocodedAddress>> getGeocodeData(List<GeocodeResult> possibleResults) {
        var postalCityMap = new HashMap<String, List<GeocodedAddress>>();
        List<GeocodedAddress> allGeocodedAddresses = possibleResults.stream().filter(BaseResult::isSuccess)
                .map(GeocodeResult::getGeocodedAddress).toList();
        for (GeocodedAddress geoAddr : allGeocodedAddresses) {
            String postalCity = geoAddr.getAddress().getPostalCity();
            List<GeocodedAddress> currGeoAddrs = postalCityMap.computeIfAbsent(postalCity, k -> new ArrayList<>());
            currGeoAddrs.add(geoAddr);
        }

        return new PostOfficeData<>(postalCityMap, allGeocodedAddresses);
    }

    public static PostOfficeData<DistrictResult> getDistrictData(List<DistrictResult> possibleResults) {
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

    /**
     * Attempts to match the postal city. Otherwise, just returns the consolidated data.
     */
    public T getData(String postalCity) {
        if (postalCity == null) {
            return consolidatedData;
        }
        return townToDataMap.getOrDefault(postalCity, consolidatedData);
    }
}
