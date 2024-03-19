package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileDataExtractor;

import java.io.File;

import static gov.nysenate.sage.model.district.DistrictType.*;

public class VoterFileParser extends BaseParser {
    public VoterFileParser(File file) {
        super(file);
    }

    @Override
    protected StreetfileDataExtractor getDataExtractor() {
        return new StreetfileDataExtractor(VoterFileParser.class.getSimpleName())
                .addBuildingIndices(4).addStreetIndices(6, 7, 8).addType(CITY, 12).addType(ZIP, 13)
                .addType(COUNTY, 23).addTypesInOrder(ELECTION, CLEG).addType(WARD, 27)
                .addTypesInOrder(CONGRESSIONAL, SENATE, ASSEMBLY)
                .addIdFunction((lineParts, lineNum) -> (long) Integer.parseInt(lineParts.get(45).replaceFirst("^NY", "")));
    }
}
