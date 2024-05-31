package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.dao.provider.district.MunicipalityType;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileDataExtractor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static gov.nysenate.sage.model.district.DistrictType.*;
import static gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileLineType.*;

public class VoterFileParser extends BaseParser {
    private static final List<String> queensNeighborhoods = List.of("Jamaica", "Flushing", "Astoria", "Ridgewood",
            "Woodside", "(East *)?Elmhurst", "ForestHills", "Queens *Village", "Far *Rockaway", "Jackson *Heights", "Corona",
            "Fresh *Meadows", "(South *)?Ozone *Park", "Bayside", "Long *Island *City", "Rego *Park", "Whitestone",
            "Springfield *Gardens", "Richmond *Hill", "Rosedale"),
            nycBoroughs = List.of("Bronx", "Brooklyn", "Manhattan", "Queens", "Staten *Island", "Kings", "N *Y *C");
    private static final String nycPattern;
    static {
        var totalList = new ArrayList<>(queensNeighborhoods);
        totalList.addAll(nycBoroughs);
        nycPattern = "(?i)(%s)".formatted(String.join("|", totalList));
    }
    private final Map<Integer, Integer> countyFipsCodeMap;
    private final String richmondId;

    public VoterFileParser(File file, Map<MunicipalityType, Map<String, Integer>> typeAndNameToIdMap,
                           Map<Integer, Integer> countyFipsCodeMap) {
        super(file, typeAndNameToIdMap);
        this.countyFipsCodeMap = countyFipsCodeMap;
        this.richmondId = dataExtractor.getTownCityId("Richmond", false);
    }

    @Override
    protected StreetfileDataExtractor getDataExtractor() {
        return super.getDataExtractor()
                .addIsProperLengthFunction(47)
                .addSplitTest(lineParts -> lineParts.get(41).matches("[IP]"), SKIP)
                .addSplitTest(lineParts -> missingStandardAddress(lineParts) && lineParts.get(11).isEmpty(), MISSING_ADDRESS_DATA)
                .addSplitTest(lineParts -> !missingStandardAddress(lineParts) && !lineParts.get(11).isEmpty(), TWO_ADDRESS_TYPES)
                .addSplitTest(lineParts -> !parsedAddress(lineParts), UNPARSED_NON_STANDARD_ADDRESS)
                .addBuildingIndices(4).addStreetIndices(6, 7, 8).addPostalCityIndex(12).addType(ZIP, 13)
                .addType(COUNTY, 23).addTypesInOrder(ELECTION, CLEG, TOWN_CITY, WARD)
                .addTypesInOrder(CONGRESSIONAL, SENATE, ASSEMBLY)
                .addCountyFunction(lineParts -> countyFipsCodeMap.get(Integer.parseInt(lineParts.get(23))))
                .addIdFunction((lineParts, lineNum) -> Long.parseLong(lineParts.get(45).replaceFirst("^NY", "")));
    }

    @Override
    protected List<String> parseLine(String line) {
        List<String> tempLine = super.parseLine(line);
        String townCity = tempLine.get(26);
        townCity = townCity.replaceAll("[()]| NY", "").replaceAll("\\.", " ")
                .replaceAll("\\s+", "").replaceFirst("^T ", "TOWN OF ")
                .replaceFirst("^C ", "CITY OF");
        // Richmond is both a NYC neighborhood and a town upstate.
        if (townCity.contains("BROOKLYN")) {
            int i = 0;
        }
        if (!richmondId.equals("0") &&
                richmondId.equals(dataExtractor.getTownCityId(townCity, false))) {
            try {
                if (Integer.parseInt(tempLine.get(28)) <= 16) {
                    townCity = "New York";
                }
            } catch (NumberFormatException ignored) {}
        }
        else if (townCity.matches(nycPattern)) {
            townCity = "New York";
        }
        tempLine.set(26, townCity);
        return tempLine;
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
