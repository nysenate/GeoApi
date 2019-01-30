package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.model.address.StreetFinderAddress;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Parses Erie County 2018 csv and puts parsed data into a tsv file
 * Looks for street, low, high, range type, townCode, District, zip
 */
public class ErieParser extends NTSParser {

    private String file;

    /**
     * Calls the super constructor which sets up the tsv file
     * @param file
     * @throws IOException
     */
    public ErieParser(String file) throws IOException {
        super(file);
        this.file = file;
    }


    /**
     * Parses the file by calling parseLine for each line of data
     * @throws FileNotFoundException
     */
    public void parseFile() throws IOException {
       super.readFile();
    }

    @Override
    /**
     * Parses the line by calling each method to find all the data given in the file
     * and add the data to the StreetFinderAddress
     * @param line
     */
    protected void parseLine(String line) {
        StreetFinderAddress streetFinderAddress = new StreetFinderAddress();

        //Split the line by ","
        String[] splitLine = line.split(",");
        getStreetAndSuffix(splitLine[0], streetFinderAddress);          //0 street and suffix
        getLow(splitLine[1], streetFinderAddress);                      //1 low range
        getHigh(splitLine[2], streetFinderAddress);                     //2 high range
        getRangeType(splitLine[3], streetFinderAddress);                //3 parity
        getZip(splitLine[4], streetFinderAddress);                      //4 zipcode
        getTownship(splitLine[5], streetFinderAddress);                 //5 town
        getElectionDistrict(splitLine[9], streetFinderAddress);         //9 precinct / ed
        getSenateDistrict(splitLine[10], streetFinderAddress);          //10 senate district
        getAssemblyDistrict(splitLine[11], streetFinderAddress);        //11 assem district
        getLegislativeDistrict(splitLine[12], streetFinderAddress);     //12 legislative district
        getCongressionalDistrict(splitLine[13], streetFinderAddress);   //13 congressional district
        super.writeToFile(streetFinderAddress);
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
        if(super.checkForDirection(string[0])) {
            StreetFinderAddress.setPreDirection(string[0]);
            count++;
        }

        //get the street name (multiple words)
        //Assume that the last index of string[] is the street suffix
        StringBuilder temp = new StringBuilder();
        for(int i = count; i < string.length - 1; i++) {
            temp.append(string[i] + " ");
        }
        StreetFinderAddress.setStreet(temp.toString().trim());
        StreetFinderAddress.setStreetSuffix(string[string.length -1]);
    }

    /**
     * Gets the Low Range
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getLow(String splitLine, StreetFinderAddress StreetFinderAddress) {
        StreetFinderAddress.setBldg_low(splitLine);
    }

    /**
     * Gets the high Range
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getHigh(String splitLine, StreetFinderAddress StreetFinderAddress) {
        StreetFinderAddress.setBldg_high(splitLine);
    }

    /**
     * Gets the Range Type and converts to standard formatting
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getRangeType(String splitLine, StreetFinderAddress StreetFinderAddress) {
        if(splitLine.equals("Odd")) {
            StreetFinderAddress.setBldg_parity("ODDS");
        } else if(splitLine.equals("Even")) {
            StreetFinderAddress.setBldg_parity("EVENS");
        } else {
            StreetFinderAddress.setBldg_parity("ALL");
        }
    }

    /**
     * Gets the zipCode
     * @param splitLine
     * @param streetFinderAddress
     */
    private void getZip(String splitLine, StreetFinderAddress streetFinderAddress) {
        streetFinderAddress.setZip(splitLine);
    }

    private void getTownship(String splitline, StreetFinderAddress streetFinderAddress) {
        streetFinderAddress.setTown(splitline);
    }

    private void getElectionDistrict(String splitline, StreetFinderAddress streetFinderAddress) {
        streetFinderAddress.setED(splitline.substring(splitline.length() - 2));
    }

    private void getSenateDistrict(String splitline, StreetFinderAddress streetFinderAddress) {
        String[] district = splitline.split("-");
        streetFinderAddress.setSen(district[1]);
    }

    private void getAssemblyDistrict(String splitline, StreetFinderAddress streetFinderAddress) {
        String[] district = splitline.split("-");
        streetFinderAddress.setAsm(district[1]);
    }

    private void getLegislativeDistrict(String splitline, StreetFinderAddress streetFinderAddress) {
        String[] district = splitline.split("-");
        streetFinderAddress.setDist(district[1]);
    }

    private void getCongressionalDistrict(String splitline, StreetFinderAddress streetFinderAddress) {
        String[] district = splitline.split("-");
        streetFinderAddress.setCong(district[1]);
    }


}
