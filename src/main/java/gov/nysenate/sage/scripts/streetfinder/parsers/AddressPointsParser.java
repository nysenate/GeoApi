package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileDataExtractor;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileLineType;

import java.io.File;
import java.util.List;
import java.util.Map;

import static gov.nysenate.sage.model.district.DistrictType.*;

public class AddressPointsParser extends BaseParser {
    private final Map<String, Integer> fipsCodeMap;

    public AddressPointsParser(File file, Map<String, Integer> fipsCodeMap) {
        super(file);
        this.fipsCodeMap = fipsCodeMap;
    }

    @Override
    protected StreetfileDataExtractor getDataExtractor() {
        return super.getDataExtractor().addIsProperLengthFunction(27)
                .addSplitTest(lineParts -> !lineParts.get(7).equals("NY") ||
                        !lineParts.get(9).matches("[123]") ||
                        !lineParts.get(14).equalsIgnoreCase("Active"), StreetfileLineType.SKIP)
                .addIdFunction((lineParts, lineNum) -> Long.valueOf(lineParts.get(0)))
                .addBuildingIndices(2).addStreetIndices(13).addPostalCityIndex(6).addType(ZIP, 8)
                .addType(SENATE, 24).addTypesInOrder(ASSEMBLY, CONGRESSIONAL)
                .addCountyFunction(16, countyStr -> fipsCodeMap.get(countyStr.toLowerCase()));
    }

    @Override
    protected String delim() {
        return "\t";
    }

    @Override
    protected List<String> parseLine(String line) {
        List<String> lineParts = super.parseLine(line);
        lineParts.set(2, lineParts.get(1) + lineParts.get(2));
        lineParts.set(1, "");
        return lineParts;
    }
}