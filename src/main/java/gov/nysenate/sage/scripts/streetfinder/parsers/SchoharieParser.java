package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.model.SchoharieStreetFileAddress;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static gov.nysenate.sage.scripts.streetfinder.model.StreetFileField.*;

/**
 * Parses Schoharie County.txt file. Note that this file has no zipcodes.
 */
public class SchoharieParser extends BaseParser<SchoharieStreetFileAddress> {
    public SchoharieParser(File file) {
        super(file);
    }

    @Override
    protected SchoharieStreetFileAddress getNewAddress() {
        return new SchoharieStreetFileAddress();
    }

    @Override
    protected List<BiConsumer<SchoharieStreetFileAddress, String>> getFunctions() {
        List<BiConsumer<SchoharieStreetFileAddress, String>> functions = new ArrayList<>();
        functions.add(function(STREET));
        functions.addAll(buildingFunctions);
        functions.add(function(TOWN));
        // Possibly the ward
        functions.add(skip);
        functions.addAll(functions(ELECTION_CODE, CONGRESSIONAL, SENATE, ASSEMBLY, SCHOOL));
        // Possibly the cleg and city, respectively
        functions.addAll(skip(2));
        functions.add(function(VILLAGE));
        // Possibly fire after this
        return functions;
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
    protected void finalize(SchoharieStreetFileAddress sfa) {
        if (sfa.hasSenateDistrict()) {
            super.finalize(sfa);
        }
    }
}
