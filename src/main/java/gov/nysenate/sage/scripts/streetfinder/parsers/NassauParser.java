package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.model.StreetFileAddress;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static gov.nysenate.sage.scripts.streetfinder.model.StreetFileField.*;

/**
 * Parses Nassau County csv file and outputs a tsv file
 */
public class NassauParser extends BasicParser {
    public NassauParser(String file) throws IOException {
        super(file);
    }

    @Override
    protected List<BiConsumer<StreetFileAddress, String>> getFunctions() {
        List<BiConsumer<StreetFileAddress, String>> functions = new ArrayList<>();
        functions.add(handlePrecinct);
        functions.add(StreetFileAddress::setStreet);
        functions.add(NassauParser::setSuffix);
        functions.addAll(functions(TOWN, ZIP));
        functions.addAll(buildingFunctions);
        functions.add(skip);
        functions.addAll(functions(true, CONGRESSIONAL, SENATE, ASSEMBLY, CLEG));
        return functions;
    }

    private static void setSuffix(StreetFileAddress streetFileAddress, String data) {
        String streetSuffix = data.replace(streetFileAddress.getStreet(), " ").trim();
        String[] splitString = streetSuffix.split("\\s+");
        int suffixIndex = 0;
        if (splitString.length > 1) {
            if (checkForDirection(splitString[1])) {
                streetFileAddress.setPostDirection(splitString[1]);
            } else if (checkForDirection(splitString[0])) {
                streetFileAddress.setStreet(splitString[0] + streetFileAddress.getStreet());
                suffixIndex = 1;
            }
        }
        streetFileAddress.setStreetSuffix(splitString[suffixIndex]);
    }
}
