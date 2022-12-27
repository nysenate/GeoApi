package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.model.StreetFileAddress;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static gov.nysenate.sage.scripts.streetfinder.model.StreetFileField.*;

/**
 * Parses files similar to Saratoga County.
 * Main difference is spacing, and the town name being at the top of each data segment.
 */
// TODO: may have towncode instead of town, or it may not have town at all
public class SaratogaParser extends NTSParser {
    private final Pattern invalidLine = Pattern.compile("Ward ,|Segments {4}|r_strtdd|Ward Ward");
    private String town;

    public SaratogaParser(File file) {
        super(file);
    }

    @Override
    protected List<BiConsumer<StreetFileAddress, String>> getFunctions() {
        List<BiConsumer<StreetFileAddress, String>> funcList = new ArrayList<>();
        funcList.add(function(STREET));
        funcList.add(StreetFileAddress::setStreetSuffix);
        funcList.addAll(functions(ZIP, TOWN));
        funcList.addAll(buildingFunctions);
        funcList.addAll(functions(BOE_TOWN_CODE, WARD, ELECTION_CODE, CONGRESSIONAL, SENATE,
                ASSEMBLY, SCHOOL, VILLAGE, CLEG, FIRE, CITY_COUNCIL, CITY));
        return funcList;
    }

    @Override
    protected void parseStartOfPage(String startingLine) {
        startingLine = startingLine.replaceAll("\\s+", " ");
        town = startingLine.split("Street Name")[0].trim();
        super.parseStartOfPage(startingLine);
    }

    @Override
    protected List<String> cleanLine(String line) {
        if (invalidLine.matcher(line).find()) {
            return List.of();
        }
        line = line.replaceAll("\\s+", " ").trim();
        Pattern dataPattern = Pattern.compile("(?<street>.+) (?<suffix>[^ ]+) (?<zip>\\d{5}) (?<afterZip>.+)");
        List<String> cleanedData = new ArrayList<>();
        Matcher dataMatcher = dataPattern.matcher(line);
        if (!dataMatcher.matches()) {
            System.err.println("Error parsing line: " + line);
        }
        cleanedData.add(dataMatcher.group("street"));
        cleanedData.add(dataMatcher.group("suffix"));
        cleanedData.add(dataMatcher.group("zip"));
        cleanedData.add(town);
        cleanedData.addAll(cleanAfterZip(dataMatcher.group("afterZip").split(" ")));
        return cleanedData;
    }
}
