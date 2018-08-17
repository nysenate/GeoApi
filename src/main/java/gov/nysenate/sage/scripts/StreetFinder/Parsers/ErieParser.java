package gov.nysenate.sage.scripts.StreetFinder.Parsers;

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
        StreetFinderAddress StreetFinderAddress = new StreetFinderAddress();

        //Split the line by ","
        String[] splitLine = line.split(",");
        getStreetAndSuffix(splitLine, StreetFinderAddress);   //splitLine[0]
        getLow(splitLine, StreetFinderAddress);               //splitLine[1]
        getHigh(splitLine, StreetFinderAddress);              //splitLine[2]
        getRangeType(splitLine, StreetFinderAddress);         //splitLine[3]
        getTownCode(splitLine, StreetFinderAddress);          //splitLine[4]
        getDistrict(splitLine, StreetFinderAddress);          //splitLine[5]
        getZip(splitLine, StreetFinderAddress);               //splitLine[6]

        super.writeToFile(StreetFinderAddress);
    }

    /**
     * Gets the Street name and Street Suffix from a string containing both
     * Also checks for  pre-Direction
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getStreetAndSuffix(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        int count = 0;
        //split the string by spaces to get each individual word
        String[] string = splitLine[0].split("\\s+");

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
    private void getLow(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        StreetFinderAddress.setBldg_low(splitLine[1]);
    }

    /**
     * Gets the high Range
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getHigh(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        StreetFinderAddress.setBldg_high(splitLine[2]);
    }

    /**
     * Gets the Range Type and converts to standard formatting
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getRangeType(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        if(splitLine[3].equals("Odd")) {
            StreetFinderAddress.setBldg_parity("ODDS");
        } else if(splitLine[3].equals("Even")) {
            StreetFinderAddress.setBldg_parity("EVENS");
        } else {
            StreetFinderAddress.setBldg_parity("ALL");
        }
    }

    /**
     * Gets the townCode
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getTownCode(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        StreetFinderAddress.setTownCode(splitLine[4]);
    }

    /**
     * Gets the District
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getDistrict(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        StreetFinderAddress.setDist(splitLine[5]);
    }

    /**
     * Gets the zipCode
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getZip(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        StreetFinderAddress.setZip(splitLine[6]);
    }
}
