package gov.nysenate.sage.scripts.StreetFinder.Parsers;

import gov.nysenate.sage.model.address.StreetFinderAddress;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * Parses NYC Street files. It does so by using column locations for 3 different data columsn within one line
 * Output is a tsv file
 */
public class NYCParser extends NTSParser {

    //fields. Most are to save location of columns within the line
    private String file;
    private String street1, street2, street3;
    private String streetSuffix1, streetSuffix2, streetSuffix3;
    private StreetFinderAddress StreetFinderAddress1, StreetFinderAddress2, StreetFinderAddress3;
    private String preDir1 = "", preDir2 = "", preDir3 = "";
    //location fields
    private int start1, start2, start3;
    private int ED1 = 0, AD1, ZIP1, CD1, SD1, MC1, CO1, FROM1, TO1;
    private int ED2, AD2, ZIP2, CD2, SD2, MC2, CO2, FROM2, TO2;
    private int ED3, AD3, ZIP3, CD3, SD3, MC3, CO3, FROM3, TO3;
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
                    //set all location fields for column 1 (first occurrence)
                    ED1 = currentLine.indexOf("ED");
                    AD1 = currentLine.indexOf("AD");
                    ZIP1 = currentLine.indexOf("ZIP");
                    CD1 = currentLine.indexOf("CD");
                    SD1 = currentLine.indexOf("SD");
                    MC1 = currentLine.indexOf("MC");
                    CO1 = currentLine.indexOf("CO");
                    FROM1 = currentLine.indexOf("FROM");
                    TO1 = currentLine.indexOf("TO");
                    //set all loction fields for column 2 (second occurrence)
                    ED2 = currentLine.indexOf("ED", currentLine.indexOf("ED") + 1);
                    AD2 = currentLine.indexOf("AD", currentLine.indexOf("AD") + 1);
                    ZIP2 = currentLine.indexOf("ZIP", currentLine.indexOf("ZIP") + 1);
                    CD2 = currentLine.indexOf("CD", currentLine.indexOf("CD") + 1);
                    SD2 = currentLine.indexOf("SD", currentLine.indexOf("SD") + 1);
                    MC2 = currentLine.indexOf("MC", currentLine.indexOf("MC") + 1);
                    CO2 = currentLine.indexOf("CO", currentLine.indexOf("CO") + 1);
                    FROM2 = currentLine.indexOf("FROM", currentLine.indexOf("FROM") + 1);
                    TO2 = currentLine.indexOf("TO", currentLine.indexOf("TO") + 1);
                    //set all loction fields for column 3 (third occurrence)
                    ED3 = currentLine.lastIndexOf("ED");
                    AD3 = currentLine.lastIndexOf("AD");
                    ZIP3 = currentLine.lastIndexOf("ZIP");
                    CD3 = currentLine.lastIndexOf("CD");
                    SD3 = currentLine.lastIndexOf("SD");
                    MC3 = currentLine.lastIndexOf("MC");
                    CO3 = currentLine.lastIndexOf("CO");
                    FROM3 = currentLine.lastIndexOf("FROM");
                    TO3 = currentLine.lastIndexOf("TO");

                    //if the line has "__________________________________________" then use that to set the column starting locations
                } else if (currentLine.contains("__________________________________________")) {
                    start1 = currentLine.indexOf("__________________________________________");
                    start2 = currentLine.indexOf("__________________________________________", start1 + 1);
                    start3 = currentLine.lastIndexOf("__________________________________________");
                } else {
                    //otherwise it is a parseable line of data
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
                substring = line.substring(start1, start2);
            }
            else {
                substring = line.substring(start1, start2 - 1);
            }

        }

        //split substring by whitespace
        String string[] = substring.split("\\s+");

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

                //street name, suffix and pre-Direction must have already occured and are stored in the corresponding storage fields
                StreetFinderAddress1 = new StreetFinderAddress();
                StreetFinderAddress1.setStreet(street1);
                StreetFinderAddress1.setStreetSuffix(streetSuffix1);
                StreetFinderAddress1.setPreDirection(preDir1);
                //get all data in here
                //first check for a from and for a to
                if (checkForFrom(line, FROM1)) {
                    //handle case when its 1/2
                    //ex. 2033 1/2
                    String temp = string[index];
                    if(string[index + 1].equals("1/2")) {
                        //skip over 1/2 for now because database is expecting all ints
                        //temp += " " + 1/2;
                        index++;
                    }
                    StreetFinderAddress1.setBldg_low(temp);
                    index++;
                }

                //check for to
                if (checkForTo(line, TO1)) {
                    //handle case when its 1/2
                    //ex. 2033 1/2
                    String temp = string[index];
                    if(string[index + 1].equals("1/2")) {
                        //skip over 1/2 for now because database is expecting all ints
                        //temp += " " + 1/2;
                        index++;
                    }
                    StreetFinderAddress1.setBldg_high(temp);
                    index++;
                }

                //check for election code
                if (checkForED(line, ED1)) {
                    StreetFinderAddress1.setED(string[index]);
                    index++;
                }

                //check for assembly code
                if (checkForAD(line, AD1)) {
                    StreetFinderAddress1.setAsm(string[index]);
                    index++;
                }

                //check for zip code
                if (checkFor(line, ZIP1)) {
                    StreetFinderAddress1.setZip(string[index]);
                    index++;
                }

                //check for congressional code
                if (checkFor(line, CD1)) {
                    StreetFinderAddress1.setCong(string[index]);
                    index++;
                }

                //check for senate code
                if (checkFor(line, SD1)) {
                    StreetFinderAddress1.setSen(string[index]);
                    index++;
                }

                //check for MC1 but skip over because that data is unneccessary as of now
                if (checkFor(line, MC1)) {
                    //StreetFinderAddress1.setBldg_high(string[index]);
                    index++;
                }

                //check for CO1 but skip over because that data is unneccessary as of now
                if (checkFor(line, CO1)) {
                    //StreetFinderAddress1.setBldg_high(string[index]);
                    index++;
                }

                //set the town then write to file
                StreetFinderAddress1.setTown(town);
                super.writeToFile(StreetFinderAddress1);


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
                    //keep streetSuffix blank if there isnt one
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
            String string[] = substring.trim().split("\\s+");

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

                    //street name, suffix and pre-Direction must have already occured and are stored in the corresponding storage fields
                    StreetFinderAddress2 = new StreetFinderAddress();
                    StreetFinderAddress2.setStreet(street2);
                    StreetFinderAddress2.setStreetSuffix(streetSuffix2);
                    StreetFinderAddress2.setPreDirection(preDir2);

                    //check for from
                    if (checkForFrom(line, FROM2)) {
                        //handle case when its 1/2
                        //ex. 2033 1/2
                        String temp = string[index];
                        if(string[index + 1].equals("1/2")) {
                            //skip over 1/2 for now because database is expecting all ints
                            //temp += " " + 1/2;
                            index++;
                        }
                        StreetFinderAddress2.setBldg_low(temp);
                        index++;
                    }

                    //check for to
                    if (checkForTo(line, TO2)) {
                        //handle case when its 1/2
                        //ex. 2033 1/2
                        String temp = string[index];
                        if(string[index + 1].equals("1/2")) {
                            //skip over 1/2 for now because database is expecting all ints
                            //temp += " " + 1/2;
                            index++;
                        }
                        StreetFinderAddress2.setBldg_high(temp);
                        index++;
                    }

                    //check for election code
                    if (checkForED(line, ED2)) {
                        StreetFinderAddress2.setED(string[index]);
                        index++;
                    }

                    //check for assembly code
                    if (checkForAD(line, AD2)) {
                        StreetFinderAddress2.setAsm(string[index]);
                        index++;
                    }

                    //check for zip code
                    if (checkFor(line, ZIP2)) {
                        StreetFinderAddress2.setZip(string[index]);
                        index++;
                    }

                    //chek for congressional code
                    //special CD check because CD2 for some reason needs to check 1 extra space over to find the data
                    if (checkForCD2(line, CD2)) {
                        StreetFinderAddress2.setCong(string[index]);
                        index++;
                    }

                    //check for senate code
                    if (checkFor(line, SD2)) {
                        StreetFinderAddress2.setSen(string[index]);
                        index++;
                    }

                    //check for MC2 but skip over because that data is unneccessary as of now
                    if (checkFor(line, MC2)) {
                        //StreetFinderAddress1.setBldg_high(string[index]);
                        index++;
                    }

                    //check for CO2 but skip over because that data is unneccessary as of now
                    if (checkFor(line, CO2)) {
                        //StreetFinderAddress1.setBldg_high(string[index]);
                        index++;
                    }

                    //set town and write to file
                    StreetFinderAddress2.setTown(town);
                    super.writeToFile(StreetFinderAddress2);

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
                        //keep streetSuffix blank if there isnt one
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
            String string[] = substring.trim().split("\\s+");
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

                    //street name, suffix and pre-Direction must have already occured and are stored in the corresponding storage fields
                    StreetFinderAddress3 = new StreetFinderAddress();
                    StreetFinderAddress3.setStreet(street3);
                    StreetFinderAddress3.setStreetSuffix(streetSuffix3);
                    StreetFinderAddress3.setPreDirection(preDir3);

                    //check for a from
                    if (checkForFrom(line, FROM3)) {
                        //handle case when its 1/2
                        //ex. 2033 1/2
                        String temp = string[index];
                        if(string[index + 1].equals("1/2")) {
                            //skip over 1/2 for now because database is expecting all ints
                            //temp += " " + 1/2;
                            index++;
                        }
                        StreetFinderAddress3.setBldg_low(temp);
                        index++;
                    }

                    //check for a to
                    if (checkForTo(line, TO3)) {
                        //handle case when its 1/2
                        //ex. 2033 1/2
                        String temp = string[index];
                        if(string[index + 1].equals("1/2")) {
                            //skip over 1/2 for now because database is expecting all ints
                            //temp += " " + 1/2;
                            index++;
                        }
                        StreetFinderAddress3.setBldg_high(temp);
                        index++;
                    }

                    //check for an election code
                    if (checkFor(line, ED3)) {
                        StreetFinderAddress3.setED(string[index]);
                        index++;
                    }

                    //check for an assembly code
                    if (checkForAD(line, AD3)) {
                        StreetFinderAddress3.setAsm(string[index]);
                        index++;
                    }

                    //check for a zip code
                    if (checkFor(line, ZIP3)) {
                        StreetFinderAddress3.setZip(string[index]);
                        index++;
                    }

                    //check for a congressional code
                    if (checkFor(line, CD3)) {
                        StreetFinderAddress3.setCong(string[index]);
                        index++;
                    }

                    //check for a senate code
                    if (checkFor(line, SD3)) {
                        StreetFinderAddress3.setSen(string[index]);
                        index++;
                    }

                    //check for MC3 but skip over because that data is unneccessary as of now
                    if (checkFor(line, MC3)) {
                        //StreetFinderAddress3.setBldg_high(string[index]);
                        index++;
                    }

                    if (checkFor(line, CO3)) {
                        //StreetFinderAddress3.setBldg_high(string[index]);
                        index++;
                    }

                    //check for CO3 but skip over because that data is unneccessary as of now
                    StreetFinderAddress3.setTown(town);
                    super.writeToFile(StreetFinderAddress3);


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
                        //keep streetSuffix blank if there isnt one
                        streetSuffix3 = "";
                        //street name must be in string[0]
                        street3 = string[0];
                    }
                }
            }
        }
    }

    /**
     * Checks for a from (low range) stating at the fromLocation for 6 spaces on the line
     * @param line
     * @param fromLocation - location of the from Column
     * @return true if data is found. false otherwise
     */
    private boolean checkForFrom(String line, int fromLocation) {
        for (int i = fromLocation; i < fromLocation + 6; i++) {
            if (!Character.isWhitespace(line.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks for a to (high range) starting at toLocation - 2 for 6 spaces
     * @param line
     * @param toLocation
     * @return true if data is found. false otherwise
     */
    private boolean checkForTo(String line, int toLocation) {
        int backwards = (toLocation - 2 < 0) ? 0 : toLocation - 2;
        for (int i = toLocation - backwards; i < toLocation + 4; i++) {
            if (!Character.isWhitespace(line.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Generic check to find data at a column location starting at location - 1 and going for 3 spaces
     * @param line
     * @param location
     * @return true if data is found. false otherwise
     */
    private boolean checkFor(String line, int location) {
        int backwards = (location - 1 < 0) ? 0 : location - 1;
        for (int i = location - backwards; i < location + 2; i++) {
            if (!Character.isWhitespace(line.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks for data in the CD2 column. CD2 requires the check to go out an extra space to find all the data
     * @param line
     * @param CD2
     * @return true if data is found. false otherwise
     */
    private boolean checkForCD2(String line, int CD2) {
        int backwards = (CD2 - 1 < 0) ? 0 : CD2 - 1;
        for (int i = CD2 - backwards; i < CD2 + 3; i++) {
            if (!Character.isWhitespace(line.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks for data in the ED column
     * @param line
     * @param ED
     * @return true if data is found. false otherwise
     */
    private boolean checkForED(String line, int ED) {
        for (int i = ED; i < ED + 3; i++) {
            if (!Character.isWhitespace(line.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks for data in the AD column
     * @param line
     * @param AD
     * @return true if data is found. false otherwise
     */
    private boolean checkForAD(String line, int AD) {
        for (int i = AD; i < AD + 2; i++) {
            if (!Character.isWhitespace(line.charAt(i))) {
                return true;
            }
        }
        return false;
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
