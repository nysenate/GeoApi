package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.model.SchoharieStreetFileAddressRange;
import gov.nysenate.sage.scripts.streetfinder.model.StreetFileFunctionList;

import java.io.File;
import java.util.List;

import static gov.nysenate.sage.scripts.streetfinder.model.StreetFileField.*;

/**
 * Parses Schoharie County.txt file. Note that this file has no zipcodes.
 */
public class SchoharieParser extends BaseParser<SchoharieStreetFileAddressRange> {
    public SchoharieParser(File file) {
        super(file);
    }

    @Override
    protected SchoharieStreetFileAddressRange getNewAddress() {
        return new SchoharieStreetFileAddressRange();
    }

    @Override
    protected StreetFileFunctionList<SchoharieStreetFileAddressRange> getFunctions() {
        return new StreetFileFunctionList<SchoharieStreetFileAddressRange>()
                .addFunctions(false, STREET).addFunctions(buildingFunctions)
                .addFunctions(false, TOWN, WARD, ELECTION_CODE, CONGRESSIONAL,
                        SENATE, ASSEMBLY, SCHOOL, CLEG, CITY, VILLAGE, FIRE);
    }

    @Override
    protected void parseLine(String line) {
        if (line.contains("House Range") && line.contains("This conflicts")
                && line.contains("Ricks Test") && line.contains("Dist")) {
            return;
        }
        String[] lineSplit = line.split(",", 3);
        String buildingData = lineSplit[1];
        if (buildingData.isEmpty() || buildingData.contains("House Range")) {
            return;
        }
        // Puts building data into the proper CSV format.
        line = String.join("," , List.of(lineSplit[0],
                buildingData.replaceAll("-", ","), lineSplit[2]));
        super.parseLine(line);
    }

    @Override
    protected void finalize(SchoharieStreetFileAddressRange range) {
        if (range.hasSenateDistrict()) {
            super.finalize(range);
        }
    }
}
