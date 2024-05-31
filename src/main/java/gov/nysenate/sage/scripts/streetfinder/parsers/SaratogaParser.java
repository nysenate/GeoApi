package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.dao.provider.district.MunicipalityType;
import gov.nysenate.sage.model.district.County;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileDataExtractor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static gov.nysenate.sage.model.district.DistrictType.*;

/**
 * Parses files similar to Saratoga County.
 * Main difference is spacing, and the town name being at the top of each data segment.
 */
// TODO: may have town_code instead of town, or it may not have town at all
public class SaratogaParser extends NTSParser {
    private final Pattern invalidLine = Pattern.compile("Ward ,|Segments {4}|r_strtdd|Ward Ward");
    private String town;

    public SaratogaParser(File file, Map<MunicipalityType, Map<String, Integer>> typeAndNameToIdMap, County county) {
        super(file, typeAndNameToIdMap, county);
    }

    @Override
    protected StreetfileDataExtractor getDataExtractor() {
        return super.getDataExtractor()
                .addBuildingIndices(4, 5, 6).addStreetIndices(0, 1).addType(ZIP, 2).addType(TOWN_CITY, 3)
                .addType(WARD, 8).addTypesInOrder(ELECTION, CONGRESSIONAL, SENATE, ASSEMBLY,
                        SCHOOL, VILLAGE, CLEG, FIRE, CITY_COUNCIL);
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
        for (String groupStr : new String[] {"street", "suffix", "zip"}) {
            cleanedData.add(dataMatcher.group(groupStr));
        }
        cleanedData.add(town);
        cleanedData.addAll(cleanAfterZip(dataMatcher.group("afterZip").split(" ")));
        return cleanedData;
    }
}
