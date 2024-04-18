package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileDataExtractor;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileLineData;

import java.io.File;
import java.util.Map;

import static gov.nysenate.sage.model.district.DistrictType.*;

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
                .addBuildingIndices(1, 2, 3).addStreetIndices(13).addPostalCityIndex(6).addType(ZIP, 8)
                .addType(SENATE, 24).addTypesInOrder(ASSEMBLY, CONGRESSIONAL)
                .addCountyFunction(16, countyStr -> fipsCodeMap.get(countyStr.toLowerCase()));
    }

    @Override
    protected StreetfileLineData getData(int lineNum, String... dataFields) {
        // TODO: need to add length checks everywhere
        if (dataFields.length != 27) {
            //System.out.println(String.join("\t", dataFields));
            return null;
        }
        if (dataFields[7].equals("NY") &&
                dataFields[9].matches("[123]") &&
                dataFields[14].equalsIgnoreCase("Active")) {
            return super.getData(lineNum, dataFields);
        }
        return null;
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
