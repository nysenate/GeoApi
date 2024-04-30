package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.model.district.County;
import gov.nysenate.sage.scripts.streetfinder.model.StreetParity;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileDataExtractor;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileLineType;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import static gov.nysenate.sage.model.district.DistrictType.*;

public class EssexParser extends CountyParser {
    public EssexParser(File file, County county) {
        super(file, county);
    }

    @Override
    protected StreetfileDataExtractor getDataExtractor() {
        return super.getDataExtractor()
                .addSplitTest(lineParts -> lineParts.get(9).matches("M"), StreetfileLineType.SKIP)
                .addBuildingIndices(3, 4, 5).addStreetIndices(1)
                .addType(TOWN_CITY, 0).addType(ELECTION, 2).addType(ASSEMBLY, 6)
                .addTypesInOrder(CONGRESSIONAL, SENATE, ZIP);
    }

    /**
     * This file has a strange way of showing parity.
     */
    @Override
    protected List<String> parseLine(String line) {
        var dataList = new LinkedList<>(super.parseLine(line));
        // Two parts must be combined into something usable.
        String firstPart = dataList.get(5);
        String secondPart = dataList.remove(6);
        StreetParity parity;
        if (firstPart.equals("-1") && secondPart.equals("0")) {
            parity = StreetParity.ODDS;
        } else if (firstPart.equals("0") && secondPart.equals("-1")) {
            parity = StreetParity.EVENS;
        } else {
            parity = StreetParity.ALL;
        }
        dataList.set(5, parity.name());
        return dataList;
    }
}
