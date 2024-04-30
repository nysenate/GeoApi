package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileDataExtractor;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static gov.nysenate.sage.model.district.DistrictType.*;
import static gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileLineType.*;

public class VoterFileParser extends BaseParser {
    public final Set<String> townCities = new HashSet<>();
    private final Map<Integer, Integer> countyFipsCodeMap;

    public VoterFileParser(File file, Map<Integer, Integer> countyFipsCodeMap) {
        super(file);
        this.countyFipsCodeMap = countyFipsCodeMap;
    }

    @Override
    protected StreetfileDataExtractor getDataExtractor() {
        // TODO: add towncity?
        return super.getDataExtractor()
                .addIsProperLengthFunction(47)
                .addSplitTest(lineParts -> lineParts.get(41).matches("[IP]"), SKIP)
                .addSplitTest(lineParts -> missingStandardAddress(lineParts) && lineParts.get(11).isEmpty(), MISSING_ADDRESS_DATA)
                .addSplitTest(lineParts -> !missingStandardAddress(lineParts) && !lineParts.get(11).isEmpty(), TWO_ADDRESS_TYPES)
                .addSplitTest(lineParts -> !parsedAddress(lineParts), UNPARSED_NON_STANDARD_ADDRESS)
                .addBuildingIndices(4).addStreetIndices(6, 7, 8).addPostalCityIndex(12).addType(ZIP, 13)
                .addType(COUNTY, 23).addTypesInOrder(ELECTION, CLEG).addType(WARD, 27)
                .addTypesInOrder(CONGRESSIONAL, SENATE, ASSEMBLY)
                .addCountyFunction(23, countyStr -> countyFipsCodeMap.get(Integer.parseInt(countyStr)))
                .addIdFunction((lineParts, lineNum) -> Long.parseLong(lineParts.get(45).replaceFirst("^NY", "")));
    }

    private static boolean missingStandardAddress(List<String> lineParts) {
        return lineParts.get(4).isEmpty() || lineParts.get(7).isEmpty();
    }

    private static boolean parsedAddress(final List<String> lineParts) {
        if (lineParts.get(11).isEmpty()) {
            return true;
        }
        var nonStAddr = new NonStandardAddress(lineParts.get(11), lineParts.get(13));
        if (nonStAddr.type().isValid()) {
            lineParts.set(4, String.valueOf(nonStAddr.getAddress().getBuildingRange().low()));
            lineParts.set(5, "");
            lineParts.set(6, "");
            lineParts.set(7, nonStAddr.getAddress().getStreet());
            lineParts.set(8, "");
        }
        return nonStAddr.type().isValid();
    }
}
