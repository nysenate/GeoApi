package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.model.address.StreetFinderAddress;
import gov.nysenate.sage.model.district.DistrictType;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Parses Schohaire County.txt file
 * Looks for street name, town, low, high, range type, sch, townCode, vill
 */
public class SchoharieParser extends NTSParser {
    public SchoharieParser(String file) throws IOException {
        super(file);
    }

    /**
     * Parses the file for the needed information. Finds where the actual data is and then
     * parseLine is called to parse the line
     *
     * @throws FileNotFoundException
     */
    public void parseFile() throws IOException {
        super.readFile();
    }

    //TODO ADD Zip code parsing once we get a streetfile with zipcodes included
    @Override
    protected void parseLine(String line) {
        if (line.contains("House Range") && line.contains("This conflicts")
                && line.contains("Ricks Test") && line.contains("Dist")) {
            return;
        }
        String[] splitLine = line.split(",", Integer.MAX_VALUE);

        if (!splitLine[6].isEmpty() || !splitLine[1].isEmpty() || splitLine.length >= 11) {


            StreetFinderAddress streetFinderAddress = new StreetFinderAddress();

            getStreet(splitLine[0] , streetFinderAddress);   //splitLine[0] StreetName
            getRanges(splitLine[1], streetFinderAddress);   //splitLine[1] House Range
            getTown(splitLine[2], streetFinderAddress);     //splitLine[2] Town
            // splitLine[3] Ward     USELESS Not used in the 2018 file
            getED(splitLine[4], streetFinderAddress);      //splitLine[4] ED
            getCong(splitLine[5], streetFinderAddress);     //splitLine[5] Cong
            getSen(splitLine[6], streetFinderAddress);      //splitLine[6] Sen
            getAsm(splitLine[7], streetFinderAddress);       //splitLine[7] Asm
            getSchool(splitLine[8],streetFinderAddress);    //splitLine[8] School
            //splitLine[9] Cleg     USELESS Not used in the 2018 file
            //splitLine[10] City    USELESS Not used in the 2018 file
            getVillage(splitLine[11], streetFinderAddress);    //splitLine[11] Village
            //splitLine[12] fire    USELESS Not used in the 2018 file


            if (streetFinderAddress.hasSenateDistrict() &&
                    streetFinderAddress.hasBuildingParity()) {
                super.writeToFile(streetFinderAddress);
            }
        }
    }

    private void getStreet(String data, StreetFinderAddress streetFinderAddress) {
        if (!data.equalsIgnoreCase("street name")) {
            streetFinderAddress.setStreet(data);
        }
    }

    private void getRanges(String data, StreetFinderAddress streetFinderAddress) {

        if (data.isEmpty() || data.contains("House Range")) {
            return;
        }

        String[] splitData = data.split("-");

        if (splitData.length == 3) {
            streetFinderAddress.setBuilding(true, false, splitData[0].trim());
            streetFinderAddress.setBuilding(false, false, splitData[1].trim());

            if (splitData[2].trim().equals("O")) {
                streetFinderAddress.setBldg_parity("ODDS");
            } else if(splitData[2].trim().equals("E")) {
                streetFinderAddress.setBldg_parity("EVENS");
            } else {
                streetFinderAddress.setBldg_parity("ALL");
            }
        }
    }

    private void getTown(String data, StreetFinderAddress streetFinderAddress) {
        if (!data.equalsIgnoreCase("Town")) {
            streetFinderAddress.setTown(data);
        }
    }

    private void getED(String data, StreetFinderAddress streetFinderAddress) {
        if (!data.equalsIgnoreCase("Dist")) {
            streetFinderAddress.setED(data);
        }
    }

    private void getCong(String data, StreetFinderAddress streetFinderAddress) {
        if (!data.equalsIgnoreCase("Cong")) {
            streetFinderAddress.put(DistrictType.CONGRESSIONAL, data);
        }
    }

    private void getSen(String data, StreetFinderAddress streetFinderAddress) {
        if (!data.equalsIgnoreCase("Sen")) {
            streetFinderAddress.put(DistrictType.SENATE, data);
        }
    }

    private void getAsm(String data, StreetFinderAddress streetFinderAddress) {
        if (!data.equalsIgnoreCase("Asm")) {
            streetFinderAddress.put(DistrictType.ASSEMBLY, data);
        }
    }

    private void getSchool(String data, StreetFinderAddress streetFinderAddress) {
        if (!data.equalsIgnoreCase("School")) {
            streetFinderAddress.setSch(data);
        }
    }

    private void getVillage(String data, StreetFinderAddress streetFinderAddress) {
        if (!data.equalsIgnoreCase("Village")) {
            streetFinderAddress.put(DistrictType.VILLAGE, data);
        }
    }
}
