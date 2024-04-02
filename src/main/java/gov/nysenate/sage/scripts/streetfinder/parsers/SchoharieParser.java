package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.model.district.County;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileDataExtractor;

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
        return super.getDataExtractor()
                .addBuildingIndices(1, 2, 3).addStreetIndices(0)
                .addType(TOWN, 4).addTypesInOrder(WARD, ELECTION, CONGRESSIONAL, SENATE, ASSEMBLY,
                        SCHOOL, CLEG, CITY, VILLAGE, FIRE);
    }

    @Override
    protected String[] parseLine(String line) {
        if (line.contains("House Range") && line.contains("This conflicts")
                && line.contains("Ricks Test") && line.contains("Dist")) {
            return null;
        }
        String[] lineSplit = line.split(",", 3);
        String buildingData = lineSplit[1];
        if (buildingData.isEmpty() || buildingData.contains("House Range")) {
            return null;
        }
        // Puts building data into the proper CSV format.
        line = String.join("," , List.of(lineSplit[0],
                buildingData.replaceAll("-", ","), lineSplit[2]));
        return super.parseLine(line);
    }
}
