package gov.nysenate.sage.scripts.StreetFinder.Parsers;

import gov.nysenate.sage.model.address.StreetFinderAddress;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

/**
 * Parses Wyoming County 2018 .txt
 * Looks for townCode, pre-Direction, Street, street suffix, post-direction, city, low,high, range type, and zip
 * Parses using the location of each column in the line
 */
public class WyomingParser extends NTSParser{

    //location indexes are used to determine where data is
    private String file;
    private int townLocation = 0;
    private int DirLocation = 0;
    private int StreetLocation = 0;
    private int RoadTypeLocation = 0;
    private int PDLocation = 0;
    private int CityLocation = 0;
    private int FromLocation = 0;
    private int ThruLocation = 0;
    private int RangeTypeLocation = 0;
    private int ZipLocation = 0;

    /**
     * Calls the super constructor which sets up the tsv file
     * @param file
     * @throws IOException
     */
    public WyomingParser(String file) throws IOException {
        super(file);
        this.file = file;
    }


    /**
     * Parses the file for the needed information. Finds where the actual data is and then
     * parseLine is called to parse the line
     * @throws FileNotFoundException
     */
    public void parseFile() throws IOException {

        Scanner scanner = new Scanner(new File(file));
        String currentLine = scanner.nextLine();
        boolean inData = false;

        while(scanner.hasNext()) {
            //if inData
            if(inData) {
                //split the line by whitespace
                String[] splitLine = currentLine.split("\\s+");
                //if < 2 then it must be a blank line so skip
                if(splitLine.length < 2) {
                    //skip
                } else if(currentLine.contains("COMMISSIONERS OF ELECTIONS")) {
                    //signals the end of data
                    inData = false;
                } else {
                    parseLine(currentLine);
                }
                currentLine = scanner.nextLine();
            }
            else {
                //look for "DIR STREET" to signal that data is starting
                if(currentLine.contains("DIR STREET")) {
                    inData = true;
                    //if DirLocation is still 0 then all the location fields need to be set
                    if(DirLocation == 0) {
                        DirLocation = currentLine.indexOf("DIR");
                        StreetLocation = currentLine.indexOf("STREET");
                        RoadTypeLocation = currentLine.indexOf("TYPE");
                        PDLocation = currentLine.indexOf("PD");
                        CityLocation = currentLine.indexOf("CITY");
                        FromLocation = currentLine.indexOf("FROM");
                        ThruLocation = currentLine.indexOf("THRU");
                        RangeTypeLocation = currentLine.indexOf("O/E");
                        ZipLocation = currentLine.indexOf("ZIP");
                    }
                }
                //gets the next line
                currentLine = scanner.nextLine();
            }
        }
        scanner.close();
        super.closeWriters();
    }

    /**
     * Parses the line by calling all necessary helper methods
     * @param line
     */
    private void parseLine(String line) {
        StreetFinderAddress StreetFinderAddress = new StreetFinderAddress();

        //use the location of the columns at the top because they are all the same
        getTownCode(StreetFinderAddress, line);
        getPreDirection(StreetFinderAddress, line);
        getStreet(StreetFinderAddress, line);
        getStreetSuffix(StreetFinderAddress, line);
        getPostDirection(StreetFinderAddress, line);
        getTown(StreetFinderAddress, line);
        getLowRange(StreetFinderAddress, line);
        getHighRange(StreetFinderAddress, line);
        getRangeType(StreetFinderAddress, line);
        getZip(StreetFinderAddress, line);

        super.writeToFile(StreetFinderAddress);

    }

    /**
     * Gets the boe_townCode
     * @param StreetFinderAddress
     * @param line
     */
    private void getTownCode(StreetFinderAddress StreetFinderAddress, String line) {
        //go char by char until pre-Direction location
        StringBuilder temp = new StringBuilder();
        for(int i = townLocation; i < DirLocation; i++) {
            temp.append(line.charAt(i));
        }
        //trim any excess spacing
        StreetFinderAddress.setTownCode(temp.toString().trim());
    }

    /**
     * Gets the pre-Direction if there is one
     * @param StreetFinderAddress
     * @param line
     */
    private void getPreDirection(StreetFinderAddress StreetFinderAddress, String line) {
        //look from pre-Directionlocation to street location for a pre-Direction
        StringBuilder temp = new StringBuilder();
        for(int i = DirLocation; i < StreetLocation; i++) {
            temp.append(line.charAt(i));
        }
        //trim any whitespace
        //if there is no pre-Direction then this will be blank
        StreetFinderAddress.setPreDirection(temp.toString().trim());
    }

    /**
     * Gets the street name
     * @param StreetFinderAddress
     * @param line
     */
    private void getStreet(StreetFinderAddress StreetFinderAddress, String line) {
        //go char by char from street location until street suffix location
        StringBuilder temp = new StringBuilder();
        for(int i = StreetLocation; i < RoadTypeLocation; i++) {
            temp.append(line.charAt(i));
        }
        //trim any whitespace
        StreetFinderAddress.setStreet(temp.toString().trim());
    }

    /**
     * Gets the street suffix
     * @param StreetFinderAddress
     * @param line
     */
    private void getStreetSuffix(StreetFinderAddress StreetFinderAddress, String line) {
        //look from Road Type location to post-direction location
        StringBuilder temp = new StringBuilder();
        for(int i = RoadTypeLocation; i < PDLocation; i++) {
            temp.append(line.charAt(i));
        }
        //trim any whitespace
        StreetFinderAddress.setStreetSuffix(temp.toString().trim());
    }

    /**
     * Gets post-Direction if there is one
     * @param StreetFinderAddress
     * @param line
     */
    private void getPostDirection(StreetFinderAddress StreetFinderAddress, String line) {
        //look from post-direction location to the city location
        StringBuilder temp = new StringBuilder();
        for(int i = PDLocation; i < CityLocation; i++) {
            temp.append(line.charAt(i));
        }
        //trim any whitespace
        //will be blank if no post-Direction
        StreetFinderAddress.setPostDirection(temp.toString().trim());
    }

    /**
     * Gets the Town
     * @param StreetFinderAddress
     * @param line
     */
    private void getTown(StreetFinderAddress StreetFinderAddress, String line) {
        //look from city location to the From location
        StringBuilder temp = new StringBuilder();
        for(int i = CityLocation; i < FromLocation; i++) {
            temp.append(line.charAt(i));
        }
        //trim any whitespaces
        StreetFinderAddress.setTown(temp.toString().trim());
    }

    /**
     * gets the low range
     * @param StreetFinderAddress
     * @param line
     */
    private void getLowRange(StreetFinderAddress StreetFinderAddress, String line) {
        //look from From location to Thru location
        StringBuilder temp = new StringBuilder();
        for(int i = FromLocation; i < ThruLocation; i++) {
            temp.append(line.charAt(i));
        }
        //trim any whitespace
        StreetFinderAddress.setBldg_low(temp.toString().trim());
    }

    /**
     * Gets the high Range
     * @param StreetFinderAddress
     * @param line
     */
    private void getHighRange(StreetFinderAddress StreetFinderAddress, String line) {
        //look from Thru location to O/E location
        StringBuilder temp = new StringBuilder();
        for(int i = ThruLocation; i < RangeTypeLocation; i++) {
            temp.append(line.charAt(i));
        }
        //trim any whitespace
        StreetFinderAddress.setBldg_high(temp.toString().trim());
    }

    /**
     * Gets the range type and converts to standard format
     * @param StreetFinderAddress
     * @param line
     */
    private void getRangeType(StreetFinderAddress StreetFinderAddress, String line) {
        //look from O/E location to the zip location
        StringBuilder temp = new StringBuilder();
        for(int i = RangeTypeLocation; i < ZipLocation; i++) {
            temp.append(line.charAt(i));
        }
        //trim any whitespace and convert to correct format
        String string = temp.toString().trim();
        if(string.equals("O")) {
            StreetFinderAddress.setBldg_parity("ODDS");
        } else if(string.equals("E")) {
            StreetFinderAddress.setBldg_parity("EVENS");
        } else {
            StreetFinderAddress.setBldg_parity("ALL");
        }
    }

    /**
     * Gets the zip
     * @param StreetFinderAddress
     * @param line
     */
    private void getZip(StreetFinderAddress StreetFinderAddress, String line) {
        //look from zip location
        StringBuilder temp = new StringBuilder();
        for(int i = ZipLocation; i < ZipLocation + 5; i++) {
            temp.append(line.charAt(i));
        }
        StreetFinderAddress.setZip(temp.toString());
    }
}
