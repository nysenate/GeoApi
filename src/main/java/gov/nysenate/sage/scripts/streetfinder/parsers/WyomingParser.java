package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.model.StreetFileAddress;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static gov.nysenate.sage.scripts.streetfinder.model.StreetFileField.*;

/**
 * Parses Wyoming County 2018 .txt
 */
public class WyomingParser extends BasicParser {
    public WyomingParser(File file) {
        super(file);
    }

    @Override
    protected List<BiConsumer<StreetFileAddress, String>> getFunctions() {
        // TODO: The 0 and 1 indices might be the town and district, respectively
        List<BiConsumer<StreetFileAddress, String>> functions = new ArrayList<>(skip(2));
        functions.addAll(streetParts(4));
        functions.addAll(buildingFunctions);
        functions.addAll(functions(TOWN, ZIP));
        functions.add(handlePrecinct);
        functions.addAll(functions(true, CONGRESSIONAL, SENATE, ASSEMBLY));
        return functions;
    }

    protected void parseLine(String line) {
        if (line.contains("ODD_EVEN")) {
            return;
        }
        super.parseLine(line);
    }
}
