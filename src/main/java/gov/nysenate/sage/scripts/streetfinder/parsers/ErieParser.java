package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.model.StreetFileAddress;
import gov.nysenate.sage.scripts.streetfinder.model.StreetFileFunctionList;

import java.io.File;

import static gov.nysenate.sage.scripts.streetfinder.model.StreetFileField.*;

public class ErieParser extends BasicParser {
    public ErieParser(File file) {
        super(file);
    }

    @Override
    protected StreetFileFunctionList<StreetFileAddress> getFunctions() {
        return new StreetFileFunctionList<>()
                .addFunction(BaseParser::setStreetAndSuffix).addFunctions(buildingFunctions)
                .addFunctions(false, ZIP, TOWN).skip(3).addFunction(BaseParser::handlePrecinct)
                .addFunctions(true, SENATE, ASSEMBLY, COUNTY_ID, CONGRESSIONAL);
    }
}
