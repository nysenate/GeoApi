package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.util.AddressDictionary;
import gov.nysenate.sage.util.Pair;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class NonStandardAddressParser {
    private static final String collegeBoxes = "((ALUMNI|COLONIAL|DUTCH|EMPIRE|FREEDOM|INDIAN|STATE) (QUAD |COMMONS )?)?BO?X",
            aptNumRegex = String.join("|", AddressDictionary.unitMap.keySet()) + "|" + collegeBoxes;
    private static final Pattern
            addressPattern = Pattern.compile("(?i)(?<bldgNum>[\\d /]+) (?<preDir>[NSEW]{1,2} )?(?<street>" + streetRegex() + ")(?<apt>.*), \\d{5}"),
    // TODO: check nums
            aptPattern = Pattern.compile("(%s) \\d+|".formatted(aptNumRegex) + String.join("|", AddressDictionary.unitNoNumMap.keySet()));
    private static final String outputFilename = "parsed-addresses.csv";

    public static void main(String[] args) throws IOException {
        // TODO: exclude PO boxes specifically
        int count = 0;
        var filePath = Path.of(args[0]);
        var scanner = new Scanner(filePath);
        var uniqueLines = new HashSet<String>();
        int totalSize = 0;
        while (scanner.hasNextLine()) {
            uniqueLines.add(scanner.nextLine());
            totalSize++;
        }
        var parsedAddresses = new ArrayList<String>();
        var aptTypeCountMap = new HashMap<String, Integer>();

        for (String line : uniqueLines) {
            line = line.replaceAll(" {2,}", " ").trim();
            Matcher matcher = addressPattern.matcher(line);
            if (matcher.matches()) {
                var aptData = new ArrayList<Pair<String>>();
                String apt = matcher.group("apt");
                if (apt.isBlank()) {
                    System.err.println(line);
                }
                Matcher aptMatcher = aptPattern.matcher(apt);
                while (aptMatcher.find()) {
                    String[] aptSplit = aptMatcher.group().split(" ");
                    String aptNum = aptSplit.length == 1 ? "" : aptSplit[1];
                    aptData.add(new Pair<>(aptSplit[0], aptNum));
                }
                parsedAddresses.add("");
            }
            else {
                //System.err.println(line);
            }
        }
        // TODO: CU,?
       // Files.write(Path.of(filePath.getParent() + "/" + outputFilename), parsedAddresses);
        System.out.println(100 * uniqueLines.size()/totalSize + "% of lines are unique");
        System.out.println("Parsed " + 100 * parsedAddresses.size()/uniqueLines.size() + "% of unique lines\n");

        aptTypeCountMap.entrySet().stream().sorted(Comparator.comparingInt(Map.Entry::getValue))
                .map(entry -> entry.getKey() + ": " + entry.getValue()).forEach(System.out::println);
        System.out.println(aptTypeCountMap.values().stream().reduce(Integer::sum).orElse(0) + " total bad apartment types");
    }

    private static String streetRegex() {
        var suffixSet = new TreeSet<>(AddressDictionary.streetTypeMap.keySet());
        var bigRoadList = new ArrayList<String>();
        for (var highwayEntry : AddressDictionary.highWayMap.entrySet()) {
            if (highwayEntry.getValue().matches(".*(Rte|Hwy)")) {
                bigRoadList.add(highwayEntry.getKey());
            }
        }
        return "BROADWAY|.+ (%s)|(%s) \\d+[A-Z]{0,1}".formatted(String.join("|", suffixSet), String.join("|", bigRoadList));
    }

    private static void printCondensedMap(Map<String, String> inputMap) {
        var ret = new TreeMap<String, SortedSet<String>>();
        for (var entry : inputMap.entrySet()) {
            if (!ret.containsKey(entry.getValue())) {
                ret.put(entry.getValue(), new TreeSet<>());
            }
            ret.get(entry.getValue()).add(entry.getKey());
        }
        var curr = new StringBuilder();
        for (var entry : ret.entrySet()) {
            String strValues = entry.getValue().stream().map("\"%s\""::formatted).collect(Collectors.joining(", "));
            if (!curr.isEmpty()) {
                curr.append(" ");
            }
            curr.append("\"%s\", List.of(%s),".formatted(entry.getKey(), strValues));
            if (curr.length() >= 60) {
                System.out.println(curr);
                curr = new StringBuilder();
            }
        }
        System.out.println();
    }
}
