package gov.nysenate.sage.scripts.streetfinder.parsers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class PNSA2 {
    private static final Pattern addressPattern = Pattern.compile("(\\d*)\\s*(.+)\\s+(\\d{5})");
    private static final String outputFilename = "Final-Parsed-Addresses.csv";

    public static void main(String[] args) throws IOException {
        var filePath = Path.of(args[0]);
        var scanner = new Scanner(filePath);
        var strSet = new LinkedHashSet<String>();

        var routeMatcher = Pattern.compile("ROUTE\\s*(\\d+)").matcher("");

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim().toUpperCase();
            Matcher matcher = addressPattern.matcher(line);
            if (matcher.find()) {
                String buildingNum = matcher.group(1);
                String street = matcher.group(2);
                String zipcode = matcher.group(3);
                street = street.replace(",", "");

                // Update the routeMatcher input for each line
                routeMatcher.reset(street);


                if (routeMatcher.find()) {
                    buildingNum = routeMatcher.group(1);
                    street = street.replaceFirst("ROUTE\\s*\\d+", "").trim();
                } else {
                    street = removeSuffix(street, "DR ", "PL "," ST ","ROUTE ","AVE ","LN ","BLVD ","HGTS ","RD ","APT ","TPKE");
                }

                // Add a check for null before accessing the groups
                if (!buildingNum.isEmpty() && street != null /*&& zipcode != null*/) {
                    strSet.add(buildingNum + "\t" + street + "\t" /*+ zipcode*/);
                }
            }
        }


        Files.write(Path.of(filePath.getParent() + "/" + outputFilename), strSet);

        // Calculate the number of lines in the output file
        int lineCount = strSet.size();

        // Print the number of lines to the console
        System.out.println("Number of lines in the output file: " + lineCount);
    }
    private static String removeSuffix(String street, String... suffixes) {
        for (String suffix : suffixes) {
            int index = street.indexOf(suffix);
            if (index != -1) {
                // Remove the substring after the suffix
                street = street.substring(0, index + suffix.length()).trim();
                break;
            }
        }
        return street;
    }
}
