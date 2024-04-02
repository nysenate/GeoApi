package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileDataExtractor;

import java.io.File;
import java.util.List;
import java.util.Map;

import static gov.nysenate.sage.model.district.DistrictType.*;

// Validation seems to do better without a city name since it is sometimes incorrect.
// TODO: use OID, or subtract 1
public class AddressPointsParser extends BaseParser {
    private final Map<String, Integer> fipsCodeMap;

    public AddressPointsParser(File file, Map<String, Integer> fipsCodeMap) {
        super(file);
        this.fipsCodeMap = fipsCodeMap;
    }

    @Override
    protected StreetfileDataExtractor getDataExtractor() {
        return new StreetfileDataExtractor(AddressPointsParser.class.getSimpleName())
                .addBuildingIndices(1, 2, 3).addStreetIndices(13).addType(ZIP, 8)
                .addType(SENATE, 24).addTypesInOrder(ASSEMBLY, CONGRESSIONAL)
                .addCountyFunction(16, countyStr -> fipsCodeMap.get(countyStr.toLowerCase()));
    }

    @Override
    protected void addData(int lineNum, List<String> dataFields) {
        if (dataFields.get(7).equals("NY") &&
                dataFields.get(9).matches("[123]") &&
                dataFields.get(14).equalsIgnoreCase("Active")) {
            super.addData(lineNum, dataFields);
        }
    }

    @Override
    public boolean isRangeData() {
        return false;
    }
}
