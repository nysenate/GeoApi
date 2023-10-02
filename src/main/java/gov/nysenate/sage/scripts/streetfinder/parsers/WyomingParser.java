package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.model.StreetFileAddressRange;
import gov.nysenate.sage.scripts.streetfinder.model.StreetFileFunctionList;

import java.io.File;

import static gov.nysenate.sage.scripts.streetfinder.model.StreetFileField.*;

/**
 * Parses Wyoming County 2018 .txt
 */
public class WyomingParser extends BasicParser {
    public WyomingParser(File file) {
        super(file);
    }

    @Override
    protected StreetFileFunctionList<StreetFileAddressRange> getFunctions() {
        // TODO: The 0 and 1 indices might be the town and district, respectively
        return new StreetFileFunctionList<>().skip(2).addStreetParts(4)
                .addFunctions(buildingFunctions).addFunctions(false, TOWN, ZIP)
                .addFunction(BaseParser::handlePrecinct)
                .addFunctions(true, CONGRESSIONAL, SENATE, ASSEMBLY);
    }

    protected void parseLine(String line) {
        if (line.contains("ODD_EVEN")) {
            return;
        }
        super.parseLine(line);
    }
}
