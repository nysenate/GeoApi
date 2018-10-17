package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.model.address.StreetFinderAddress;
import gov.nysenate.sage.util.AddressDictionary;
import java.io.*;
import java.util.Scanner;

/**
 * Parses files in the base NTS format (see Albany, Broome Counties as example)
 * Output is a tsv file
 */
public class NTSParser {

    //These are used to save the variables when the line is a continuation of an above street
    private StreetFinderAddress StreetFinderAddressStorage = new StreetFinderAddress();
    private String file;
    private String overloadStreetSuf;
    //storage for column locations
    private int schIndex;
    private int villIndex;
    private int cleIndex;
    private int fireIndex;
    private int ccIndex;
    private int cityIndex;
    //tsv file writers
    private FileWriter fileWriter;
    private PrintWriter outputWriter;

    /**
     * Constructor sets the file name and sets up tsv file
     * @param file
     * @throws IOException
     */
    public NTSParser(String file) throws IOException {
        this.file = file;
        String output = file.replace(".txt", ".tsv");
        output = output.replace(".csv", ".tsv");            //in case its a csv file
        fileWriter = new FileWriter(output);
        outputWriter = new PrintWriter(fileWriter);
        //add columns for the tsv file
        outputWriter.print("street\ttown\tstate\tzip5\tbldg_lo_num\tbldg_lo_chr\tbldg_hi_num\tbldg_hi_chr\tbldg_parity\tapt_lo_num\tapt_lo_chr\tapt_hi_num\tapt_hi_chr\tapt_parity\telection_code\tcounty_code\t" +
                "assembly_code\tsenate_code\tcongressional_code\tboe_town_code\ttown_code\tward_code\tboe_school_code\tschool_code\tcleg_code\tcc_code\tfire_code\tcity_code\tvill_code\n");
    }

    /**
     * Parses the file for the needed information. Finds where the actual data is and then
     * parseLine is called to parse the line
     *
     * @throws FileNotFoundException
     */
    public void parseFile() throws IOException {
        Scanner scanner = new Scanner(new File(file));
        String currentLine = scanner.nextLine();
        boolean inData = false;

        //While there is more lines in the file
        while (scanner.hasNext()) {
            //if inData then check for special case then call parseLine
            if (inData) {
                //check if this is the end of data
                if (currentLine.contains("TEAM SQL Version") || currentLine.contains("r_strstd") || currentLine.contains("Total No. of Street") || currentLine.contains("r_ppstreet")) {
                    inData = false;
                    //check for a blank line that will be less than 25 in length or less than 5 indices in the split array (not actual data)
                    //skip over these
                } else if (currentLine.length() < 25 || currentLine.trim().split("\\s+").length < 5) {
                    currentLine = scanner.nextLine();
                } else {
                    //get the nextLine to check for special case
                    String nextLine = scanner.nextLine();

                    //special case is where street suffix is bumped to its own line on the next line
                    //check if the next line is less than 5 characters long and it is not empty
                    if (nextLine.length() < 5 && !nextLine.trim().isEmpty()) {
                        //special overrun case is true
                        overloadStreetSuf = nextLine;
                        parseLine(currentLine, true);
                        //then skip over the line with just the suffix
                        nextLine = scanner.nextLine();
                        currentLine = nextLine;
                    } else {
                        //normal data line
                        parseLine(currentLine, false);
                        //get next line for parsing
                        currentLine = nextLine;
                    }
                }
            } else {
                //look for "Housing Range" to signal that data is starting
                if (currentLine.contains("House Range")) {
                    inData = true;
                    this.getIndexes(currentLine);
                }
                //gets the next line
                currentLine = scanner.nextLine();
            }
        }
        //close all writers/readers
        scanner.close();
        this.closeWriters();
    }

    /**
     * Parses the line by splitting the line into a string array by whitespaces
     * Calls parseAfterZip after getting the street name, street suf, and zip
     *
     * @param line
     * @param specialCase
     */
    protected void parseLine(String line, boolean specialCase) {

        //set up the streetFinderAddress for storage of current data
        StreetFinderAddress StreetFinderAddress = new StreetFinderAddress();
        //split the line by whitespace
        String[] splitLine = line.split("\\s+");

        int zipIndex = 0;
        int index = 0;

        //check if this is a line w/o street name (meaning street name came before and is stored in StreetFinderAddressStorage)
        //you can tell this by a blank first character
        if (line.charAt(0) == ' ') {
            //check that StreetFinderAddressStorage isn't blank. If it is then this is probably just a blank line so just return
            if (StreetFinderAddressStorage.getStreet().equals("")) {
                return;
            }
            //for some reason splitLine[0] is whitespace and the zip is at splitLine[1]
            if (splitLine[1].length() == 5) {
                StreetFinderAddress.setZip(splitLine[1]);
            }
            //set other fields using StreetFinderAddressStorage
            StreetFinderAddress.setPreDirection(StreetFinderAddressStorage.getPreDirection().trim());
            StreetFinderAddress.setStreet(StreetFinderAddressStorage.getStreet());
            StreetFinderAddress.setStreetSuffix((StreetFinderAddressStorage.getStreetSuffix()));
            //call parseAfterZip
            parseAfterZip(splitLine, specialCase, 2, line, StreetFinderAddress);

        } else if (line.charAt(0) == '*') {
            //check for unknown addresses at the start of the file
            //ex "*UNKNOWN*"
            //dont include the UNKNOWN as the street name
            //zip must be after *UNKNOWN*
            if (splitLine[1].length() == 5) {
                StreetFinderAddress.setZip(splitLine[1]);
            }
            //call parseAfterZip
            parseAfterZip(splitLine, specialCase, 2, line, StreetFinderAddress);

        } else {
            //first find array index of zip
            //skip first index because that must be street name
            for (int i = 1; i < splitLine.length; i++) {
                //zip must have 5 digits
                //except for 1 special case in Schenectady
                if (splitLine[i].matches("\\d{5}") || (splitLine[i].equals("1239") && file.contains("Schenectady"))) {
                    zipIndex = i;
                    break;
                }
            }

            //now zip = splitLine[zipIndex]
            //streetSuf = splitline[zipIndex -1]
            //streetName = everything before zipIndex -1
            StreetFinderAddress.setZip(splitLine[zipIndex]);

            //save the index of the end of the street name
            int endOfStreetName = zipIndex - 1;

            //check if the endOfStreetName index is actually a postDirection
            //first check for a conversion to formatting in directionMap
            if (AddressDictionary.directionMap.containsKey(splitLine[endOfStreetName])) {
                //if a format conversion is found then change it in splitLine
                splitLine[endOfStreetName] = AddressDictionary.directionMap.get(splitLine[endOfStreetName]);
            }

            //now actually check for the postDirection
            if (checkForDirection(splitLine[endOfStreetName])) {
                StreetFinderAddress.setPostDirection(splitLine[endOfStreetName]);
                //if a postDirection is found decrement endOfStreetName
                endOfStreetName--;
            }

            //if SplitLine[endOfStreetName] is not numbers then it must be the street suffix
            if (!splitLine[endOfStreetName].matches("\\d+")) {
                StreetFinderAddress.setStreetSuffix(splitLine[endOfStreetName]);
                //decrment end of StreetName
                endOfStreetName--;
            }

            int temp = 0;

            //check the beginning of splitLine for a pre-Direction
            //Not converting the beginning because it would convert North Street to just N st which
            //is not a pre-direction
            if (checkForDirection(splitLine[temp])) {
                StreetFinderAddress.setPreDirection(splitLine[temp].trim());
                //increment temp if one is found
                temp++;
            }

            String streetName = "";
            //concatenate Street Name into one string
            //Street Name is in indexes temp to endOfStreetName
            for (int i = temp; i <= endOfStreetName; i++) {
                streetName = streetName + splitLine[i] + " ";
            }
            StreetFinderAddress.setStreet(streetName.trim());
            //increment index for parseAfterZip call
            index = zipIndex + 1;
            //call parseAfterZip
            parseAfterZip(splitLine, specialCase, index, line, StreetFinderAddress);
        }
    }

    /**
     * Parses the split array after the zip to find the rest of the data.
     * index must be the the low range index in splitLine
     * @param splitLine
     * @param specialCase
     * @param index
     */
    public void parseAfterZip(String[] splitLine, boolean specialCase, int index, String line, StreetFinderAddress StreetFinderAddress) {

        //get the low range from splitLine
        String low = splitLine[index];

        //low could possibly be something like "1-" or just "1" and the "-" is its own index
        if (low.contains("-")) {
            //trim off "-"
            low = low.substring(0, low.length() - 1);
        } else {
            //otherwise skip over the "-"
            index++;
        }
        StreetFinderAddress.setBldg_low(low);
        index++;

        //get the high range
        StreetFinderAddress.setBldg_high(splitLine[index]);
        index++;

        //get the range type by calling setBldgParity which could change the index
        //And increase index to next value
        index = setBldgParity(splitLine, index, StreetFinderAddress);
        index++;

        //check that there is still more data in the line
        if (index < splitLine.length) {

            //now get townName which could possibly be multiple words (indexes)
            //find townCodeIndex same way as zipCodeIndex to use to tell when the town name ends
            int townCodeIndex = 0;
            for (int i = index; i < splitLine.length; i++) {
                //townCode could be 3 digits or 3 uppercase letters
                if (splitLine[i].length() == 3) {
                    //Also check for 3 special cases where the town ends in a 3 letter word
                    if (splitLine[i].matches("\\d+") || splitLine[i].matches("^[A-Z]{3}") && !splitLine[i].equals("LEE")
                            && !splitLine[i].equals("AVA") && !splitLine[i].equals("ANN")) {
                        //check that splitLine[i + 1] is 3 digits
                        //splitLine[i+1] is the ward code for all NTS files
                        //prevents any confusion between townName and townCode that is letters
                        if(splitLine[i +1].matches("\\d+")) {
                            //set townCodeIndex
                            townCodeIndex = i;
                            break;
                        }
                    }
                    //town code could also just be two letters
                } else if (splitLine[i].length() == 2) {
                    if (splitLine[i].matches("^[A-Z]{2}")) {
                        //check that splitLine[i + 1] is 3 digits
                        //splitLine[i+1] is the ward code for all NTS files
                        //prevents any confusion between townName and townCode that is letters
                        if(splitLine[i +1].matches("\\d+")) {
                            //set townCodeIndex
                            townCodeIndex = i;
                            break;
                        }
                    }
                    //A few files have townCodes of just 1 letter
                } else if(splitLine[i].length() == 1) {
                    if (splitLine[i].matches("^[A-Z]{1}")) {
                        //check that splitLine[i + 1] is 3 digits
                        //splitLine[i+1] is the ward code for all NTS files
                        //prevents any confusion between townName and townCode that is letters
                        if(splitLine[i +1].matches("\\d+")) {
                            //set townCodeIndex
                            townCodeIndex = i;
                            break;
                        }
                    }
                }
            }

            //now get the town Name going from splitLine[index to townCodeIndex - 1]
            String town = "";
            for (int i = index; i < townCodeIndex; i++) {
                town = town + splitLine[i] + " ";
            }
            StreetFinderAddress.setTown(town);

            //set the townCode
            StreetFinderAddress.setTownCode(splitLine[townCodeIndex]);
            //move to the index after townCode
            index = townCodeIndex + 1;

            //check for Ward, Dist, Cong, Sen, and Asm if not at the end of splitLine
            //These will all go in that order if they exist
            if (index < splitLine.length) {
                StreetFinderAddress.setWard(splitLine[index]);
                index++;

                if (index < splitLine.length) {
                    StreetFinderAddress.setDist(splitLine[index]);
                    index++;

                    if (index < splitLine.length) {
                        StreetFinderAddress.setCong(splitLine[index]);
                        index++;
                    }

                    if (index < splitLine.length) {
                        StreetFinderAddress.setSen(splitLine[index]);
                        index++;
                    }

                    if (index < splitLine.length) {
                        StreetFinderAddress.setAsm(splitLine[index]);
                        index++;
                    }
                }
            }

            //if the index is at the end then there is no more data and parsing is finished for the line
            //otherwise call parseAfterAsm to finish parsing
            if (index <= splitLine.length - 1) {
                parseAfterAsm(line, StreetFinderAddress);
            }

            //for the special case the street suf is really part of the street name
            //and overloadStreetSuf contains the real street Suffix
            if (specialCase) {
                StreetFinderAddress.setStreet(StreetFinderAddress.getStreet() + StreetFinderAddress.getStreetSuffix());
                StreetFinderAddress.setStreetSuffix(overloadStreetSuf);
            }

            this.writeToFile(StreetFinderAddress);
            StreetFinderAddressStorage = StreetFinderAddress;
        }
    }

    /**
     * Searches after Asm for Sch, vill, cle, fire, cc, and city code
     * This does so by calling helper methods that use the Index fields as
     * the location to search for the info
     * @param line
     * @param StreetFinderAddress - StreetFinderAddress to add info too
     */
    protected void parseAfterAsm(String line, StreetFinderAddress StreetFinderAddress) {
        getSch(line, StreetFinderAddress);
        getVill(line, StreetFinderAddress);
        getCle(line, StreetFinderAddress);
        getFire(line, StreetFinderAddress);
        getCC(line, StreetFinderAddress);
        getCityCode(line, StreetFinderAddress);
    }

    /**
     * Checks the line for a sch code. Uses the SchIndex as the location
     * to search for if the file has a sch column (schIndex > 0)
     * If found then it is added to the StreetFinderAddress
     * @param line
     * @param StreetFinderAddress
     */
    private void getSch(String line, StreetFinderAddress StreetFinderAddress) {

        if (schIndex > 0) {
            //check that the schIndex is not past the end of the line for this line
            if (schIndex >= line.length()) {
                return;
            }
            StringBuilder sch = new StringBuilder();
            //check from schIndex - 1 to schIndex + 3
            for (int i = -1; i < 4; i++) {
                //if its not whitespace then add to StringBuilder
                if (!Character.isWhitespace(line.charAt(schIndex + i))) {
                    sch.append(line.charAt(i + schIndex));
                }
                //check that the next index is not past the end of the line
                if (i + schIndex + 1 >= line.length()) {
                    //if it is then save the sch and return
                    StreetFinderAddress.setSch(sch.toString());
                    return;
                }
            }
            StreetFinderAddress.setSch(sch.toString());
        }
    }

    /**
     * Checks the line for a vill code. Uses the villIndex as the location
     * to search for if the file has a vill column (villIndex > 0)
     * If found then it is added to the StreetFinderAddress
     * @param line
     * @param StreetFinderAddress
     */
    private void getVill(String line, StreetFinderAddress StreetFinderAddress) {

        if (villIndex > 0) {
            //check that the villIndex is not past the end of the line for this line
            if (villIndex >= line.length()) {
                return;
            }
            StringBuilder temp = new StringBuilder();
            //check from villIndex - 3 to villIndex + 3
            for (int i = -3; i < 4; i++) {
                //if its not whitespace then add to StringBuilder
                if (!Character.isWhitespace(line.charAt(villIndex + i))) {
                    temp.append(line.charAt(i + villIndex));
                }
                //check that the next index is not past the end of the line
                if (i + villIndex + 1 >= line.length()) {
                    //if it is then save the vill and return
                    StreetFinderAddress.setVill(temp.toString());
                    return;
                }
            }
            StreetFinderAddress.setVill(temp.toString());
        }
    }

    /**
     * Checks the line for a cle code. Uses the cleIndex as the location
     * to search for if the file has a cle column (cleIndex > 0)
     * If found then it is added to the StreetFinderAddress
     * @param line
     * @param StreetFinderAddress
     */
    private void getCle(String line, StreetFinderAddress StreetFinderAddress) {

        if (cleIndex > 0) {
            int max;
            //check for Greene county
            if (file.contains("Greene")) {
                //Greene has a special case cleg that requires a larger search area
                max = 6;
            } else {
                max = 4;
            }
            //check that the cleIndex is not past the end of the line for this line
            if (cleIndex >= line.length()) {
                return;
            }
            StringBuilder temp = new StringBuilder();
            //check from cleIndex - 2 to cleIndex + (max -1)
            for (int i = -2; i < max; i++) {
                //if its not whitespace then add to StringBuilder
                if (!Character.isWhitespace(line.charAt(cleIndex + i))) {
                    temp.append(line.charAt(i + cleIndex));
                }
                //check that the next index is not past the end of the line
                if (i + cleIndex + 1 >= line.length()) {
                    //if it is then save the cle and return
                    StreetFinderAddress.setCle(temp.toString());
                    return;
                }
            }
            StreetFinderAddress.setCle(temp.toString());
        }
    }

    /**
     * Checks the line for a fire code. Uses the fireIndex as the location
     * to search for if the file has a fire column (FireIndex > 0)
     * If found then it is added to the StreetFinderAddress
     * @param line
     * @param StreetFinderAddress
     */
    private void getFire(String line, StreetFinderAddress StreetFinderAddress) {

        if (fireIndex > 0) {
            //check that the fireIndex is not past the end of the line for this line
            if (fireIndex >= line.length()) {
                return;
            }
            StringBuilder temp = new StringBuilder();
            //check from fireIndex - 2 to fireIndex + 3
            for (int i = -2; i < 4; i++) {
                //if its not whitespace then add to StringBuilder
                if (!Character.isWhitespace(line.charAt(fireIndex + i))) {
                    temp.append(line.charAt(i + fireIndex));
                }
                //check that the next index is not past the end of the line
                if (i + fireIndex + 1 >= line.length()) {
                    //if it is then save the fire and return
                    StreetFinderAddress.setFire(temp.toString());
                    return;
                }
            }
            StreetFinderAddress.setFire(temp.toString());
        }
    }

    /**
     * Checks the line for a cc code. Uses the ccIndex as the location
     * to search for if the file has a cc column (ccIndex > 0)
     * If found then it is added to the StreetFinderAddress
     * @param line
     * @param StreetFinderAddress
     */
    private void getCC(String line, StreetFinderAddress StreetFinderAddress) {

        if (ccIndex > 0) {
            //check that the ccIndex is not past the end of the line for this line
            if (ccIndex >= line.length()) {
                return;
            }
            StringBuilder temp = new StringBuilder();
            //check from ccIndex - 2 to ccIndex + 3
            for (int i = -2; i < 4; i++) {
                if (!Character.isWhitespace(line.charAt(ccIndex + i))) {
                    temp.append(line.charAt(i + ccIndex));
                }
                //check that the next index is not past the end of the line
                if (i + ccIndex + 1 >= line.length()) {
                    //if it is then save the cc and return
                    StreetFinderAddress.setCC(temp.toString());
                    return;
                }
            }
            StreetFinderAddress.setCC(temp.toString());
        }
    }

    /**
     * Checks the line for a city code. Uses the cityIndex as the location
     * to search for if the file has a city column (cityIndex > 0)
     * If found then it is added to the StreetFinderAddress
     * @param line
     * @param StreetFinderAddress
     */
    private void getCityCode(String line, StreetFinderAddress StreetFinderAddress) {

        if (cityIndex > 0) {
            //check that the cityIndex is not past the end of the line for this line
            if (cityIndex >= line.length()) {
                return;
            }
            StringBuilder temp = new StringBuilder();
            //check from cityIndex - 2 to cityIndex + 3
            //the data can be a few spaces away from the column head
            for (int i = -2; i < 4; i++) {
                if (!Character.isWhitespace(line.charAt(cityIndex + i))) {
                    temp.append(line.charAt(i + cityIndex));
                }
                //check that the next index is not past the end of the line
                if (i + cityIndex + 1 >= line.length()) {
                    //if it is then save the city and return
                    StreetFinderAddress.setCityCode(temp.toString());
                    return;
                }
            }
            StreetFinderAddress.setCityCode(temp.toString());
        }
    }

    /**
     * Utility method that checks if the given string is equal to a direction
     * Only checks if equal to "N", "E", "S", and "W"
     * @param string
     * @return true if a direction, false otherwise
     */
    protected boolean checkForDirection(String string) {
        if (string.equals("N") || string.equals("E") || string.equals("S") || string.equals("W")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Writes the StreetFinderAddress to the file in StreetFileForm by using the PrintWriter
     * @param StreetFinderAddress
     */
    protected void writeToFile(StreetFinderAddress StreetFinderAddress) {
        outputWriter.print(StreetFinderAddress.toStreetFileForm());
        outputWriter.flush();
    }

    /**
     * Closes the PrintWriter and FileWriter
     * @throws IOException
     */
    protected void closeWriters() throws IOException {
        outputWriter.close();
        fileWriter.close();
    }

    /**
     * Gets the indexes for sch, vill, cleg, fire, cc, and city if they exist.
     * Checks for multiple spelling/capitalization
     * The indexes stay - 1 if they do not exist
     * @param currentLine - must be a column header line with column titles
     */
    protected void getIndexes(String currentLine) {

        //sch
        schIndex = currentLine.indexOf("Schl");

        //vill
        villIndex = currentLine.indexOf("Vill");

        //cle
        cleIndex = currentLine.indexOf("Cleg");
        if (cleIndex < 0) {
            cleIndex = currentLine.indexOf("CLE");
            if (cleIndex < 0) {
                cleIndex = currentLine.indexOf("CLEG");
                if (cleIndex < 0) {
                    cleIndex = currentLine.indexOf(("Leg"));
                    if (cleIndex < 0) {
                        cleIndex = currentLine.indexOf("LEG");
                    }
                }
            }
        }

        //fire
        fireIndex = currentLine.indexOf("Fire");
        if (fireIndex < 0) {
            fireIndex = currentLine.indexOf("FD");
            if (fireIndex < 0) {
                fireIndex = currentLine.indexOf("FIRE");
            }
        }

        //cc
        ccIndex = currentLine.indexOf("CC");

        //city
        cityIndex = currentLine.indexOf("City");

    }

    /**
     * Gets the range type ans stores in streetFinderAddress. The index must the index where the range type occurs in splitLine
     * Could change the index for certain cases to skip over the word "Inclusive" So the
     * updated index is returned
     * @param splitLine
     * @param index - updated index
     * @return
     */
    protected int setBldgParity(String[] splitLine, int index, StreetFinderAddress StreetFinderAddress) {

        //for range type it could be "Evens Inclusive" "Odds Inclusive" or just "Inclusive"
        if (splitLine[index].equals("Odds")) {
            StreetFinderAddress.setBldg_parity("ODDS");
            index++; //skip over "Inclusive"
        } else if (splitLine[index].equals("Evens")) {
            StreetFinderAddress.setBldg_parity("EVENS");
            index++; //skip over "Inclusive"
        } else {
            //just Inclusive means both even and odd
            StreetFinderAddress.setBldg_parity("ALL");
        }
        return index;
    }

    /**
     * Utility method that scans through a file and calls the child classes version of
     * parseLine(String). This method is only intended for classes that extend this class and should not be used within
     * the NTSParser class
     * @throws IOException
     */
    protected void readFile() throws IOException {
        Scanner scanner = new Scanner
                (new File(file));
        String currentLine = scanner.nextLine();
        //While there is more lines in the file
        while(scanner.hasNext()) {
            currentLine = scanner.nextLine();
            parseLine(currentLine);
        }
        //close all writers/readers
        scanner.close();
        this.closeWriters();
    }

    /**
     * Utility method required for readFile(). Child classes must override this method
     * to use readFile(). This is not intended for use in NTSParser class
     * @param line
     */
    protected void parseLine(String line) {
        this.parseLine(line, false);
    }
}
