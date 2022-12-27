package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.model.CountyStreetfileName;
import gov.nysenate.sage.scripts.streetfinder.model.StreetFileAddress;
import gov.nysenate.sage.scripts.streetfinder.model.StreetParity;
import gov.nysenate.sage.scripts.streetfinder.scripts.ApiHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static gov.nysenate.sage.scripts.streetfinder.model.StreetFileField.*;

public class VoterFileParser extends BasicParser {
    private static final Map<String, String> voterFileCodeToSenateCountyCodeMap = getVoterFileCountyCodeToSenateCountyCode();

    public VoterFileParser(File file) {
        super(file);
    }

    @Override
    protected void parseLine(String line) {
        super.parseLine(line, 0, "\t");
    }

    @Override
    protected List<BiConsumer<StreetFileAddress, String>> getFunctions() {
        List<BiConsumer<StreetFileAddress, String>> functions = new ArrayList<>();
        functions.add(VoterFileParser::singleAddressBuildingFunction);
        functions.add(skip);
        functions.addAll(streetParts(3));
        functions.addAll(skip(3));
        functions.addAll(functions(CITY, ZIP));
        functions.add(skip);
        functions.add((sfa, str) -> sfa.put(COUNTY_ID,
                // TODO: move this elsewhere
                voterFileCodeToSenateCountyCodeMap.get(str.replaceFirst("^0+", ""))));
        functions.addAll(functions(ELECTION_CODE, CLEG));
        functions.add(skip);
        functions.addAll(functions(WARD, CONGRESSIONAL, SENATE, ASSEMBLY));
        return functions;
    }

    private static void singleAddressBuildingFunction(StreetFileAddress address, String houseNum) {
        address.setBuilding(true, houseNum);
        address.setBuilding(false, houseNum);
        address.setBldgParity(StreetParity.ALL.name());
    }

    private static Map<String, String> getVoterFileCountyCodeToSenateCountyCode() {
        try {
            Map<String, String> countyNameToVoterFileNumMap = new HashMap<>();
            for (var nameEnum : CountyStreetfileName.values()) {
                if (nameEnum != CountyStreetfileName.VoterFile) {
                    countyNameToVoterFileNumMap.put(nameEnum.getNameInCountyData(), String.valueOf(nameEnum.ordinal() + 1));
                }
            }
            // TODO: don't hard-code these
            ApiHelper apiHelper = new ApiHelper("http://localhost:8080", "ahh");
            Map<String, String> countyNameToSenateCodeMap = apiHelper.getCodes(false);
            Map<String, String> ret = new HashMap<>();
            for (String countyName : countyNameToSenateCodeMap.keySet()) {
                ret.put(countyNameToVoterFileNumMap.get(countyName), countyNameToSenateCodeMap.get(countyName));
            }
            return ret;
        }
        catch (Exception ex) {
            return null;
        }
    }
}
