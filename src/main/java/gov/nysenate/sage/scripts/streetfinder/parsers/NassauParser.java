package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.model.address.StreetFinderAddress;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Parses Nassau County csv file and outputs a tsv file
 */
public class NassauParser extends NTSParser {

    private String file;

    /**
     * Calls the super constructor which sets up the tsv output file
     * @param file
     * @throws IOException
     */
    public NassauParser(String file) throws IOException {
        super(file);
        this.file = file;
    }

    @Override
    /**
     * Parses the file by calling parseLine for each line of data
     * @throws FileNotFoundException
     */ public void parseFile() throws IOException {

       super.readFile();
    }

    @Override
    /**
     * Parses the line by calling each helper method to extract all data
     * @param line
     */
    protected void parseLine(String line) {
        StreetFinderAddress StreetFinderAddress = new StreetFinderAddress();

        //split the line by ,
        String[] splitLine = line.split(",");

        getED(splitLine, StreetFinderAddress);                //skip precinct splitLine[0]
        getStreet(splitLine, StreetFinderAddress);            //splitLine[1]
        getSuffix(splitLine, StreetFinderAddress);            //splitLine[2]
        getTown(splitLine, StreetFinderAddress);              //splitLine[3]
        getZip(splitLine, StreetFinderAddress);               //splitLine[4]
        getLow(splitLine, StreetFinderAddress);               //splitLine[5]
        getHigh(splitLine, StreetFinderAddress);              //splitLine[6]
        getRangeType(splitLine, StreetFinderAddress);         //splitLine[7]
        getWard(splitLine, StreetFinderAddress);              //splitLine[8]
        getCong(splitLine, StreetFinderAddress);              //splitLine[9]
        getSen(splitLine, StreetFinderAddress);               //splitLine[10]
        getAsm(splitLine, StreetFinderAddress);               //splitLine[11]
        getCleg(splitLine, StreetFinderAddress);              //splitLine[12]
        //ignore TD
        super.writeToFile(StreetFinderAddress);
    }

    /**
     * Gets the street name
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getStreet(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        StreetFinderAddress.setStreet(splitLine[1]);
    }

    /**
     * Gets the street suffix. Also checks for any post-Directions
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getSuffix(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        //suffix is everything in splitLine[2] - streetName which is already saved in the StreetFinderAddress
        String streetSuffix = splitLine[2].replace(StreetFinderAddress.getStreet(), " ").trim();
        String[] splitString = streetSuffix.split("\\s+");
        //if the length is > 1 then there must be a pre or post direction
        if(splitString.length > 1) {
            if(super.checkForDirection(splitString[1])) {
                StreetFinderAddress.setPostDirection(splitString[1]);
                StreetFinderAddress.setStreetSuffix(splitString[0]);
            } else if(super.checkForDirection(splitString[0])) {
                StreetFinderAddress.setPreDirection(splitString[0]);
                StreetFinderAddress.setStreetSuffix(splitString[1]);
            }
        } else {
            //only the street suffix
            StreetFinderAddress.setStreetSuffix(splitString[0]);
        }
    }

    /**
     * Pulls ED out of precinct. It is the last 3 characters of the precinct
     * @param splitLine
     * @param streetFinderAddress
     */
    private void getED(String[] splitLine, StreetFinderAddress streetFinderAddress) {
        String ed = splitLine[0];
        ed = ed.substring(ed.length() - 3);
        streetFinderAddress.setED(ed);
    }

    /**
     * Gets the town name
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getTown(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        StreetFinderAddress.setTown(splitLine[3]);
    }

    /**
     * Gets the zip code
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getZip(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        StreetFinderAddress.setZip(splitLine[4]);
    }

    /**
     * Gets the low range
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getLow(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        StreetFinderAddress.setBldg_low(splitLine[5]);
    }

    /**
     * Gets the high range
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getHigh(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        StreetFinderAddress.setBldg_high(splitLine[6]);
    }

    /**
     * Gets the range type and converts it to the correct format
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getRangeType(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        if(splitLine[7].equals("O")) {
            StreetFinderAddress.setBldg_parity("ODDS");
        } else if(splitLine[7].equals("E")) {
            StreetFinderAddress.setBldg_parity("EVENS");
        } else {
            StreetFinderAddress.setBldg_parity("ALL");
        }
    }

    /**
     * Gets the ward
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getWard(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        StreetFinderAddress.setWard(this.trim(splitLine[8]));
    }

    /**
     * Gets the cong code
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getCong(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        StreetFinderAddress.setCong(this.trim(splitLine[9]));
    }

    /**
     * Gets the sen code
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getSen(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        StreetFinderAddress.setSen(this.trim(splitLine[10]));
    }

    /**
     * Gets the asm code
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getAsm(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        StreetFinderAddress.setAsm(this.trim(splitLine[11]));
    }

    /**
     * Gets the cleg code
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getCleg(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        StreetFinderAddress.setCle(this.trim(splitLine[12]));
    }

    /**
     * Trims off the first part of a string
     * ex. TW-01 trims off the TW-
     * and returns 01
     * @param string
     * @return
     */
    private String trim(String string) {
        //split by "-"
        String[] temp = string.split("-");
        //return the number after the -
        return temp[1];
    }
}
