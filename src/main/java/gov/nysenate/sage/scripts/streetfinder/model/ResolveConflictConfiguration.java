package gov.nysenate.sage.scripts.streetfinder.model;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.CompactDistrictMap;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.RangeDistrictData;
import gov.nysenate.sage.util.Tuple;

import java.util.*;

/**
 * Contains data and methods for attempting to resolve conflicts.
 * Note that anything not in the List will have equal and lowest priority.
 */
public class ResolveConflictConfiguration {
    private final List<StreetfileType> sourcePriorityList;
    private final double threshold;

    public ResolveConflictConfiguration(List<StreetfileType> sourcePriorityList, double threshold) {
        this.sourcePriorityList = sourcePriorityList;
        if (threshold <= 0.5 || threshold > 1) {
            throw new IllegalArgumentException("Threshold needs to be in (0.5, 1]");
        }
        this.threshold = threshold;
    }

    /**
     * Districts with a value of 0 are ignored entirely.
     * Otherwise, if there is a conflict for a DistrictType, that type is ignored as well.
     * @return the consolidated district map, and a display String for conflicts.
     */
    public Tuple<CompactDistrictMap, String> consolidationResult(
            RangeDistrictData rdd, final Map<String, StreetfileType> sourceToTypeMap) {
        var typeMap = new HashMap<DistrictType, Short>();
        var conflictTypes = new HashSet<DistrictType>();

        // Extract the district number one type at a time.
        for (DistrictType districtType : DistrictType.values()) {
            Table<StreetfileType, Short, Integer> table = HashBasedTable.create();
            for (int i = 0; i < rdd.getSourceNames().length; i++) {
                short currValue = rdd.getDistrictMaps()[i].get(districtType);
                // A 0 means "no data": it adds no information.
                if (currValue == 0) {
                    continue;
                }
                StreetfileType currStreetfileType = sourceToTypeMap.get(rdd.getSourceNames()[i]);
                Integer currCount = table.get(currStreetfileType, currValue);
                table.put(currStreetfileType, currValue, currCount == null ? 1 : currCount + 1);
            }
            // An
            if (table.isEmpty()) {
                continue;
            }
            final Map<Short, Integer> countMap = getCountMap(table);
            final double total = countMap.values().stream().reduce(0, Integer::sum);
            Optional<Short> districtNumOpt = countMap.keySet().stream()
                    // Due to the constructor constraint, this will have at most 1 element.
                    .dropWhile(s -> countMap.get(s)/total < threshold).findFirst();
            if (districtNumOpt.isPresent()) {
                typeMap.put(districtType, districtNumOpt.get());
            }
            else {
                conflictTypes.add(districtType);
            }
        }
        return new Tuple<>(CompactDistrictMap.getMap(typeMap), rdd.conflictString(conflictTypes));
    }

    /**
     * Returns either the data for the highest priority source, or all data combined.
     */
    private Map<Short, Integer> getCountMap(Table<StreetfileType, Short, Integer> table) {
        for (StreetfileType streetfileType : sourcePriorityList) {
            if (table.containsRow(streetfileType)) {
                return table.row(streetfileType);
            }
        }
        var map = new HashMap<Short, Integer>();
        for (Map<Short, Integer> valueMap : table.rowMap().values()) {
            valueMap.forEach((districtNum, count) -> map.merge(districtNum, count, Integer::sum));
        }
        return map;
    }
}
