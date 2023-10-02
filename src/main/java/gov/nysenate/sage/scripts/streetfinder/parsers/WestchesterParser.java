package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.model.StreetFileAddressRange;
import gov.nysenate.sage.scripts.streetfinder.model.StreetFileFunctionList;

import java.io.File;

import static gov.nysenate.sage.scripts.streetfinder.model.StreetFileField.*;

/**
 * Parses Westchester County 2018 file
 * Looks for town, pre-Direction, street, street suffix, post-direction, low, high, range type, zip, skips CNG, sen, asm, dist
 */
public class WestchesterParser extends BasicParser {

    public WestchesterParser(File file) {
        super(file);
    }

    @Override
    protected StreetFileFunctionList<StreetFileAddressRange> getFunctions() {
        return new StreetFileFunctionList<>().addFunctions(false, TOWN)
                .addFunction(BaseParser::handlePrecinct).addStreetParts(4)
                .addFunctions(buildingFunctions).addFunctions(false, ZIP)
                .addFunctions(true, CONGRESSIONAL, SENATE, ASSEMBLY, CLEG);
    }
}
