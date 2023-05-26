package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.model.StreetFileAddress;
import gov.nysenate.sage.scripts.streetfinder.model.StreetParity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static gov.nysenate.sage.scripts.streetfinder.model.StreetFileField.*;

public class VoterFileParser extends BasicParser {
    public VoterFileParser(File file) {
        super(file);
    }

    @Override
    protected void parseLine(String line) {
        super.parseLine(line, 0, "\t");
    }

    @Override
    protected List<BiConsumer<StreetFileAddress, String>> getFunctions() {
        List<BiConsumer<StreetFileAddress, String>> functions = new ArrayList<>();
        functions.add(VoterFileParser::singleAddressBuildingFunction);
        functions.addAll(streetParts(3));
        functions.addAll(functions(ZIP, COUNTY_ID, ELECTION_CODE, CLEG, WARD, CONGRESSIONAL, SENATE, ASSEMBLY));
        return functions;
    }

    private static void singleAddressBuildingFunction(StreetFileAddress address, String houseNum) {
        address.setBuilding(true, houseNum);
        address.setBuilding(false, houseNum);
        address.setBldgParity(StreetParity.ALL.name());
    }
}
