package gov.nysenate.sage.scripts.streetfinder.model;


import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

import java.util.*;

/**
 * Stores a range of building numbers and their parity.
 */
public record BuildingRange(int low, int high, StreetParity parity) {
    public BuildingRange(int low, int high) {
        this(low, high, StreetParity.getParityFromRange(low, high));
    }

    public BuildingRange(String lowBuilding, String highBuilding) {
        this(getBuildingNum(lowBuilding), getBuildingNum(highBuilding));
    }

    public BuildingRange(String lowBuilding, String highBuilding, String parityStr) {
        this(getBuildingNum(lowBuilding), getBuildingNum(highBuilding),
                StreetParity.getParityFromWord(parityStr));
    }

    public BuildingRange(String building) {
        this(building, building);
    }

    public static BuildingRange getBuildingRange(String... buildingData) {
        if (buildingData.length == 1) {
            return new BuildingRange(buildingData[0]);
        }
        else if (buildingData.length == 2) {
            return new BuildingRange(buildingData[0], buildingData[1]);
        }
        else if (buildingData.length == 3) {
            return new BuildingRange(buildingData[0], buildingData[1], buildingData[2]);
        }
        else {
            throw new RuntimeException("Can't parse " + buildingData.length + " building fields.");
        }
    }

    /**
     * Combines given BuildingRanges into a canonical Set.
     * E.g. [(1, 5, ODDS), (2, 6, EVENS)] -> {(1, 6, ALL)}
     * Really extends the idea of a RangeSet to use StreetParity.
     */
    public static Set<BuildingRange> combineRanges(Collection<BuildingRange> ranges) {
        var rangeSetMap = new HashMap<StreetParity, RangeSet<Integer>>();
        List.of(StreetParity.EVENS, StreetParity.ODDS)
                .forEach(parity -> rangeSetMap.put(parity, getParityRangeSet(ranges, parity)));
        // Some ranges will be added as part of BuildingRanges with parity ALL: we'll want to skip these.
        var added = new HashSet<Range<Integer>>();

        var combinedResults = new HashSet<BuildingRange>();
        for (Range<Integer> evenRange : rangeSetMap.get(StreetParity.EVENS).asRanges()) {
            for (Range<Integer> oddRange : rangeSetMap.get(StreetParity.ODDS).asRanges()) {
                // E.g. (3, 7, ODDS) can combine with (2, 6, EVENS) or (4, 8, EVENS)
                if (!added.contains(oddRange) &&
                        Math.abs(evenRange.lowerEndpoint() - oddRange.lowerEndpoint()) == 1 &&
                        Math.abs(evenRange.upperEndpoint() - oddRange.upperEndpoint()) == 1) {
                    Range<Integer> allRange = evenRange.span(oddRange);
                    combinedResults.add(get(allRange, StreetParity.ALL));
                    added.add(evenRange);
                    added.add(oddRange);
                }
            }
        }
        for (StreetParity parity : rangeSetMap.keySet()) {
            rangeSetMap.get(parity).asRanges()
                    .stream().filter(range -> !added.contains(range))
                    .forEach(range -> combinedResults.add(get(range, parity)));
        }
        return combinedResults;
    }

    public Range<Integer> getRange() {
        return Range.closed(low, high);
    }

    public boolean overlaps(BuildingRange otherRange) {
        StreetParity commonParity = StreetParity.commonParity(parity, otherRange.parity);
        if (commonParity == null) {
            return false;
        }
        return this.intRangeWithParity(commonParity).isConnected(otherRange.intRangeWithParity(commonParity));
    }

    private Range<Integer> intRangeWithParity(StreetParity newParity) {
        if (parity == newParity || newParity == StreetParity.ALL) {
            return getRange();
        }
        if (parity == StreetParity.ALL) {
            int mod = newParity == StreetParity.EVENS ? 0 : 1;
            int lower = low;
            int higher = high;
            if (lower%2 != mod) {
                lower++;
            }
            if (higher%2 != mod) {
                higher--;
            }
            if (higher >= lower) {
                return Range.closed(lower, higher);
            }
        }
        return null;
    }

    private static BuildingRange get(Range<Integer> range, StreetParity parity) {
        range = range.canonical(DiscreteDomain.integers());
        return new BuildingRange(range.lowerEndpoint(), range.upperEndpoint() - 1, parity);
    }

    private static RangeSet<Integer> getParityRangeSet(Collection<BuildingRange> ranges, StreetParity parity) {
        if (parity == StreetParity.ALL) {
            throw new IllegalArgumentException("Parity cannot be ALL!");
        }
        RangeSet<Integer> parityRangeSet = TreeRangeSet.create();
        for (BuildingRange bldgRange : ranges) {
            Range<Integer> toAdd = bldgRange.intRangeWithParity(parity);
            if (toAdd != null) {
                parityRangeSet.add(toAdd.canonical(DiscreteDomain.integers()));
            }
        }
        return parityRangeSet;
    }

    /**
     * Parses a building number from a String.
     * Note that letters and fractions are ignored.
     */
    private static int getBuildingNum(String data) {
        return Integer.parseInt(data.replaceAll("(?i)[A-Z]|-|\\d/\\d", "").trim());
    }
}
