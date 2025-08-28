package gov.nysenate.sage.scripts.streetfinder.parsers;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import gov.nysenate.sage.model.district.County;
import gov.nysenate.sage.model.district.TownCity;
import gov.nysenate.sage.scripts.streetfinder.model.StreetfileType;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileDataExtractor;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.collections.CaseInsensitiveKeyMap;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static gov.nysenate.sage.model.district.DistrictType.*;
import static gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileLineType.*;

public class VoterFileParser extends BaseParser {
    private final ImmutableMap<Integer, Integer> countyCodeMap;
    private final ImmutableSet<String> nycVoterfileCodes;
    private final ImmutableMap<Pattern, TownCity> patternToTownCityMap;
    private final Map<String, TownCity> nameStrToTownCityMap = new CaseInsensitiveKeyMap<>();

    public VoterFileParser(File file, Set<County> counties, Set<TownCity> townCities) {
        super(file);
        this.countyCodeMap = ImmutableMap.copyOf(counties.stream()
                .collect(Collectors.toMap(County::voterfileCode, County::senateCode))
        );
        this.nycVoterfileCodes = ImmutableSet.copyOf(counties.stream().filter(County::inNYC)
                .map(county -> String.valueOf(county.voterfileCode())).collect(Collectors.toSet())
        );

        var tempPatternMap = new HashMap<Pattern, TownCity>();
        var tempDuplicateMap = new HashMap<String, Integer>();
        for (TownCity townCity : townCities) {
            tempPatternMap.put(getPatternString(townCity), townCity);
            tempDuplicateMap.merge(townCity.baseName(), 1, Integer::sum);
        }
        this.patternToTownCityMap = ImmutableMap.copyOf(tempPatternMap);

        townCities.stream().filter(tc -> !StringUtils.isBlank(tc.voterfileCode()))
                .forEach(tc -> nameStrToTownCityMap.put(tc.voterfileCode(), tc));
        // Empty Strings will never match, and if multiple towns/cities have the same baseName,
        // that baseName will always match multiple TownCity.
        nameStrToTownCityMap.put("", null);
        tempDuplicateMap.entrySet().stream().filter(entry -> entry.getValue() > 1)
                .map(Map.Entry::getKey).forEach(baseName -> nameStrToTownCityMap.put(baseName, null));
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
        String townCityStr = tempLine.get(26);
        // In NYC, this field may contain e.g. the borough or Queens neighborhood, which should be overridden.
        if (nycVoterfileCodes.contains(tempLine.get(23).replaceFirst("^0+", ""))) {
            townCityStr = "New York City";
        }
        TownCity townCity = null;
        if (!nameStrToTownCityMap.containsKey(townCityStr)) {
            var candidates = new HashSet<TownCity>();
            for (var entry : patternToTownCityMap.entrySet()) {
                if (entry.getKey().matcher(townCityStr).matches()) {
                    candidates.add(entry.getValue());
                }
            }
            if (candidates.isEmpty()) {
                System.err.println("No matches for " + townCityStr);
            }
            else if (candidates.size() > 1) {
                System.err.printf("Multiple matches for %s: %s%n", townCityStr, candidates);
            }
            else {
                townCity = candidates.iterator().next();
            }
            // Ensures we don't need to re-calculate the correct code.
            // TODO: configurable whether to just put null

            nameStrToTownCityMap.put(townCityStr, townCity);
        }
        townCity = nameStrToTownCityMap.get(townCityStr);
        tempLine.set(26, townCity == null ? townCityStr : townCity.code());
        return tempLine;
    }

    private static Pattern getPatternString(TownCity townCity) {
        String patternBase = townCity.isCity() ?
                "(C |City?( of)? )?%s([ /]City)?" : "(T |Town( of)? )?%s([ /]Town)?";
        String[] split = townCity.baseName().split("[ .]", 2);
        if (split[0].matches("(?i)North|South|East|West")) {
            split[0] = "(" + split[0].charAt(0) + "|" + split[0] + ")";
        }
        else if ("Mount".equalsIgnoreCase(split[0])) {
            split[0] = "(MT|" + split[0] + ")";
        } else if ("Fort".equalsIgnoreCase(split[0])) {
            split[0] = "(FT|" + split[0] + ")";
        }

        return Pattern.compile(patternBase.formatted(String.join("[. ]{0,2}", split), Pattern.CASE_INSENSITIVE));
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
