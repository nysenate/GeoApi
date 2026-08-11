package gov.nysenate.sage.scripts.streetfinder.parsers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static gov.nysenate.sage.model.district.DistrictType.*;
import static gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileLineType.*;

public class VoterFileParser extends BaseParser {
    private static final Logger logger = LoggerFactory.getLogger(VoterFileParser.class);
    // The voter file numbers counties in plain lexicographic order.
    public static final Comparator<County> countyOrder = Comparator.comparing(County::name);

    private final ImmutableList<County> countyList;
    private final ImmutableSetMultimap<County, TownCity> countyToTownCityMap;
    private final TownCity nyc;
    private final Map<County, CaseInsensitiveKeyMap<TownCity>> countyToTownCityFieldMap = new HashMap<>();

    public VoterFileParser(File file, Multimap<County, TownCity> countyToTownCityMap, TownCity nyc) {
        super(file);
        this.countyList = countyToTownCityMap.keySet().stream().sorted(countyOrder)
                .collect(ImmutableList.toImmutableList());
        this.countyToTownCityMap = ImmutableSetMultimap.copyOf(countyToTownCityMap);
        this.nyc = nyc;

        // Populate the map.
        for (var county : countyToTownCityMap.keySet()) {
            var tempMap = new CaseInsensitiveKeyMap<TownCity>();
            for (TownCity currTownCity : countyToTownCityMap.get(county)) {
                if (!StringUtils.isBlank(currTownCity.voterFileCode())) {
                    tempMap.put(currTownCity.voterFileCode(), currTownCity);
                }
            }
            countyToTownCityFieldMap.put(county, tempMap);
        }
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
                .addType(COUNTY, 23).addTypesInOrder(ELECTION, COUNTY_LEGISLATURE, TOWN_CITY, WARD)
                .addTypesInOrder(CONGRESSIONAL, SENATE, ASSEMBLY)
                .addIdFunction((lineParts, lineNum) -> Long.parseLong(lineParts.get(45).replaceFirst("^NY", "")));
    }

    @Override
    protected List<String> parseLine(String line) {
        List<String> tempLine = super.parseLine(line);
        // Adjust for zero-indexing of List.
        County county = countyList.get(Integer.parseInt(tempLine.get(23)) - 1);
        tempLine.set(23, String.valueOf(county.code()));
        TownCity townCity = null;
        // In NYC, this field may contain e.g. the borough or Queens neighborhood, which should be overridden.
        if (county.inNYC()) {
            townCity = nyc;
        } else {
            String townCityStr = tempLine.get(26);
            CaseInsensitiveKeyMap<TownCity> fieldMap = countyToTownCityFieldMap.get(county);
            if (!fieldMap.containsKey(townCityStr)) {
                Set<TownCity> candidates = countyToTownCityMap.get(county).stream()
                        .filter(tc -> tc.pattern().matcher(townCityStr).matches())
                        .collect(Collectors.toSet());
                // If just a baseName is given when there is a town and a city with the same name, it refers to the town.
                if (candidates.size() > 1 && candidates.stream().map(TownCity::baseName).distinct().count() == 1) {
                    candidates = candidates.stream().filter(TownCity::isTown).collect(Collectors.toSet());
                }
                if (candidates.size() != 1) {
                    logger.warn("Couldn't get TownCity from '{}'. Matched: {}", townCityStr, candidates);
                } else {
                    townCity = candidates.iterator().next();
                }
                // Ensures we don't need to re-calculate the correct code.
                fieldMap.put(townCityStr, townCity);
            }
            townCity = fieldMap.get(townCityStr);
        }
        tempLine.set(26, townCity == null ? "" : townCity.id().toString());
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
