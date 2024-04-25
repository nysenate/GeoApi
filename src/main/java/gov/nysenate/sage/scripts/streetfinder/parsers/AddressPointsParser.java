package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileDataExtractor;

import java.io.File;
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
        return new StreetfileDataExtractor(AddressPointsParser.class.getSimpleName()).addIsProperLengthFunction(27)
                .addIsProperFunction(lineParts -> lineParts.get(7).equals("NY") &&
                        lineParts.get(9).matches("[123]") &&
                        lineParts.get(14).equalsIgnoreCase("Active"))
                .addIdFunction((lineParts, lineNum) -> Long.valueOf(lineParts.get(0)))
                .addBuildingIndices(1, 2, 3).addStreetIndices(13).addPostalCityIndex(6).addType(ZIP, 8)
                .addType(SENATE, 24).addTypesInOrder(ASSEMBLY, CONGRESSIONAL)
                .addCountyFunction(16, countyStr -> fipsCodeMap.get(countyStr.toLowerCase()));
    }

    @Override
    protected String delim() {
        return "\t";
    }

    @Override
    public boolean isRangeData() {
        return false;
    }
}
