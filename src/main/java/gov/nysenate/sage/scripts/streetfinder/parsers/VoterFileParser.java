package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileDataExtractor;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileLineData;

import java.io.File;
import java.util.Map;

import static gov.nysenate.sage.model.district.DistrictType.*;

public class VoterFileParser extends BaseParser {
    private final Map<Integer, Integer> fipsCodeMap;

    public VoterFileParser(File file, Map<Integer, Integer> fipsCodeMap) {
        super(file);
        this.fipsCodeMap = fipsCodeMap;
    }

    @Override
    protected StreetfileDataExtractor getDataExtractor() {
        // TODO: add towncity?
        return new StreetfileDataExtractor(VoterFileParser.class.getSimpleName())
                .addBuildingIndices(4).addStreetIndices(6, 7, 8).addPostalCityIndex(12).addType(ZIP, 13)
                .addType(COUNTY, 23).addTypesInOrder(ELECTION, CLEG).addType(WARD, 27)
                .addTypesInOrder(CONGRESSIONAL, SENATE, ASSEMBLY)
                .addCountyFunction(23, countyStr -> fipsCodeMap.get(Integer.parseInt(countyStr)))
                .addIdFunction((lineParts, lineNum) -> Long.parseLong(lineParts.get(45).replaceFirst("^NY", "")));
    }

    // TODO: better document improper addresses
    @Override
    protected StreetfileLineData getData(int lineNum, String... dataFields) {
        if (dataFields[41].matches("[IP]")) {
            return null;
        }
        boolean emptyStandardAddress = dataFields[4].isEmpty() || dataFields[7].isEmpty();
        if (!dataFields[11].isEmpty()) {
            if (!emptyStandardAddress) {
                return null;
            }
            else {
                var nonStAddr = new NonStandardAddress(dataFields[11], dataFields[13]);
                if (nonStAddr.type().isValid()) {
                    dataFields[4] = String.valueOf(nonStAddr.getAddress().getBuildingRange().low());
                    dataFields[5] = "";
                    dataFields[6] = "";
                    dataFields[7] = nonStAddr.getAddress().getStreet();
                    dataFields[8] = "";
                } else {
                    return null;
                }
            }
        } else if (emptyStandardAddress) {
            return null;
        }
        return super.getData(lineNum, dataFields);
    }

    @Override
    public boolean isRangeData() {
        return false;
    }
}
