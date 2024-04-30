package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.model.district.County;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileDataExtractor;
import gov.nysenate.sage.scripts.streetfinder.scripts.utils.StreetfileLineType;

import java.io.File;

import static gov.nysenate.sage.model.district.DistrictType.*;

/**
 * Parses Suffolk County txt file and outputs a tsv file
 */
public class SuffolkParser extends CountyParser {
    public SuffolkParser(File file, County county) {
        super(file, county);
    }

    @Override
    protected StreetfileDataExtractor getDataExtractor() {
        // TODO: Suffix may be in street. Also has apartment data.
        return super.getDataExtractor().addIsProperLengthFunction(19)
                .addSplitTest(list -> list.get(3).contains("DO NOT USE MAIL"), StreetfileLineType.SKIP)
                .addBuildingIndices(6, 7, 8).addStreetIndices(2, 3, 4, 5).addType(TOWN_CITY, 13)
                .addTypesInOrder(ELECTION, CONGRESSIONAL, SENATE, ASSEMBLY, CLEG, COUNTY, FIRE, VILLAGE);
    }

    @Override
    protected String delim() {
        return "\t";
    }
}
