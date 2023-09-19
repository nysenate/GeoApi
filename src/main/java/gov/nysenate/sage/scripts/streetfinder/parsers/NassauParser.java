package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.model.StreetFileAddress;
import gov.nysenate.sage.scripts.streetfinder.model.StreetFileFunctionList;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import static gov.nysenate.sage.scripts.streetfinder.model.StreetFileField.*;

public class NassauParser extends BasicParser {
    public NassauParser(File file) {
        super(file);
    }

    /**
     * A pre-direction and/or post-direction may need to be inserted.
     */
    @Override
    protected void parseLine(String line) {
        String[] commaSplit = line.split(",", 4);
        LinkedList<String> streetList = new LinkedList<>(List.of(commaSplit[2].split("\\s+")));
        if (isNotDirection(streetList.getFirst())) {
            streetList.push("");
        }
        if (isNotDirection(streetList.getLast())) {
            streetList.add("");
        }
        if (streetList.size() != 3) {
            System.err.println("Problem with line: " + line);;
        }
        else {
            streetList.add(1, commaSplit[1]);
            String finalStreet = String.join(",", streetList);
            parseData(commaSplit[0], finalStreet, commaSplit[3]);
        }
    }

    @Override
    protected StreetFileFunctionList<StreetFileAddress> getFunctions() {
        return new StreetFileFunctionList<>().addFunction(BaseParser::handlePrecinct)
                .addStreetParts(4).addFunctions(false, TOWN, ZIP)
                .addFunctions(buildingFunctions).skip(1)
                .addFunctions(true, CONGRESSIONAL, SENATE, ASSEMBLY, CLEG);
    }
}
