package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.scripts.streetfinder.model.CountyStreetfileName;
import gov.nysenate.sage.scripts.streetfinder.model.StreetFileAddress;
import gov.nysenate.sage.scripts.streetfinder.scripts.GetCodes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static gov.nysenate.sage.scripts.streetfinder.model.StreetFileField.*;

public class VoterFileParser extends BasicParser {
    private static final Map<String, String> voterFileCodeToSenateCountyCodeMap = getVoterFileCountyCodeToSenateCountyCode();

    public VoterFileParser(String filename) throws IOException {
        super(filename);
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
        functions.add(StreetFileAddress::setPreDirection);
        functions.add(function(STREET));
        functions.add(StreetFileAddress::setPostDirection);
        functions.addAll(skip(3));
        functions.addAll(functions(CITY, ZIP));
        functions.add(skip);
        functions.add((sfa, str) -> sfa.put(COUNTY_ID, voterFileCodeToSenateCountyCodeMap.get(str.replaceFirst("^0+", ""))));
        functions.addAll(functions(ELECTION_CODE, CLEG, TOWN, WARD, CONGRESSIONAL, SENATE, ASSEMBLY));
        return functions;
    }

    private static void singleAddressBuildingFunction(StreetFileAddress address, String houseNum) {
        address.setBuilding(true, houseNum);
        address.setBuilding(false, houseNum);
        address.setBldgParity("ALL");
    }

    private static Map<String, String> getVoterFileCountyCodeToSenateCountyCode() {
        try {
            Map<String, String> countyNameToVoterFileNumMap = new HashMap<>();
            for (var nameEnum : CountyStreetfileName.values()) {
                if (nameEnum != CountyStreetfileName.VoterFile) {
                    countyNameToVoterFileNumMap.put(nameEnum.getNameInCountyData(), String.valueOf(nameEnum.ordinal() + 1));
                }
            }
            Map<String, String> countyNameToSenateCodeMap = GetCodes.getCodesHelper("http://localhost:8080", "ahh", false);
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
