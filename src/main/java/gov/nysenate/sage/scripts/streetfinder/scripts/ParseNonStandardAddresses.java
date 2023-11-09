package gov.nysenate.sage.scripts.streetfinder.scripts;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseNonStandardAddresses {
    private static final Pattern bldgNumPattern = Pattern.compile("(\\d*)(.+)"),
            streetSuffixPattern = Pattern.compile("\\d+.+ (ST|AVE|RD|DR) .*");
    private static final String outputFilename = "parsedAddresses.csv";

    public static void main(String[] args) throws IOException {
        var filePath = Path.of(args[0]);
        var scanner = new Scanner(filePath);
        var strSet = new LinkedHashSet<String>();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim().toUpperCase();
            Matcher matcher = bldgNumPattern.matcher(line);
            if (matcher.matches()) {
                strSet.add(matcher.group(1) + "\t" + matcher.group(2));
            }
        }

        long matchCount = strSet.stream()
                .filter(line -> line.matches(streetSuffixPattern.pattern())).count();
        System.out.printf("%d%% of the lines matched.%n", 100 * matchCount / strSet.size());
        Files.write(Path.of(filePath.getParent() + "/" + outputFilename), strSet);
    }
}
