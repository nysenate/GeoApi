package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.model.address.StreetFinderAddress;
import gov.nysenate.sage.model.district.DistrictType;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Parses Erie County 2018 csv and puts parsed data into a tsv file
 * Looks for street, low, high, range type, townCode, District, zip
 */
public class ErieParser extends NTSParser {

    /**
     * Calls the super constructor which sets up the tsv file
     * @param file
     * @throws IOException
     */
    public ErieParser(String file) throws IOException {
        super(file);
    }


    /**
     * Parses the file by calling parseLine for each line of data
     * @throws FileNotFoundException
     */
    public void parseFile() throws IOException {
       super.readFile();
    }

    /**
     * Parses the line by calling each method to find all the data given in the file
     * and add the data to the StreetFinderAddress
     * @param line
     */
    @Override
    protected void parseLine(String line) {
        StreetFinderAddress streetFinderAddress = new StreetFinderAddress();

        //Split the line by ","
        String[] splitLine = line.split(",");
        getStreetAndSuffix(splitLine[0], streetFinderAddress);          //0 street and suffix
        streetFinderAddress.setBuilding(true, false, splitLine[1]);
        streetFinderAddress.setBuilding(false, false, splitLine[2]);
        streetFinderAddress.setBldg_parity(getRangeType(splitLine[3]));
        streetFinderAddress.setZip(splitLine[4]);
        streetFinderAddress.setTown(splitLine[5]);
        handlePrecinct(splitLine, streetFinderAddress);                 //9 precinct / ed
        getSenateDistrict(splitLine[10], streetFinderAddress);          //10 senate district
        getAssemblyDistrict(splitLine[11], streetFinderAddress);        //11 assem district
        getLegislativeDistrict(splitLine[12], streetFinderAddress);     //12 legislative district
        getCongressionalDistrict(splitLine[13], streetFinderAddress);   //13 congressional district
        writeToFile(streetFinderAddress);
    }

    private void handlePrecinct(String[] splitLine, StreetFinderAddress streetFinderAddress) {
        //always 6 digits
        //leading zero if only 5 digits
        //first 2 digits are town code
        //second 2 digits are the ward
        //third 2 digits are the ED

        String precinct = splitLine[9];
        if (precinct.length() == 5) {
            precinct = "0" + precinct;
        }
        getTownCode(precinct.substring(0, 2), streetFinderAddress);
        getWard(precinct.substring(2, 4), streetFinderAddress);
        getElectionDistrict(precinct.substring(precinct.length() - 2), streetFinderAddress);

    }

    /**
     * Gets the Street name and Street Suffix from a string containing both
     * Also checks for  pre-Direction
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getStreetAndSuffix(String splitLine, StreetFinderAddress StreetFinderAddress) {
        int count = 0;
        //split the string by spaces to get each individual word
        String[] string = splitLine.split("\\s+");

        //check for pre-Direction
        if (checkForDirection(string[0])) {
            StreetFinderAddress.setPreDirection(string[0]);
            count++;
        }

        //get the street name (multiple words)
        //Assume that the last index of string[] is the street suffix
        StringBuilder temp = new StringBuilder();
        for(int i = count; i < string.length - 1; i++) {
            temp.append(string[i]).append(" ");
        }
        StreetFinderAddress.setStreet(temp.toString().trim());
        StreetFinderAddress.setStreetSuffix(string[string.length -1]);
    }

    /**
     * Gets the Range Type and converts to standard formatting
     * @param splitLine
     */
    private String getRangeType(String splitLine) {
        if (splitLine.matches("Even|Odd")) {
            return splitLine.toUpperCase() + "S";
        }
        return "ALL";
    }

    private void getElectionDistrict(String ed, StreetFinderAddress streetFinderAddress) {
        streetFinderAddress.setED(ed);
    }

    private void getWard(String ward, StreetFinderAddress streetFinderAddress) {
        streetFinderAddress.put(DistrictType.WARD, ward);
    }

    private void getTownCode(String townCode, StreetFinderAddress streetFinderAddress) {
        streetFinderAddress.setTownCode(townCode);
    }

    private void getSenateDistrict(String splitline, StreetFinderAddress streetFinderAddress) {
        String[] district = splitline.split("-");
        streetFinderAddress.put(DistrictType.SENATE, district[1]);
    }

    private void getAssemblyDistrict(String splitline, StreetFinderAddress streetFinderAddress) {
        String[] district = splitline.split("-");
        streetFinderAddress.put(DistrictType.ASSEMBLY, district[1]);
    }

    private void getLegislativeDistrict(String splitline, StreetFinderAddress streetFinderAddress) {
        String[] district = splitline.split("-");
        streetFinderAddress.setDist(district[1]);
    }

    private void getCongressionalDistrict(String splitline, StreetFinderAddress streetFinderAddress) {
        String[] district = splitline.split("-");
        streetFinderAddress.put(DistrictType.CONGRESSIONAL, district[1]);
    }


}
