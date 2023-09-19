package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.model.StreetFileAddress;
import gov.nysenate.sage.scripts.streetfinder.model.StreetFileFunctionList;
import gov.nysenate.sage.scripts.streetfinder.model.StreetParity;

import java.io.File;

import static gov.nysenate.sage.scripts.streetfinder.model.StreetFileField.*;

public class VoterFileParser extends BasicParser {
    public VoterFileParser(File file) {
        super(file);
    }

    @Override
    protected void parseLine(String line) {
        super.parseLine(line, "\t");
    }

    @Override
    protected StreetFileFunctionList<StreetFileAddress> getFunctions() {
        return new StreetFileFunctionList<>()
                .addFunction(VoterFileParser::singleAddressBuildingFunction)
                .addStreetParts(3)
                .addFunctions(false, ZIP, COUNTY_ID, ELECTION_CODE, CLEG, WARD, CONGRESSIONAL, SENATE, ASSEMBLY);
    }

    private static void singleAddressBuildingFunction(StreetFileAddress address, String houseNum) {
        address.setBuilding(true, houseNum);
        address.setBuilding(false, houseNum);
        address.setBldgParity(StreetParity.ALL.name());
    }
}
