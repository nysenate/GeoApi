package gov.nysenate.sage.scripts.streetfinder.parsers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// TODO: WIP for quick testing
public class NonStandardAddressParser {
    private static final String outputFilename = "parsed-addresses.csv";
    private static final Map<Integer, Integer> otherZipMap = new HashMap<>();

    public static void main(String[] args) throws IOException {
        var filePath = Path.of(args[0]);
        var scanner = new Scanner(filePath);
        var uniqueLines = new HashSet<String>();
        int totalSize = 0;
        var addressTypeMap = new EnumMap<NonStandardAddressType, Integer>(NonStandardAddressType.class);
        while (scanner.hasNextLine()) {
            uniqueLines.add(scanner.nextLine().replaceAll(" {2,}", " ").trim());
            totalSize++;
        }
        for (String line : uniqueLines) {
            var matcher = Pattern.compile("(?<data>.*), (?<zip>\\d{5})$").matcher(line);
            matcher.matches();
            var nsa = new NonStandardAddress(matcher.group("data"), matcher.group("zip"));
            if (nsa.type() == NonStandardAddressType.OTHER) {
                otherZipMap.merge(Integer.parseInt(matcher.group("zip")), 1, Integer::sum);
            }
            addressTypeMap.merge(nsa.type(), 1, Integer::sum);
        }
//        Files.write(Path.of(filePath.getParent() + "/" + outputFilename), parsedAddresses);
        System.out.println(100 * uniqueLines.size()/totalSize + "% of lines are unique");
        System.out.println("Unique line count: " + uniqueLines.size());
        for (var entry : otherZipMap.entrySet()) {
            if (entry.getValue() >= 40) {
                System.out.println(entry);
            }
        }
        System.out.println(addressTypeMap);
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
