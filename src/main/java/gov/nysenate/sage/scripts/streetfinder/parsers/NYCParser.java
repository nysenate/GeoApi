package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.model.district.County;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.BasicLineType;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileDataExtractor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static gov.nysenate.sage.model.district.DistrictType.*;

/**
 * Parses NYC Street files.
 */
public class NYCParser extends CountyParser {
    private static final String skippableLine = "^$|_{10,}|FROM TO ED AD ZIP CD SD MC CO",
            streetNameRegex = "(\\d{1,4} |9/11 )?\\D.+",
            // There are no zipcode for things like islands, nature areas, metro stops, and bridges.
            data = "(?<buildingRange>(\\d{1,5}([A-Z]|-\\d{2,3}[A-Z]?)? ){2})?\\d{3} \\d{2}(?<zip> \\d{5})?( \\d{2}){4}";

    private final String mailCity;
    private String currStreet;

    public NYCParser(File file, County county, String mailCity) {
        super(file, county);
        this.mailCity = mailCity;
    }

    @Override
    protected StreetfileDataExtractor getDataExtractor() {
        // There are initially 9 parts, but the postalCity and street are added in.
        return super.getDataExtractor().addIsProperLengthFunction(11)
                .addPostalCityIndex(0).addStreetIndices(1)
                .addBuildingIndices(2, 3).addType(ELECTION, 4)
                .addTypesInOrder(ASSEMBLY, ZIP, CONGRESSIONAL, SENATE, MUNICIPAL_COURT, CITY_COUNCIL);
    }

    @Override
    protected String delim() {
        return " ";
    }

    /**
     * In these files, the street names are followed by a list of data points for that street.
     */
    @Override
    protected List<String> parseLine(String line) {
        line = line.replaceAll("\\s+", " ");
        if (line.matches(skippableLine)) {
            return null;
        }
        Matcher dataMatcher = Pattern.compile(data).matcher(line);
        if (dataMatcher.matches()) {
            List<String> dataList = new ArrayList<>(List.of(mailCity, currStreet));
            Collections.addAll(dataList, line.split(" "));
            return dataList;
        }
        else if (line.matches(streetNameRegex)) {
            currStreet = line;
        }
        else {
            improperLineMap.put(BasicLineType.ERROR, "street: %s, line: %s".formatted(currStreet, line));
        }
        return null;
    }
}
