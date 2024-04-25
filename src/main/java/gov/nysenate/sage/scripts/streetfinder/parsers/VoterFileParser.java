package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileDataExtractor;

import java.io.File;
import java.util.List;
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
                .addIsProperLengthFunction(47).addIsProperFunction(lineParts -> !lineParts.get(41).matches("[IP]"))
                .addBuildingIndices(4).addStreetIndices(6, 7, 8).addPostalCityIndex(12).addType(ZIP, 13)
                .addType(COUNTY, 23).addTypesInOrder(ELECTION, CLEG).addType(WARD, 27)
                .addTypesInOrder(CONGRESSIONAL, SENATE, ASSEMBLY)
                .addCountyFunction(23, countyStr -> fipsCodeMap.get(Integer.parseInt(countyStr)))
                .addIdFunction((lineParts, lineNum) -> Long.parseLong(lineParts.get(45).replaceFirst("^NY", "")));
    }

    @Override
    protected List<String> parseLine(String line) {
        List<String> parsedLine = super.parseLine(line);
        boolean emptyStandardAddress = parsedLine.get(4).isEmpty() || parsedLine.get(7).isEmpty();
        if (!parsedLine.get(11).isEmpty()) {
            if (!emptyStandardAddress) {
                return null;
            }
            else {
                var nonStAddr = new NonStandardAddress(parsedLine.get(11), parsedLine.get(13));
                if (nonStAddr.type().isValid()) {
                    parsedLine.set(4, String.valueOf(nonStAddr.getAddress().getBuildingRange().low()));
                    parsedLine.set(5, "");
                    parsedLine.set(6, "");
                    parsedLine.set(7, nonStAddr.getAddress().getStreet());
                    parsedLine.set(8, "");
                } else {
                    return null;
                }
            }
        } else if (emptyStandardAddress) {
            return null;
        }
        return parsedLine;
    }

    @Override
    public boolean isRangeData() {
        return false;
    }
}
