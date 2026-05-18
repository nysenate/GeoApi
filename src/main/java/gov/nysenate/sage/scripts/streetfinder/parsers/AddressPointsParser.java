package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.model.StreetfileType;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileDataExtractor;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileLineType;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.List;
import java.util.Map;

import static gov.nysenate.sage.model.district.DistrictType.*;

public class AddressPointsParser extends BaseParser {
    private final Map<String, Integer> codeMap;

    public AddressPointsParser(File file, Map<String, Integer> codeMap) {
        super(file);
        this.codeMap = codeMap;
    }

    @Nonnull
    @Override
    public StreetfileType type() {
        return StreetfileType.ADDRESS_POINTS;
    }

    @Override
    protected StreetfileDataExtractor getDataExtractor() {
        return super.getDataExtractor().addIsProperLengthFunction(27)
                .addSplitTest(lineParts -> !lineParts.get(7).equals("NY") ||
                        !lineParts.get(9).matches("[123]") ||
                        !lineParts.get(14).equalsIgnoreCase("Active"), StreetfileLineType.SKIP)
                .addIdFunction((lineParts, lineNum) -> Long.valueOf(lineParts.getFirst()))
                .addBuildingIndices(2).addStreetIndices(13).addPostalCityIndex(6).addType(ZIP, 8)
                .addType(SENATE, 24).addTypesInOrder(ASSEMBLY, CONGRESSIONAL);
    }

    @Override
    protected String delim() {
        return "\t";
    }

    @Override
    protected List<String> parseLine(String line) {
        List<String> lineParts = super.parseLine(line);
        lineParts.set(2, lineParts.get(1) + lineParts.get(2));
        lineParts.set(1, "");
        lineParts.set(16, codeMap.get(lineParts.get(16).toLowerCase()).toString());
        return lineParts;
    }
}
