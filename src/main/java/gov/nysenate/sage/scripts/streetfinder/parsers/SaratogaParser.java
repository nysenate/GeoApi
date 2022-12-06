package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.model.address.StreetFinderAddress;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static gov.nysenate.sage.model.address.StreetFileField.*;

/**
 * Parses files similar to Saratoga County. Uses very similar logic to NTSParser but is separated for
 * simplicity of NTSParser. The largest difference is in the spacing and the town name being at the top of each
 * Data segment
 */
// TODO: may have towncode instead of town, or it may not have town at all
public class SaratogaParser extends NTSParser {
    private final Pattern invalidLine = Pattern.compile("Ward ,|Segments {4}|r_strtdd|Ward Ward");
    private String town;

    /**
     * Calls super method to set up tsv file
     * @param file
     * @throws IOException
     */
    public SaratogaParser(String file) throws IOException {
        super(file);
    }

    @Override
    protected List<BiConsumer<StreetFinderAddress, String>> getFunctions() {
        List<BiConsumer<StreetFinderAddress, String>> funcList = new ArrayList<>();
        funcList.add(StreetFinderAddress::setStreet);
        funcList.add(StreetFinderAddress::setStreetSuffix);
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
