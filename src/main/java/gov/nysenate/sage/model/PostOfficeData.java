package gov.nysenate.sage.model;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import gov.nysenate.sage.controller.api.DistrictUtil;
import gov.nysenate.sage.model.address.DistrictedAddress;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictMatchLevel;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.provider.district.DistrictSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static PostOfficeData<DistrictResult> getPostOfficeDistrictData(List<DistrictResult> possibleResults) {
        Map<String, DistrictResult> dataMap = new HashMap<>();
        // A town may have multiple Post Offices.
        Multimap<String, DistrictResult> postalCityToResultMultimap = ArrayListMultimap.create();
        for (DistrictResult result : possibleResults) {
            postalCityToResultMultimap.put(result.getDistrictedAddress().getAddress().getPostalCity().toUpperCase(), result);
        }
        for (var entry : postalCityToResultMultimap.asMap().entrySet()) {
            DistrictInfo consolidatedInfo = DistrictUtil.getDistrictInfoWithoutConflicts(
                    entry.getValue().stream().map(result -> result.getDistrictedAddress().getDistrictInfo()).toList());
            DistrictMatchLevel consolidatedMatchLevel = DistrictMatchLevel.getMin(
                    entry.getValue().stream().map(result -> result.getDistrictedAddress().getDistrictMatchLevel()).toList()
            );
            List<DistrictSource> sources  = entry.getValue().stream().map(result -> ((DistrictSource) result.getSource())).toList();
            DistrictSource source = sources.size() == 1 ? sources.get(0) : DistrictSource.STREETFILE_AND_SHAPEFILE;
            DistrictResult result = new DistrictResult(source, null);
            result.setDistrictedAddress(new DistrictedAddress(null, consolidatedInfo, consolidatedMatchLevel));
            dataMap.put(entry.getKey(), result);
        }

        // TODO: full consolidation. Perhaps only valid results?
        return new PostOfficeData<>(dataMap, null);
    }

    public static PostOfficeData<GeocodeResult> getPostOfficeGeocodeData(List<GeocodeResult> possibleResults) {
        // There's no real way to consolidate Geocodes
        final var multipleResult = new GeocodeResult(null, ResultStatus.MULTIPLE_POST_OFFICES);
        Map<String, GeocodeResult> dataMap = new HashMap<>();
        for (GeocodeResult result : possibleResults) {
            String postalCity = result.getGeocodedAddress().getAddress().getPostalCity().toUpperCase();
            if (!dataMap.containsKey(postalCity)) {
                dataMap.put(postalCity, result);
            }
            else {
                dataMap.put(postalCity, multipleResult);
            }
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
