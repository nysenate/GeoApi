package gov.nysenate.sage.scripts.streetfinder.model;

import gov.nysenate.sage.scripts.streetfinder.scripts.utils.Intern;
import gov.nysenate.sage.util.Pair;

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

    public static BuildingRange getBuildingRange(List<String> buildingData) {
        int highIndex = buildingData.size() == 1 ? 0 : 1;
        Pair<Integer> nums = getBuildingNums(buildingData.get(0), buildingData.get(highIndex));
        if (buildingData.size() < 3) {
            return new BuildingRange(nums.first(), nums.second());
        }
        if (buildingData.size() == 3) {
            StreetParity parity = StreetParity.getParityFromWord(buildingData.get(2));
            return new BuildingRange(nums.first(), nums.second(), parity);
        }
        throw new RuntimeException("Can't parse " + buildingData.size() + " building fields.");
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
    private static Pair<Integer> getBuildingNums(String data1, String data2) {
        final String replacePattern = "(?i)[A-Z]|\\d/\\d";
        data1 = data1.replaceAll(replacePattern, "").trim();
        data2 = data2.replaceAll(replacePattern, "").trim();
        // Extra care is needed with Queens addresses.
        if (data1.contains("-") || data2.contains("-")) {
            String[] data1Ar = data1.split("-");
            String[] data2Ar = data2.split("-");
            if (data1Ar.length != 2 || data2Ar.length != 2
                    || data1Ar[0].length() > data2Ar[0].length()
                    || data1Ar[1].length() > data2Ar[1].length()) {
                throw new NumberFormatException("Unclear how to parse %s to %s".formatted(data1, data2));
            }
            if (data1Ar[1].length() < data2Ar[1].length()) {
                boolean isEven = Integer.parseInt(data2Ar[1])%2 == 0;
                int newNum = (int) (Math.pow(10, data1Ar[1].length()) - (isEven ? 2 : 1));
                System.err.printf("Losing some data on the range: %s to %s%n", data1, data2);
                data2Ar[1] = Integer.toString(newNum);
            }
            data1 = data1Ar[0] + data1Ar[1];
            data2 = data2Ar[0] + data2Ar[1];
        }
        return new Pair<>(Integer.parseInt(data1), Integer.parseInt(data2));
    }
}
