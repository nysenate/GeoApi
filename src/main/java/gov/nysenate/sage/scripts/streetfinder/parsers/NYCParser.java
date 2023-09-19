package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.model.StreetFileAddress;
import gov.nysenate.sage.scripts.streetfinder.model.StreetFileFunctionList;
import gov.nysenate.sage.scripts.streetfinder.model.StreetParity;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static gov.nysenate.sage.scripts.streetfinder.model.StreetFileField.*;

/**
 * Parses NYC Street files.
 */
public class NYCParser extends BasicParser {
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
    public List<StreetFileAddress> parseFile() throws IOException {
        var scanner = new Scanner(file);
        String currStreet = "";
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim().replaceAll("\\s+", " ");
            if (line.matches(skippableLine)) {
                continue;
            }
            Matcher dataMatcher = Pattern.compile(data).matcher(line);
            if (dataMatcher.matches()) {
                LinkedList<String> dataList = new LinkedList<>(List.of(line.split(" ")));
                dataList.add(0, town);
                dataList.add(1, currStreet);
                // Adds in building data
                if (dataMatcher.group("buildingRange") == null) {
                    dataList.add(2, "");
                    dataList.add(3, "");
                }
                dataList.add(4, StreetParity.ALL.name());
                if (dataMatcher.group("zip") == null) {
                    continue;
                }
                parseData(dataList);
            }
            else if (line.matches(streetNameRegex)) {
                currStreet = line;
            }
            else {
                putBadLine(currStreet, line);
            }
        }
        scanner.close();
        endProcessing();
        return addresses;
    }

    @Override
    protected StreetFileFunctionList<StreetFileAddress> getFunctions() {
        return new StreetFileFunctionList<>().addFunctions(false, TOWN, STREET)
                .addFunctions(buildingFunctions)
                .addFunctions(false, ELECTION_CODE, ASSEMBLY, ZIP, CONGRESSIONAL, SENATE)
                .skip(1).addFunctions(false, CITY_COUNCIL);
    }

    private String getTown() {
        Matcher townMatcher = townPattern.matcher(file.getName());
        return townMatcher.find() ? townMatcher.group().toUpperCase() : "STATEN ISLAND";
    }
}
