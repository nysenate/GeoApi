package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.model.StreetFileAddress;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static gov.nysenate.sage.scripts.streetfinder.model.StreetFileField.*;

/**
 * Parses Westchester County 2018 file
 * Looks for town, pre-Direction, street, street suffix, post-direction, low, high, range type, zip, skips CNG, sen, asm, dist
 */
public class WestchesterParser extends BasicParser {

    /**
     * Calls the super constructor which sets up the tsv file
     * @param file
     */
    public WestchesterParser(File file) {
        super(file);
    }

    @Override
    protected List<BiConsumer<StreetFileAddress, String>> getFunctions() {
        List<BiConsumer<StreetFileAddress, String>> functions = new ArrayList<>();
        functions.add(function(TOWN));
        functions.add(handlePrecinct);
        functions.addAll(streetParts(4));
        functions.addAll(buildingFunctions);
        functions.add(function(ZIP));
        functions.addAll(functions(true, CONGRESSIONAL, SENATE, ASSEMBLY, CLEG));
        return functions;
    }
}
