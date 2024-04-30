package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.model.district.County;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileDataExtractor;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileLineType;

import java.io.File;
import java.util.List;

import static gov.nysenate.sage.model.district.DistrictType.*;

/**
 * Parses Schoharie County.txt file. Note that this file has no zipcodes.
 */
public class SchoharieParser extends CountyParser {
    public SchoharieParser(File file, County county) {
        super(file, county);
    }

    @Override
    protected StreetfileDataExtractor getDataExtractor() {
        return super.getDataExtractor().addTest(SchoharieParser::isNotProper, StreetfileLineType.SKIP)
                .addBuildingIndices(1, 2, 3).addStreetIndices(0)
                .addType(TOWN_CITY, 4).addTypesInOrder(WARD, ELECTION, CONGRESSIONAL, SENATE, ASSEMBLY,
                        SCHOOL, CLEG, VILLAGE, FIRE);
    }

    private static boolean isNotProper(String line) {
        return line.contains("House Range") || line.contains("This conflicts")
                || line.contains("Ricks Test") || line.contains("Dist");
    }


    @Override
    protected List<String> parseLine(String line) {
        String[] lineSplit = line.split(",", 3);
        // Puts building data into the proper CSV format.
        line = String.join("," , List.of(lineSplit[0],
                lineSplit[1].replaceAll("-", ","), lineSplit[2]));
        return super.parseLine(line);
    }
}
