package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.model.StreetFileAddress;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

import static gov.nysenate.sage.scripts.streetfinder.model.StreetFileField.*;

/**
 * Parses Nassau County csv file and outputs a tsv file
 */
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
            super.parseLine(List.of(commaSplit[0], finalStreet, commaSplit[3]));
        }
    }

    @Override
    protected List<BiConsumer<StreetFileAddress, String>> getFunctions() {
        List<BiConsumer<StreetFileAddress, String>> functions = new ArrayList<>();
        functions.add(handlePrecinct);
        functions.addAll(streetParts(4));
        functions.addAll(functions(TOWN, ZIP));
        functions.addAll(buildingFunctions);
        functions.add(skip);
        functions.addAll(functions(true, CONGRESSIONAL, SENATE, ASSEMBLY, CLEG));
        return functions;
    }
}
