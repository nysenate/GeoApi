package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.model.StreetFileAddress;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static gov.nysenate.sage.scripts.streetfinder.model.StreetFileField.*;

/**
 * Parses NYC Street files.
 */
public class NYCParser extends BasicParser {
    private static final String skippableLine = "^$|_{10,}|FROM TO ED AD ZIP CD SD MC CO",
            streetName = "\\d{1,2} (AVENUE|PRECINCT).*|^\\D.+",
            data = "(?<buildingRange>(\\d{1,4}([A-Z]|-\\d{2})? ){2})?\\d{3} \\d{2}(?<zip> \\d{5})?( \\d{2}){4}";
    private final String town;

    public NYCParser(String filename) throws IOException {
        super(filename);
        this.town = getTown();
    }

    /**
     * In these files, the street names are followed by a list of data points for that street.
     */
    public void parseFile() throws IOException {
        var scanner = new Scanner(new File(filename));
        String currStreet = "";
        while (scanner.hasNext()) {
            String originalLine = scanner.nextLine().trim();
            String line = originalLine.replaceAll("\\s+", " ");
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
                dataList.add(4, "ALL");
                if (dataMatcher.group("zip") == null) {
                    System.out.println("Warning: this line has no zipcode: " + originalLine);
                    dataList.add(7, "");
                }
                parseLine(dataList);
            }
            else if (line.matches(streetName)) {
                currStreet = line;
            }
            else if (!line.matches(skippableLine)) {
                System.err.println("Could not parse line: " + originalLine);
            }
        }
        scanner.close();
        closeWriters();
    }

    @Override
    protected List<BiConsumer<StreetFileAddress, String>> getFunctions() {
        List<BiConsumer<StreetFileAddress, String>> functions = new ArrayList<>();
        functions.addAll(functions(TOWN, STREET));
        functions.addAll(buildingFunctions);
        functions.addAll(functions(ELECTION_CODE, ASSEMBLY, ZIP, CONGRESSIONAL, SENATE));
        // The data in this spot is the municipal court.
        functions.add(skip);
        functions.add(function(CITY_COUNCIL));
        return functions;
    }

    /**
     * @return the town of the current file.
     */
    private String getTown() {
        if (filename.contains("Bronx")) {
            return "BRONX";
        } else if(filename.contains("Brooklyn")) {
            return "BROOKLYN";
        } else if(filename.contains("Manhattan")) {
            return "MANHATTAN";
        } else if(filename.contains("Queens")) {
            return "QUEENS";
        } else {
            return "STATEN ISLAND";
        }
    }
}
