package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.model.StreetFileAddress;
import gov.nysenate.sage.scripts.streetfinder.model.StreetFileFunctionList;
import gov.nysenate.sage.scripts.streetfinder.model.StreetParity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static gov.nysenate.sage.scripts.streetfinder.model.StreetFileField.*;

public class EssexParser extends BasicParser {
    public EssexParser(File file) {
        super(file);
    }

    @Override
    protected StreetFileFunctionList<StreetFileAddress> getFunctions() {
        return new StreetFileFunctionList<>().addFunctions(false, TOWN).
                addFunction(BaseParser::setStreetAndSuffix).addFunctions(false, ELECTION_CODE)
                .addFunctions(buildingFunctions).addFunctions(false, ASSEMBLY, CONGRESSIONAL, SENATE)
                .addFunction(EssexParser::skipM);
    }


    private static void skipM(StreetFileAddress sfa, String str) {
        if (!str.equals("M")) {
            sfa.put(ZIP, str);
        }
    }

    /**
     * This type has a strange way of showing parity.
     */
    @Override
    protected void parseLine(String line) {
        List<String> split = new ArrayList<>(List.of(line.split(",")));
        // Two parts must be combined into something usable.
        String firstPart = split.get(5);
        String secondPart = split.remove(6);
        StreetParity parity;
        if (firstPart.equals("-1") && secondPart.equals("0")) {
            parity = StreetParity.ODDS;
        } else if (firstPart.equals("0") && secondPart.equals("-1")) {
            parity = StreetParity.EVENS;
        } else {
            parity = StreetParity.ALL;
        }
        split.set(5, parity.name());
        parseData(split);
    }
}
