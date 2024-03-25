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
                .addIdFunction((lineParts, lineNum) -> Long.parseLong(lineParts.get(45).replaceFirst("^NY", "")));
    }

    @Override
    protected void addData(int lineNum, String... dataFields) {
        if (dataFields[41].matches("[IP]")) {
            return;
        }
        var nonStandardAddress = new NonStandardAddress(dataFields[11], dataFields[13]);
        boolean emptyStandardAddress = dataFields[4].isEmpty() || dataFields[7].isEmpty();
        if (!dataFields[11].isEmpty()) {
            if (!emptyStandardAddress) {
                // TODO: add problem
            }
            else {
                var nonStAddr = new NonStandardAddress(dataFields[11], dataFields[13]);
                if (nonStAddr.type().isValid()) {
                    // TODO: add
                } else {
                    //TODO: set type
                }
            }
        } else if (emptyStandardAddress) {
            // TODO: no address type
        }
    }

    @Override
    public boolean isRangeData() {
        return false;
    }
}
