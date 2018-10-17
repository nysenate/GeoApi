package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.model.address.StreetFinderAddress;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Parses Westchester County 2018 file
 * Looks for town, pre-Direction, street, street suffix, post-direction, low, high, range type, zip, skips CNG, sen, asm, dist
 */
public class WestchesterParser extends NTSParser {

    private String file;

    /**
     * Calls the super constructor which sets up the tsv file
     * @param file
     * @throws IOException
     */
    public WestchesterParser(String file) throws IOException {
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
     * Parses the line by calling each helper method to extract all data
     * @param line
     */
    protected void parseLine(String line) {
        StreetFinderAddress StreetFinderAddress = new StreetFinderAddress();

        //split the line by ,
        String[] splitLine = line.split(",");

        getTown(splitLine, StreetFinderAddress);              //splitLine[0]
        //skip precinct                                 //splitLine[1]
        getPreDirection(splitLine, StreetFinderAddress);      //splitLine[2]
        getStreet(splitLine, StreetFinderAddress);            //splitLine[3]
        getStreetSuffix(splitLine, StreetFinderAddress);      //splitLine[4]
        getPostDirection(splitLine, StreetFinderAddress);     //splitLine[5]
        getLow(splitLine, StreetFinderAddress);               //splitLine[6]
        getHigh(splitLine, StreetFinderAddress);              //splitLine[7]
        getRangeType(splitLine, StreetFinderAddress);         //splitLine[8]
        getZip(splitLine, StreetFinderAddress);               //splitLine[9]
        getCong(splitLine, StreetFinderAddress);                 //splitLine[10]
        getSen(splitLine, StreetFinderAddress);               //splitLine[11]
        getAsm(splitLine, StreetFinderAddress);               //splitLine[12]
        getCle(splitLine, StreetFinderAddress);          //splitLine[13]
        //ignore CNL-DT
        super.writeToFile(StreetFinderAddress);
    }

    /**
     * Gets the town
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getTown(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        StreetFinderAddress.setTown(splitLine[0]);
    }

    /**
     * Gets the Pre-Direction
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getPreDirection(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        StreetFinderAddress.setPreDirection(splitLine[2]);
    }

    /**
     * Gets the Street name
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getStreet(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        StreetFinderAddress.setStreet(splitLine[3]);
    }

    /**
     * Gets the street Suffix
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getStreetSuffix(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        StreetFinderAddress.setStreetSuffix(splitLine[4]);
    }

    /**
     * Gets the post-Direction
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getPostDirection(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        StreetFinderAddress.setPostDirection(splitLine[5]);
    }

    /**
     * Gets the low Range
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getLow(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        StreetFinderAddress.setBldg_low(splitLine[6]);
    }

    /**
     * gets the High Range
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getHigh(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        StreetFinderAddress.setBldg_high(splitLine[7]);
    }

    /**
     * Gets the Range type and converts to standard formatting
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getRangeType(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        if(splitLine[8].equals("O")) {
            StreetFinderAddress.setBldg_parity("ODDS");
        } else if(splitLine[8].equals("E")) {
            StreetFinderAddress.setBldg_parity("EVENS");
        } else {
            StreetFinderAddress.setBldg_parity("ALL");
        }
    }

    /**
     * Gets the zip
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getZip(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        StreetFinderAddress.setZip(splitLine[9]);
    }

    /**
     * Gets the Cong. Also gets rid of the "CD-"
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getCong(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        String[] string = splitLine[10].split("-");
        StreetFinderAddress.setCong(string[1]);
    }

    /**
     * Gets the sen. Also gets rid of the "SD-"
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getSen(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        String[] string = splitLine[11].split("-");
        StreetFinderAddress.setSen(string[1]);
    }

    /**
     * Gets the asm. Also gets rid of the "AD-"
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getAsm(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        String[] string = splitLine[12].split("-");
        StreetFinderAddress.setAsm(string[1]);
    }

    /**
     * Gets the cleg. Also gets rid of the "LD-"
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getCle(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        String[] string = splitLine[13].split("-");
        StreetFinderAddress.setCle(string[1]);
    }

}
