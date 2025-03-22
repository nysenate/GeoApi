package gov.nysenate.sage.model;

import com.google.common.collect.ArrayListMultimap;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.model.result.ResultStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static gov.nysenate.sage.controller.api.DistrictUtil.consolidateResultsWithoutConflicts;

/**
 * Stores data about Post Office addresses in a single zipcode.
 */
public class PostOfficeData<T> {
    private final Map<String, T> townToDataMap;
    private final T consolidatedData;

    private PostOfficeData(Map<String, T> townToDataMap, T consolidatedData) {
        this.townToDataMap = townToDataMap;
        this.consolidatedData = consolidatedData;
    }

    public static PostOfficeData<DistrictResult> getDistrictData(List<DistrictResult> possibleResults) {
        Map<String, DistrictResult> dataMap = new HashMap<>();
        // A town may have multiple Post Offices.
        ArrayListMultimap<String, DistrictResult> postalCityToResultMultimap = ArrayListMultimap.create();
        for (DistrictResult result : possibleResults) {
            postalCityToResultMultimap.put(result.getAddress().getPostalCity().toUpperCase(), result);
        }
        for (String postalCity : postalCityToResultMultimap.keySet()) {
            dataMap.put(postalCity, consolidateResultsWithoutConflicts(postalCityToResultMultimap.get(postalCity)));
        }

        return new PostOfficeData<>(dataMap, consolidateResultsWithoutConflicts(possibleResults));
    }

    public static PostOfficeData<GeocodeResult> getGeocodeData(List<GeocodeResult> possibleResults) {
        // There's no real way to consolidate Geocodes.
        final var multipleResult = new GeocodeResult(null, ResultStatus.MULTIPLE_POST_OFFICES);
        Map<String, GeocodeResult> dataMap = new HashMap<>();
        for (GeocodeResult result : possibleResults) {
            String postalCity = result.getGeocodedAddress().getAddress().getPostalCity().toUpperCase();
            dataMap.merge(postalCity, result, (oldResult, newResult) -> multipleResult);
        }

        return new PostOfficeData<>(dataMap, possibleResults.size() == 1 ? possibleResults.get(0) : multipleResult);
    }

    /**
     * Attempts to match the postal city. Otherwise, just returns the consolidated data.
     */
    public T getData(String postalCity) {
        if (postalCity == null) {
            return consolidatedData;
        }
        return townToDataMap.getOrDefault(postalCity.toUpperCase(), consolidatedData);
    }
}
