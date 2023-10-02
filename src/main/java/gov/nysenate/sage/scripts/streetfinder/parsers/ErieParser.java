package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.model.StreetFileAddressRange;
import gov.nysenate.sage.scripts.streetfinder.model.StreetFileFunctionList;

import java.io.File;

import static gov.nysenate.sage.scripts.streetfinder.model.StreetFileField.*;

public class ErieParser extends BasicParser {
    public ErieParser(File file) {
        super(file);
    }

    @Override
    protected StreetFileFunctionList<StreetFileAddressRange> getFunctions() {
        return new StreetFileFunctionList<>().addStreetParts(1).addFunctions(buildingFunctions)
                .addFunctions(false, ZIP, TOWN).skip(3).addFunction(BaseParser::handlePrecinct)
                .addFunctions(true, SENATE, ASSEMBLY, COUNTY_ID, CONGRESSIONAL);
    }
}
