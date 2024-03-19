package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileDataExtractor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static gov.nysenate.sage.model.district.DistrictType.*;

/**
 * Parses NYC Street files.
 */
public class NYCParser extends BaseParser {
    private static final String skippableLine = "^$|_{10,}|FROM TO ED AD ZIP CD SD MC CO",
            streetNameRegex = "(\\d{1,4} |9/11 )?\\D.+",
    // There is no zipcode for things like islands, nature areas, metro stops, and bridges.
            data = "(?<buildingRange>(\\d{1,5}([A-Z]|-\\d{2,3}[A-Z]?)? ){2})?\\d{3} \\d{2}(?<zip> \\d{5})?( \\d{2}){4}";
    private static final Pattern townPattern = Pattern.compile("(?i)Bronx|Brooklyn|Manhattan|Queens");

    private final String town;

    public NYCParser(File file) {
        super(file);
        this.town = getTown();
    }

    /**
     * In these files, the street names are followed by a list of data points for that street.
     */
    @Override
    public void parseFile() throws IOException {
        var scanner = new Scanner(file);
        String currStreet = "";
        int lineNum = 0;
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim().replaceAll("\\s+", " ");
            if (line.matches(skippableLine)) {
                continue;
            }
            Matcher dataMatcher = Pattern.compile(data).matcher(line);
            if (dataMatcher.matches()) {
                if (dataMatcher.group("zip") == null) {
                    continue;
                }
                List<String> dataList = new ArrayList<>(List.of(town, currStreet));
                String range = dataMatcher.group("buildingRange");
                // Adds empty building range
                if (range == null) {
                    dataList.add("");
                    dataList.add("");
                }
                Collections.addAll(dataList, line.split(" "));
                addData(lineNum, dataList);
            }
            else if (line.matches(streetNameRegex)) {
                currStreet = line;
            }
            else {
                badLines.put(currStreet, line);
            }
            lineNum++;
        }
        scanner.close();
    }

    @Override
    protected StreetfileDataExtractor getDataExtractor() {
        // TODO: 9 might be municipal court?
        return new StreetfileDataExtractor(NYCParser.class.getSimpleName())
                .addBuildingIndices(2, 3).addStreetIndices(1)
                .addType(TOWN, 0).addType(ELECTION, 4)
                .addTypesInOrder(ASSEMBLY, ZIP, CONGRESSIONAL, SENATE).addType(CITY_COUNCIL, 10);
    }

    private String getTown() {
        Matcher townMatcher = townPattern.matcher(file.getName());
        return townMatcher.find() ? townMatcher.group() : "Staten Island";
    }
}
