package gov.nysenate.sage.scripts.streetfinder.parsers;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Parses NYC Street files. It does so by using column locations for 3 different data columns within one line
 * Output is a tsv file
 */
public class NYCParser extends NTSParser {
    private static final String underscores = "_".repeat(42);
    private final NYCColumnParser parser1, parser2, parser3;

    /**
     * Calls the super constructor to set up the tsv file.
     * Also sets the town which is the same for the entire file
     * @param file
     * @throws IOException
     */
    public NYCParser(String file) throws IOException {
        super(file);
        String town = getTown();
        this.parser1 = new NYCColumnParser(town);
        this.parser2 = new NYCColumnParser(town);
        this.parser3 = new NYCColumnParser(town);
    }

    /**
     * parses the file by calling parseLine for each line of data. It also sets all the locations of the column location fields when necessary
     * @throws IOException
     */
    public void parseFile() throws IOException {
        var scanner = new Scanner(new File(file));
        var containsPattern = Pattern.compile("Information|Reproduction|STREET FINDER LIST|TOTIN|FROM");
        var matchesPattern = Pattern.compile("Board of Elections|V TE NYC|STATEN ISLAND|STREET FINDER");
        int firstCutIndex = 0, secondCutIndex = 0;

        while (scanner.hasNext()) {
            String currentLine = scanner.nextLine();
            String trimmedLine = currentLine.replaceAll("\\s+", " ").trim();
            if (trimmedLine.split(" ").length <= 1 ||
                    containsPattern.matcher(trimmedLine).find() ||
                    matchesPattern.matcher(trimmedLine).matches()) {}
            //if the line has a series of underscored, then use that to set the column starting locations
            else if (currentLine.contains(underscores)) {
                int start1 = currentLine.indexOf(underscores);
                int start2 = currentLine.indexOf(underscores, start1 + 1);
                int start3 = currentLine.lastIndexOf(underscores);
                // Attempts to cover edge cases by splitting at the average of the end of one column, and the start of another.
                firstCutIndex = (start1 + underscores.length() + start2)/2;
                secondCutIndex = (start2 + underscores.length() + start3)/2;
            }
            // Otherwise, it is a parseable line of data
            else {
                String line1 = substringHelper(currentLine, 0, firstCutIndex);
                parser1.parse(line1).ifPresent(this::writeToFile);
                String line2 = substringHelper(currentLine, firstCutIndex, secondCutIndex);
                parser2.parse(line2).ifPresent(this::writeToFile);
                String line3 = substringHelper(currentLine, secondCutIndex, currentLine.length());
                parser3.parse(line3).ifPresent(this::writeToFile);
            }
        }
        scanner.close();
        closeWriters();
    }

    /**
     * Gets the town by using the file name. All data in the file has the same town
     * @return
     */
    private String getTown() {
        if(file.contains("Bronx")) {
            return "BRONX";
        } else if(file.contains("Brooklyn")) {
            return "BROOKLYN";
        } else if(file.contains("Manhattan")) {
            return "MANHATTAN";
        } else if(file.contains("Queens")) {
            return "QUEENS";
        } else {
            return "STATEN ISLAND";
        }
    }
}
