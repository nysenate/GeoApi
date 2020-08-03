package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.model.address.StreetFinderAddress;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Parses NYC Street files. It does so by using column locations for 3 different data columns within one line
 * Output is a tsv file
 */
public class NYCParser extends NTSParser {

    //fields. Most are to save location of columns within the line
    private String file;
    private String street1, street2, street3;
    private String streetSuffix1, streetSuffix2, streetSuffix3;
    private StreetFinderAddress streetFinderAddress1, streetFinderAddress2, streetFinderAddress3;
    private String preDir1 = "", preDir2 = "", preDir3 = "";
    //location fields
    private int start1, start2, start3;
    private String town;

    /**
     * Calls the super constructor to set up the tsv file.
     * Also sets the town which is the same for the entire file
     * @param file
     * @throws IOException
     */
    public NYCParser(String file) throws IOException {
        super(file);
        this.file = file;
        town = this.getTown();
    }

    /**
     * parses the file by calling parseLine for each line of data. It also sets all of the locations of the column location fields when necessary
     * @throws IOException
     */
    public void parseFile() throws IOException {
        Scanner scanner = new Scanner(new File(file));
        String currentLine;

        while (scanner.hasNext()) {
            currentLine = scanner.nextLine();

            //if it is not a skip over line
            if (currentLine.trim().split("\\s+").length > 1 && !currentLine.contains("Information") && !currentLine.contains("Reproduction") &&
                    !currentLine.contains("STREET  FINDER LIST") && !currentLine.contains("TOTIN") && !currentLine.matches("\\s*Board\\s+of\\s+Elections\\s*")
                    && !currentLine.matches("\\s*V\\s+TE\\s+NYC\\s*") &&
                    !currentLine.matches("\\s*STATEN\\s+ISLAND\\s*") &&
                    !currentLine.matches("\\s*STREET\\s+FINDER\\s*")) {
                //if the line contains "FROM" then it is the top of a data segment so set all column field locations
                if (currentLine.contains("FROM")) {
                    continue;
                    //if the line has "__________________________________________" then use that to set the column starting locations
                } else if (currentLine.contains("__________________________________________")) {
                    start1 = currentLine.indexOf("__________________________________________");
                    start2 = currentLine.indexOf("__________________________________________", start1 + 1);
                    start3 = currentLine.lastIndexOf("__________________________________________");
                } else {
                    //otherwise it is a parse able line of data
                    parseColumn1(currentLine);
                    parseColumn2(currentLine);
                    parseColumn3(currentLine);
                }
            }
        }
        //close all writers/readers
        scanner.close();
        super.closeWriters();
    }

    private void handleNineDataPoints(StreetFinderAddress streetFinderAddress, String[] data) {
        //FROM TO ED AD ZIP CD SD MC CO
        streetFinderAddress.setBldg_low(data[0].replaceAll("1/2",""));
        streetFinderAddress.setBldg_high(data[1].replaceAll("1/2",""));
        streetFinderAddress.setED(data[2]);
        streetFinderAddress.setAsm(data[3]);
        streetFinderAddress.setZip(data[4]);
        streetFinderAddress.setCong(data[5]);
        streetFinderAddress.setSen(data[6]);
    }

    private void handleEightDataPoints(StreetFinderAddress streetFinderAddress, String[] data) {
        //FROM ED AD ZIP CD SD MC CO
        streetFinderAddress.setBldg_low(data[0].replaceAll("1/2",""));
        streetFinderAddress.setED(data[1]);
        streetFinderAddress.setAsm(data[2]);
        streetFinderAddress.setZip(data[3]);
        streetFinderAddress.setCong(data[4]);
        streetFinderAddress.setSen(data[5]);
    }

    private void handleSevenDataPoints(StreetFinderAddress streetFinderAddress, String[] data) {
        //ED AD ZIP CD SD MC CO
        streetFinderAddress.setED(data[0]);
        streetFinderAddress.setAsm(data[1]);
        streetFinderAddress.setZip(data[2]);
        streetFinderAddress.setCong(data[3]);
        streetFinderAddress.setSen(data[4]);
    }

    private String[] removeEmptyData(String[] input) {
        List<String> list = new ArrayList<String>(Arrays.asList(input));
        list.removeAll(Arrays.asList("", null," "));
        String[] refinedData = new String[list.size()];
        refinedData = list.toArray(refinedData);
        return refinedData;
    }

    /**
     * Parses column 1 using the column 1 location fields
     * @param line
     */
    private void parseColumn1(String line) {
        String substring;
        //check if the line goes up to the start location of column 2
        if (line.length() < start2 - 1) {
            substring = line.substring(start1, line.length());
        } else {
            //if it doesnt then substring can go all the way until start2 - 1
            if (start2 - 1 < 0) {
                substring = line.substring(0, start2);
            }
            else {
                substring = line.substring(0, start2 - 1);
            }

        }

        //split substring by whitespace
        String[] string = substring.trim().split("\\s+");
        string = removeEmptyData(string);

        int index = 0;
        //check for a blank column line
        if (string.length > 1) {
            //for some reason string[0] is sometimes just a blank space. skip over this if it is there
            if (string[0].trim().equals("")) {
                index++;
            }
            //the line can either be just a street name or it can be all the other data
            //if the 1st two indexes of the line matches the pattern of some numbers, possibly a -, and possibly a letter
            //Then these are the from and the to or another number data field
            // which means that this is not the street name
            //Also check for a special case in which the from has a 1/2. ex. "49 1/2"
            if (string[index].matches("\\d+[-]?\\d*[A-Z]?") && string[index + 1].matches("\\d+[-]?\\d*[A-Z]?")
                    || string[index].matches("\\d+[-]?\\d*[A-Z]?") && string[index + 1].matches("1/2")) {

                //street name, suffix and pre-Direction must have already occurred and are stored in the corresponding storage fields
                streetFinderAddress1 = new StreetFinderAddress();
                streetFinderAddress1.setStreet(street1);
                streetFinderAddress1.setStreetSuffix(streetSuffix1);
                streetFinderAddress1.setPreDirection(preDir1);
                boolean handledData = false;
                //get all data in here
                //first check for a from and for a to

                if (string.length == 9) {
                    handleNineDataPoints(streetFinderAddress1, string);
                    handledData = true;
                }
                if (string.length == 8) {
                    handleEightDataPoints(streetFinderAddress1, string);
                    handledData = true;
                }
                if (string.length == 7) {
                    handleSevenDataPoints(streetFinderAddress1, string);
                    handledData = true;
                }

                //set the town then write to file
                if (handledData && street1 != null) {
                    streetFinderAddress1.setTown(town);
                    super.writeToFile(streetFinderAddress1);
                }


            } else {
                //must be a street address
                String temp = "";
                //check for a pre-Direction
                if (super.checkForDirection(string[index])) {
                    preDir1 = string[index];
                    index++;
                } else {
                    //otherwise keep preDir1 blank
                    preDir1 = "";
                }
                //Get the street name and suffix
                if(string.length > 1) {
                    streetSuffix1 = string[string.length - 1];
                    //Get the street name
                    for (int i = index; index < string.length - 1; index++) {
                        temp += string[index] + " ";
                    }
                    street1 = temp.trim();
                } else {
                    //keep streetSuffix blank if there isn't one
                    streetSuffix1 = "";
                    //street name must be in string[0]
                    street1 = string[0];
                }
            }
        }
    }

    /**
     * Parses column 2 using the column 2 location fields
     * @param line
     */
    private void parseColumn2(String line) {
        String substring;
        //check that the line doesnt end after column 1
        if (line.length() > start2) {
            //check if the line is shorter than start3 - 1
            if (line.length() < start3 - 1) {
                //shorter so use line.length() to make a substring
                substring = line.substring(start2, line.length());
            } else {
                //longer spo use start3 - 1
                if (start3 - 1 < 0) {
                    substring = line.substring(start2, start3);
                }
                else {
                    substring = line.substring(start2, start3 - 1);
                }
            }
            //split by whitespace
            String[] string = substring.trim().split("\\s+");
            string = removeEmptyData(string);

            int index = 0;

            //check for a blank column line
            if (string.length > 1) {
                //for some reason string[0] is sometimes just a blank space. So skip it if it is
                if (string[0].trim().equals("")) {
                    index++;
                }

                //the line can either be just a street name or it can be all the other data
                //if the 1st two indexes of the line matches the pattern of some numbers, possibly a -, and possibly a letter
                //Then these are the from and the to or another number data field
                // which means that this is not the street name
                //Also check for a special case in which the from has a 1/2. ex. "49 1/2"
                if (string[index].matches("\\d+[-]?\\d*[A-Z]?") && string[index + 1].matches("\\d+[-]?\\d*[A-Z]?")
                        || string[index].matches("\\d+[-]?\\d*[A-Z]?") && string[index + 1].matches("1/2")) {

                    //street name, suffix and pre-Direction must have already occurred and are stored in the corresponding storage fields
                    streetFinderAddress2 = new StreetFinderAddress();
                    streetFinderAddress2.setStreet(street2);
                    streetFinderAddress2.setStreetSuffix(streetSuffix2);
                    streetFinderAddress2.setPreDirection(preDir2);


                    boolean handledData = false;
                    //get all data in here
                    //first check for a from and for a to

                    if (string.length == 9) {
                        handleNineDataPoints(streetFinderAddress2, string);
                        handledData = true;
                    }
                    if (string.length == 8) {
                        handleEightDataPoints(streetFinderAddress2, string);
                        handledData = true;
                    }
                    if (string.length == 7) {
                        handleSevenDataPoints(streetFinderAddress2, string);
                        handledData = true;
                    }

                    if (handledData && street2 != null) {
                        //set town and write to file
                        streetFinderAddress2.setTown(town);
                        super.writeToFile(streetFinderAddress2);
                    }

                } else {
                    //must be a street address
                    String temp = "";
                    //check for a pre-Direction
                    if (super.checkForDirection(string[index])) {
                        preDir2 = string[index];
                        index++;
                    } else {
                        //otherwise keep preDir2 blank
                        preDir2 = "";
                    }
                    //Get the street name and suffix
                    if(string.length > 1) {
                        streetSuffix2 = string[string.length - 1];
                        //Get the street name
                        for (int i = index; index < string.length - 1; index++) {
                            temp += string[index] + " ";
                        }
                        street2 = temp.trim();
                    } else {
                        //keep streetSuffix blank if there isn't one
                        streetSuffix2 = "";
                        //street name must be in string[0]
                        street2 = string[0];
                    }
                }
            }
        }
    }

    /**
     * Parses column 3 using the column 3 location fields
     * @param line
     */
    private void parseColumn3(String line) {
        String substring;
        //check to make sure that the line doesnt end before start of column 3
        if (line.length() > start3) {
            //create a substring of column 3 to the end of the line and split by whitespace
            substring = line.substring(start3, line.length());
            String[] string = substring.trim().split("\\s+");
            string = removeEmptyData(string);
            int index = 0;
            //check for a blank column line
            if (string.length > 1) {
                //for some reason string[0] is sometimes just a blank space. So skip it if it is
                if (string[0].trim().equals("")) {
                    index++;
                }
                //the line can either be just a street name or it can be all the other data
                //if the 1st two indexes of the line matches the pattern of some numbers, possibly a -, and possibly a letter
                //Then these are the from and the to or another number data field
                // which means that this is not the street name
                //Also check for a special case in which the from has a 1/2. ex. "49 1/2"
                if (string[index].matches("\\d+[-]?\\d*[A-Z]?") && string[index + 1].matches("\\d+[-]?\\d*[A-Z]?")
                        || string[index].matches("\\d+[-]?\\d*[A-Z]?") && string[index + 1].matches("1/2")) {

                    //street name, suffix and pre-Direction must have already occurred and are stored in the corresponding storage fields
                    streetFinderAddress3 = new StreetFinderAddress();
                    streetFinderAddress3.setStreet(street3);
                    streetFinderAddress3.setStreetSuffix(streetSuffix3);
                    streetFinderAddress3.setPreDirection(preDir3);

                    boolean handledData = false;
                    //get all data in here
                    //first check for a from and for a to

                    if (string.length == 9) {
                        handleNineDataPoints(streetFinderAddress3, string);
                        handledData = true;
                    }
                    if (string.length == 8) {
                        handleEightDataPoints(streetFinderAddress3, string);
                        handledData = true;
                    }
                    if (string.length == 7) {
                        handleSevenDataPoints(streetFinderAddress3, string);
                        handledData = true;
                    }

                    if (handledData && street3 != null) {
                        streetFinderAddress3.setTown(town);
                        super.writeToFile(streetFinderAddress3);
                    }
                } else {
                    //must be a street address
                    String temp = "";
                    //check for a pre-Direction
                    if (super.checkForDirection(string[index])) {
                        preDir3 = string[index];
                        index++;
                    } else {
                        //otherwise keep preDir3 blank
                        preDir3 = "";
                    }
                    //Get the street name and suffix
                    if (string.length > 1) {
                        streetSuffix3 = string[string.length - 1];
                        //Get the street name
                        for (int i = index; index < string.length - 1; index++) {
                            temp += string[index] + " ";
                        }
                        street3 = temp.trim();
                    } else {
                        //keep streetSuffix blank if there isn't one
                        streetSuffix3 = "";
                        //street name must be in string[0]
                        street3 = string[0];
                    }
                }
            }
        }
    }

    /**
     * Gets the town by using the file name. All data in the file has the same town
     * @return
     */
    private String getTown() {
        if(file.contains("Bronx")) {
            return "BRONX";
        } else if(file.contains("Brooklyn")) {
            return "BROOKLYN";
        } else if(file.contains("Manhattan")) {
            return "MANHATTAN";
        } else if(file.contains("Queens")) {
            return "QUEENS";
        } else {
            return "STATEN ISLAND";
        }
    }
}
