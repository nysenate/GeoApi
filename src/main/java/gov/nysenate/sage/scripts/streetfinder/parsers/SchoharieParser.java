package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.model.address.SchoharieStreetFinderAddress;
import gov.nysenate.sage.model.address.StreetFinderAddress;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import static gov.nysenate.sage.model.address.StreetFileField.*;

/**
 * Parses Schohaire County.txt file
 * Looks for street name, town, low, high, range type, sch, townCode, vill
 */
public class SchoharieParser extends BaseParser<SchoharieStreetFinderAddress> {
    public SchoharieParser(String file) throws IOException {
        super(file);
    }

    @Override
    protected SchoharieStreetFinderAddress getNewAddress() {
        return new SchoharieStreetFinderAddress();
    }

    @Override
    protected List<BiConsumer<SchoharieStreetFinderAddress, String>> getFunctions() {
        List<BiConsumer<SchoharieStreetFinderAddress, String>> functions = new ArrayList<>();
        functions.add(function(STREET));
        functions.addAll(buildingFunctions);
        functions.add(function(TOWN));
        // Possibly the ward
        functions.addAll(skip(1));
        functions.addAll(functions(ELECTION_CODE, CONGRESSIONAL, SENATE, ASSEMBLY, SCHOOL));
        // Possibly the cleg and city, respectively
        functions.addAll(skip(2));
        functions.add(function(VILLAGE));
        // Possibly fire after this
        return functions;
    }

    // TODO ADD Zip code parsing once we get a streetfile with zipcodes included
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
    protected void writeToFile(SchoharieStreetFinderAddress streetFinderAddress) {
        if (streetFinderAddress.hasSenateDistrict()) {
            super.writeToFile(streetFinderAddress);
        }
    }
}
