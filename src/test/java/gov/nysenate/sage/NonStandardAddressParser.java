package gov.nysenate.sage;

import gov.nysenate.sage.annotation.SillyTest;
import gov.nysenate.sage.scripts.streetfinder.model.College;
import gov.nysenate.sage.scripts.streetfinder.parsers.NonStandardAddress;
import gov.nysenate.sage.scripts.streetfinder.parsers.NonStandardAddressType;
import org.junit.experimental.categories.Category;

import java.io.IOException;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.regex.Pattern;

// For quick testing.
@Category(SillyTest.class)
public class NonStandardAddressParser {
    public static void main(String[] args) throws IOException {
        var filePath = Path.of(args[0]);
        var scanner = new Scanner(filePath);
        var uniqueLines = new HashSet<String>();
        int totalSize = 0;
        var addressTypeMap = new EnumMap<NonStandardAddressType, Integer>(NonStandardAddressType.class);
        var collegeMap = new HashMap<College, Integer>();
        while (scanner.hasNextLine()) {
            uniqueLines.add(scanner.nextLine().replaceAll(" {2,}", " ").trim());
            totalSize++;
        }
        for (String line : uniqueLines) {
            var matcher = Pattern.compile("(?<data>.*), (?<zip>\\d{5})$").matcher(line);
            matcher.matches();
            var nsa = new NonStandardAddress(matcher.group("data"), matcher.group("zip"));
            addressTypeMap.merge(nsa.type(), 1, Integer::sum);
            collegeMap.merge(nsa.getCollege(), 1, Integer::sum);
        }
        System.out.println(100 * uniqueLines.size()/totalSize + "% of lines are unique");
        System.out.println("Unique line count: " + uniqueLines.size());
        System.out.println(addressTypeMap);
        System.out.println(collegeMap);
    }
}
