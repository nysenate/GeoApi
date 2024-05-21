package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.model.district.County;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileDataExtractor;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileLineType;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static gov.nysenate.sage.model.district.DistrictType.*;

/**
 * Has some strange entries: there are no zipcode for things like islands, nature areas, metro stops, and bridges.
 * Also, tons of lines are identifiable buildings, but without building numbers.
 */
public class NYCParser extends CountyParser {
    private static final String streetNameRegex = "(\\d{1,4} |9/11 )?\\D.+";
    private final String mailCity;
    private String currStreet;

    public NYCParser(File file, County county) {
        super(file, county);
        this.mailCity = switch (county.name()) {
            case "Queens" -> "";
            case "Kings", "Richmond" -> county.streetfileName();
            default -> county.name();
        };
    }

    @Override
    protected StreetfileDataExtractor getDataExtractor() {
        // There are initially 9 parts, but the postalCity and street are added in.
        return super.getDataExtractor().addIsProperLengthFunction(11)
                .addTest(this::skipLine, StreetfileLineType.SKIP)
                .addPostalCityIndex(0).addStreetIndices(1)
                .addBuildingIndices(2, 3).addType(ELECTION, 4)
                .addTypesInOrder(ASSEMBLY, ZIP, CONGRESSIONAL, SENATE, MUNICIPAL_COURT, CITY_COUNCIL);
    }

    @Override
    protected String delim() {
        return " ";
    }

    @Override
    protected List<String> parseLine(String line) {
        String[] lineParts = line.replaceAll("\\s+", " ").split(" ");
        List<String> dataList = new ArrayList<>(List.of(mailCity, currStreet));
        Collections.addAll(dataList, lineParts);
        return dataList;
    }

    private boolean skipLine(String line) {
        line = line.replaceAll("\\s+", " ").trim();
        // In these files, the street names are followed by a list of data points for that street.
        if (line.matches(streetNameRegex)) {
            currStreet = line;
            return true;
        }
        return false;
    }
}
