package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.model.StreetParity;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileDataExtractor;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import static gov.nysenate.sage.model.district.DistrictType.*;

// TODO: skip "M" for zip
public class EssexParser extends BaseParser {
    public EssexParser(File file) {
        super(file);
    }

    @Override
    protected StreetfileDataExtractor getDataExtractor() {
        return new StreetfileDataExtractor(EssexParser.class.getSimpleName())
                .addBuildingIndices(3, 4, 5).addStreetIndices(1)
                .addType(TOWN, 0).addType(ELECTION, 2).addType(ASSEMBLY, 6)
                .addTypesInOrder(CONGRESSIONAL, SENATE, ZIP);
    }


    /**
     * This file has a strange way of showing parity.
     */
    @Override
    protected void addData(int lineNum, String... dataFields) {
        var dataList = new LinkedList<>(List.of(dataFields));
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
        super.addData(lineNum, dataList);
    }
}
