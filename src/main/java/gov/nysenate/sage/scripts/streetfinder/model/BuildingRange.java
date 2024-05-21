package gov.nysenate.sage.scripts.streetfinder.model;

import gov.nysenate.sage.scripts.streetfinder.scripts.utils.Intern;

import java.util.*;

/**
 * Stores a range of building numbers and their parity.
 */
public record BuildingRange(int low, int high, StreetParity parity) {
    private static final Intern<BuildingRange> interned = new Intern<>();

    public BuildingRange {
        if (low > high) {
            throw new IllegalArgumentException(low + " > " + high);
        }
        if (low < 0) {
            throw new IllegalArgumentException(low + " is negative");
        }
        if (!parity.matches(low) || !parity.matches(high)) {
            throw new IllegalArgumentException("%d and %d must have parity %s".formatted(low, high, parity.name()));
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
    public static Set<BuildingRange> combineRanges(Collection<Integer> bldgNums) {
        var rangeSetMap = new HashMap<StreetParity, Set<BuildingRange>>();
        // Generates the canonical Sets of even and odd ranges.
        List.of(StreetParity.EVENS, StreetParity.ODDS)
                .forEach(parity -> rangeSetMap.put(parity, getParityRangeSet(bldgNums, parity)));

        var combinedResults = new HashSet<BuildingRange>();
        Iterator<BuildingRange> evensIter = rangeSetMap.get(StreetParity.EVENS).iterator();
        // Combines even and odd ranges when possible.
        while (evensIter.hasNext()) {
            BuildingRange evenRange = evensIter.next();
            Iterator<BuildingRange> oddsIter = rangeSetMap.get(StreetParity.ODDS).iterator();
            while (oddsIter.hasNext()) {
                BuildingRange oddRange = oddsIter.next();
                // E.g. (3, 7, ODDS) can combine with (2, 6, EVENS) or (4, 8, EVENS)
                if (Math.abs(evenRange.low - oddRange.low) == 1 && Math.abs(evenRange.high - oddRange.high) == 1) {
                    combinedResults.add(new BuildingRange(
                            Math.min(evenRange.low, oddRange.low), Math.max(evenRange.high, oddRange.high), StreetParity.ALL
                    ));
                    // Removed from the underlying Sets to prevent duplication.
                    evensIter.remove();
                    oddsIter.remove();
                    break;
                }
            }
        }
        // Remaining even and odd values can be directly added.
        rangeSetMap.values().stream().flatMap(Collection::stream).forEach(combinedResults::add);
        return combinedResults;
    }

    public Set<Integer> allInRange() {
        var set = new HashSet<Integer>();
        for (int i = low; i <= high; i += (parity == StreetParity.ALL ? 1 : 2)) {
            set.add(i);
        }
        return set;
    }

    public BuildingRange intern() {
        return interned.get(this);
    }

    public String rangeString() {
        return low + "-" + high;
    }

    /**
     * A major part of the range consolidation algorithm.
     * @param bldgNums to consolidate.
     * @param parity to pull from ranges.
     * @return a canonical Set of all the ranges with the given parity.
     */
    private static Set<BuildingRange> getParityRangeSet(Collection<Integer> bldgNums, StreetParity parity) {
        if (parity == StreetParity.ALL) {
            throw new IllegalArgumentException("Parity cannot be ALL!");
        }
        // This set will contain all numbers in these ranges with the proper parity.
        var numSet = new TreeSet<Integer>();
        bldgNums.stream().filter(parity::matches).forEach(numSet::add);
        if (numSet.isEmpty()) {
            return Set.of();
        }
        var rangeSet = new HashSet<BuildingRange>();
        int currLow = numSet.pollFirst(), currHigh = currLow;
        while (!numSet.isEmpty()) {
            if (numSet.first() - currHigh == 2) {
                currHigh = numSet.pollFirst();
            }
            else {
                rangeSet.add(new BuildingRange(currLow, currHigh, parity));
                currLow = numSet.pollFirst();
                currHigh = currLow;
            }
        }
        rangeSet.add(new BuildingRange(currLow, currHigh, parity));
        return rangeSet;
    }

    /**
     * Parses a building number from a String.
     * Note that letters and fractions are ignored.
     */
    private static int getBuildingNum(String data) {
        return Integer.parseInt(data.replaceAll("(?i)[A-Z]|-|\\d/\\d", "").trim());
    }
}
