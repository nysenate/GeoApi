package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.model.StreetFileAddress;
import gov.nysenate.sage.scripts.streetfinder.model.StreetFileFunctionList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
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
    protected StreetFileFunctionList<StreetFileAddress> getFunctions() {
        return new StreetFileFunctionList<>().addFunctions(false, STREET)
                .addFunction(StreetFileAddress::setStreetSuffix)
                .addFunctions(false, ZIP, TOWN)
                .addFunctions(buildingFunctions)
                .addFunctions(false, BOE_TOWN_CODE, WARD, ELECTION_CODE, CONGRESSIONAL, SENATE,
                        ASSEMBLY, SCHOOL, VILLAGE, CLEG, FIRE, CITY_COUNCIL, CITY);
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
        for (String groupStr : new String[]{"street", "suffix", "zip"}) {
            cleanedData.add(dataMatcher.group(groupStr));
        }
        cleanedData.add(town);
        cleanedData.addAll(cleanAfterZip(dataMatcher.group("afterZip").split(" ")));
        return cleanedData;
    }
}
