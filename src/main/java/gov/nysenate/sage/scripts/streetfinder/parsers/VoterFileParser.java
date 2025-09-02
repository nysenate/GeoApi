package gov.nysenate.sage.scripts.streetfinder.parsers;

import com.google.common.collect.*;
import gov.nysenate.sage.model.district.County;
import gov.nysenate.sage.model.district.TownCity;
import gov.nysenate.sage.scripts.streetfinder.model.StreetfileType;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileDataExtractor;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.collections.CaseInsensitiveKeyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static gov.nysenate.sage.model.district.DistrictType.*;
import static gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileLineType.*;

public class VoterFileParser extends BaseParser {
    private static final Logger logger = LoggerFactory.getLogger(VoterFileParser.class);

    private final ImmutableMap<Integer, Integer> countyCodeMap;
    private final ImmutableSet<Integer> nycVoterfileCodes;
    private final ImmutableSetMultimap<Integer, TownCity> countyVoterfileCodeToTownCityMap;
    private final Map<String, TownCity> nameStrToTownCityMap = new CaseInsensitiveKeyMap<>();

    public VoterFileParser(File file, Multimap<County, TownCity> countyToTownCityMap) {
        super(file);
        this.countyCodeMap = ImmutableMap.copyOf(countyToTownCityMap.keySet().stream()
                .collect(Collectors.toMap(County::voterfileCode, County::senateCode))
        );
        this.nycVoterfileCodes = ImmutableSet.copyOf(countyToTownCityMap.keySet().stream().filter(County::inNYC)
                .map(County::voterfileCode).collect(Collectors.toSet())
        );

        Multimap<Integer, TownCity> tempMap = HashMultimap.create();
        for (County county : countyToTownCityMap.keySet()) {
            for (TownCity townCity : countyToTownCityMap.get(county)) {
                tempMap.put(county.voterfileCode(), townCity);
            }
        }
        this.countyVoterfileCodeToTownCityMap = ImmutableSetMultimap.copyOf(tempMap);

        countyToTownCityMap.values().stream().filter(tc -> !StringUtils.isBlank(tc.voterfileCode()))
                .forEach(tc -> nameStrToTownCityMap.put(tc.voterfileCode(), tc));
        // Empty Strings will never match.
        nameStrToTownCityMap.put("", null);
    }

    @Nonnull
    @Override
    public StreetfileType type() {
        return StreetfileType.VOTER;
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
                .addType(COUNTY, 23).addTypesInOrder(ELECTION, COUNTY_LEG, TOWN_CITY, WARD)
                .addTypesInOrder(CONGRESSIONAL, SENATE, ASSEMBLY)
                .addCountyFunction(lineParts -> countyCodeMap.get(Integer.parseInt(lineParts.get(23))))
                .addIdFunction((lineParts, lineNum) -> Long.parseLong(lineParts.get(45).replaceFirst("^NY", "")));
    }

    @Override
    protected List<String> parseLine(String line) {
        List<String> tempLine = super.parseLine(line);
        String townCityStr;
        int countyCode = Integer.parseInt(tempLine.get(23));
        // In NYC, this field may contain e.g. the borough or Queens neighborhood, which should be overridden.
        if (nycVoterfileCodes.contains(countyCode)) {
            townCityStr = "New York City";
        } else {
            townCityStr = tempLine.get(26);
        }
        TownCity townCity = null;
        if (!nameStrToTownCityMap.containsKey(townCityStr)) {
            Set<TownCity> candidates = countyVoterfileCodeToTownCityMap.get(countyCode).stream()
                    .filter(tc -> tc.pattern().matcher(townCityStr).matches())
                    .collect(Collectors.toSet());
            // If just a baseName is given when there is a town and a city with the same name, it refers to the town.
            if (candidates.size() > 1 && candidates.stream().map(TownCity::baseName).distinct().count() == 1) {
                candidates = candidates.stream().filter(TownCity::isTown).collect(Collectors.toSet());
            }
            if (candidates.size() != 1) {
                logger.warn("Couldn't match {}. Matched: {}", townCityStr, candidates);
            }
            else {
                townCity = candidates.iterator().next();
            }
            // Ensures we don't need to re-calculate the correct code.
            nameStrToTownCityMap.put(townCityStr, townCity);
        }
        townCity = nameStrToTownCityMap.get(townCityStr);
        tempLine.set(26, townCity == null ? "" : townCity.code());
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
