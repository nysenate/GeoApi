package gov.nysenate.sage.scripts.streetfinder.model;


import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;

import java.util.*;

/**
 * Stores a range of building numbers and their parity.
 */
public record BuildingRange(int low, int high, StreetParity parity) {
    private static final Range<Integer> emptyRange = Range.closedOpen(-1, -1).canonical(DiscreteDomain.integers());

    public BuildingRange {
        if (low > high) {
            throw new IllegalArgumentException(low + " > " + high);
        }
    }

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

    public static BuildingRange getBuildingRange(List<String> buildingData) {
        if (buildingData.size() == 1) {
            return new BuildingRange(buildingData.get(0));
        }
        else if (buildingData.size() == 2) {
            return new BuildingRange(buildingData.get(0), buildingData.get(1));
        }
        else if (buildingData.size() == 3) {
            return new BuildingRange(buildingData.get(0), buildingData.get(1), buildingData.get(2));
        }
        else {
            throw new RuntimeException("Can't parse " + buildingData.size() + " building fields.");
        }
    }

    /**
     * Combines given BuildingRanges into a canonical Set.
     * E.g. [(1, 5, ODDS), (2, 6, EVENS)] -> {(1, 6, ALL)}
     * Really extends the idea of a RangeSet to use StreetParity.
     */
    public static Set<BuildingRange> combineRanges(Collection<BuildingRange> ranges) {
        var rangeSetMap = new HashMap<StreetParity, Set<Range<Integer>>>();
        List.of(StreetParity.EVENS, StreetParity.ODDS)
                .forEach(parity -> rangeSetMap.put(parity, getParityRangeSet(ranges, parity)));
        // Some ranges will be added as part of BuildingRanges with parity ALL: we'll want to skip these.
        var added = new HashSet<Range<Integer>>();

        var combinedResults = new HashSet<BuildingRange>();
        var evens = rangeSetMap.get(StreetParity.EVENS);
        var odds = rangeSetMap.get(StreetParity.ODDS);
        for (Range<Integer> evenRange : evens) {
            for (Range<Integer> oddRange : odds) {
                // E.g. (3, 7, ODDS) can combine with (2, 6, EVENS) or (4, 8, EVENS)
                if (!added.contains(evenRange) && !added.contains(oddRange) &&
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
            rangeSetMap.get(parity)
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
        Range<Integer> thisCommonParity = this.intRangeWithParity(commonParity),
                otherCommonParity = otherRange.intRangeWithParity(commonParity);
        try {
            Range<Integer> overlap = thisCommonParity.intersection(otherCommonParity);
            return !overlap.isEmpty();
        } catch (IllegalArgumentException ex) {
            return false;
        }
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
        return emptyRange;
    }

    private static BuildingRange get(Range<Integer> range, StreetParity parity) {
        range = range.canonical(DiscreteDomain.integers());
        return new BuildingRange(range.lowerEndpoint(), range.upperEndpoint() - 1, parity);
    }

    private static Set<Range<Integer>> getParityRangeSet(Collection<BuildingRange> ranges, StreetParity parity) {
        if (parity == StreetParity.ALL) {
            throw new IllegalArgumentException("Parity cannot be ALL!");
        }
        var numSet = new TreeSet<Integer>();
        ranges.stream()
                .map(bldgRange -> bldgRange.intRangeWithParity(parity))
                .filter(range -> range != null && !range.isEmpty())
                .forEach(range -> addAllInRange(numSet, range));
        if (numSet.isEmpty()) {
            return Set.of();
        }
        var rangeSet = new HashSet<Range<Integer>>();
        int currLow = numSet.pollFirst(), currHigh = currLow;
        while (!numSet.isEmpty()) {
            if (numSet.first() - currHigh == 2) {
                currHigh = numSet.pollFirst();
            }
            else {
                rangeSet.add(Range.closed(currLow, currHigh));
                currLow = numSet.pollFirst();
                currHigh = currLow;
            }
        }
        rangeSet.add(Range.closed(currLow, currHigh));
        return rangeSet;
    }

    private static void addAllInRange(SortedSet<Integer> set, Range<Integer> range) {
        for (int i = range.lowerEndpoint(); i <= range.upperEndpoint(); i += 2) {
            set.add(i);
        }
    }

    /**
     * Parses a building number from a String.
     * Note that letters and fractions are ignored.
     */
    private static int getBuildingNum(String data) {
        return Integer.parseInt(data.replaceAll("(?i)[A-Z]|-|\\d/\\d", "").trim());
    }
}
