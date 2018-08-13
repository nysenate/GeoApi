package gov.nysenate.sage.scripts.StreetFinder.Parsers;

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
            StreetFinderAddress.setZip(splitLine[1]);
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
            StreetFinderAddress.setZip(splitLine[1]);
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
     * index must be the the low range
     * @param splitLine
     * @param specialCase
     * @param index
     */
    public void parseAfterZip(String[] splitLine, boolean specialCase, int index, String line, StreetFinderAddress StreetFinderAddress) {

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
        StreetFinderAddress.setBldg_high(splitLine[index]);

        index++;
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

        index++;
        if (index < splitLine.length) {


            //now on Town which could possibly be multiple words (indexes)
            //find townCodeIndex same way as zipCodeIndex
            int townCodeIndex = 0;
            for (int i = index; i < splitLine.length; i++) {
                //townCode must be 3 digits
                if (splitLine[i].length() == 3) {
                    if (splitLine[i].matches("\\d+") || splitLine[i].matches("^[A-Z]{3}") && !splitLine[i].equals("NEW")) {
                        townCodeIndex = i;
                        break;
                    }
                    //town code could be two letters
                } else if (splitLine[i].length() == 2) {
                    if (splitLine[i].matches("^[A-Z]{2}")) {
                        if (splitLine[i].equals("OF")) {
                            //skip because OF is not the townCode
                            //ex. TOWN OF MADISON
                        } else {
                            townCodeIndex = i;
                            break;
                        }
                    }
                } else if(splitLine[i].length() == 1) {
                    if (splitLine[i].matches("^[A-Z]{1}")) {
                            townCodeIndex = i;
                            break;
                    }
                }
            }

            String town = "";
            //concatenate town name
            for (int i = index; i < townCodeIndex; i++) {
                town = town + splitLine[i] + " ";
            }
            StreetFinderAddress.setTown(town);

            StreetFinderAddress.setTownCode(splitLine[townCodeIndex]);
            index = townCodeIndex + 1;

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

            //cant assume that there is any more data
            //if the index is at the end then there is no more data and parsing is finished for the line
            if (index <= splitLine.length - 1) {
                //splitLine[index] is the next string after asm
                parseAfterAsm(line, StreetFinderAddress);
            }

            //for the special case the street suf is really part of the street name
            //and the real suffix is RD
            if (specialCase) {
                StreetFinderAddress.setStreet(StreetFinderAddress.getStreet() + StreetFinderAddress.getStreetSuffix());
                StreetFinderAddress.setStreetSuffix(overloadStreetSuf);
            }

            this.writeToFile(StreetFinderAddress);
            StreetFinderAddressStorage = StreetFinderAddress;
        }
    }

    /**
     * Searches after Asm for any additional info. Only looks for a sch in the Albany County form
     * Assume that all files using this will only have a sch code
     * Any specifics will extend the class and override this method
     *
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

    private void getSch(String line, StreetFinderAddress StreetFinderAddress) {
        //might or might not have a school
        if (schIndex > 0) {
            if (schIndex >= line.length()) {
                return;
            }
            StringBuilder sch = new StringBuilder();
            for (int i = -1; i < 4; i++) {
                if (!Character.isWhitespace(line.charAt(schIndex + i))) {
                    sch.append(line.charAt(i + schIndex));
                    //System.out.println("School");
                }
                if (i + schIndex + 1 >= line.length()) {
                    StreetFinderAddress.setSch(sch.toString());
                    return;
                }
            }
            StreetFinderAddress.setSch(sch.toString());
        }
    }

    private void getVill(String line, StreetFinderAddress StreetFinderAddress) {
        //might or might not have a school
        if (villIndex > 0) {
            if (villIndex >= line.length()) {
                return;
            }
            StringBuilder temp = new StringBuilder();
            for (int i = -3; i < 4; i++) {
                if (!Character.isWhitespace(line.charAt(villIndex + i))) {
                    temp.append(line.charAt(i + villIndex));
                }
                if (i + villIndex + 1 >= line.length()) {
                    StreetFinderAddress.setVill(temp.toString());
                    return;
                }
            }
            StreetFinderAddress.setVill(temp.toString());
        }
    }

    private void getCle(String line, StreetFinderAddress StreetFinderAddress) {
        //might or might not have a school
        if (cleIndex > 0) {
            int max;
            if (file.contains("Greene")) {
                //Greene has a special case cleg
                max = 6;
            } else {
                max = 4;
            }
            if (cleIndex >= line.length()) {
                return;
            }
            StringBuilder temp = new StringBuilder();
            for (int i = -2; i < max; i++) {
                if (!Character.isWhitespace(line.charAt(cleIndex + i))) {
                    temp.append(line.charAt(i + cleIndex));
                }
                if (i + cleIndex + 1 >= line.length()) {
                    StreetFinderAddress.setCle(temp.toString());
                    return;
                }
            }
            StreetFinderAddress.setCle(temp.toString());
        }
    }

    private void getFire(String line, StreetFinderAddress StreetFinderAddress) {
        //might or might not have a fire
        if (fireIndex > 0) {
            if (fireIndex >= line.length()) {
                return;
            }
            StringBuilder temp = new StringBuilder();
            for (int i = -2; i < 4; i++) {
                if (!Character.isWhitespace(line.charAt(fireIndex + i))) {
                    temp.append(line.charAt(i + fireIndex));
                }
                if (i + fireIndex + 1 >= line.length()) {
                    StreetFinderAddress.setFire(temp.toString());
                    return;
                }
            }
            StreetFinderAddress.setFire(temp.toString());
        }
    }

    private void getCC(String line, StreetFinderAddress StreetFinderAddress) {
        //might or might not have a CC
        if (ccIndex > 0) {
            if (ccIndex >= line.length()) {
                return;
            }
            StringBuilder temp = new StringBuilder();
            for (int i = -2; i < 4; i++) {
                if (!Character.isWhitespace(line.charAt(ccIndex + i))) {
                    temp.append(line.charAt(i + ccIndex));
                }
                if (i + ccIndex + 1 >= line.length()) {
                    StreetFinderAddress.setCC(temp.toString());
                    return;
                }
            }
            StreetFinderAddress.setCC(temp.toString());
        }
    }

    private void getCityCode(String line, StreetFinderAddress StreetFinderAddress) {
        //might or might not have a CityCode
        if (cityIndex > 0) {
            if (cityIndex >= line.length()) {
                return;
            }
            StringBuilder temp = new StringBuilder();
            for (int i = -2; i < 4; i++) {
                if (!Character.isWhitespace(line.charAt(cityIndex + i))) {
                    temp.append(line.charAt(i + cityIndex));
                }
                if (i + cityIndex + 1 >= line.length()) {
                    StreetFinderAddress.setCityCode(temp.toString());
                    return;
                }
            }
            StreetFinderAddress.setCityCode(temp.toString());
        }
    }

    protected boolean checkForDirection(String string) {
        if (string.equals("N") || string.equals("E") || string.equals("S") || string.equals("W")) {
            return true;
        } else {
            return false;
        }
    }

    protected void writeToFile(StreetFinderAddress StreetFinderAddress) {
        outputWriter.print(StreetFinderAddress.toStreetFileForm());
        outputWriter.flush();
    }

    protected void closeWriters() throws IOException {
        outputWriter.close();
        fileWriter.close();
    }

    protected void getIndexes(String currentLine) {
        //get all indexes
        schIndex = currentLine.indexOf("Schl");
        villIndex = currentLine.indexOf("Vill");            //V
        if (villIndex < 0) {
            villIndex = currentLine.indexOf("V");
        }
        cleIndex = currentLine.indexOf("Cleg");             //maybe Cle
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
        fireIndex = currentLine.indexOf("Fire");
        if (fireIndex < 0) {
            fireIndex = currentLine.indexOf("FD");
            if (fireIndex < 0) {
                fireIndex = currentLine.indexOf("FIRE");
            }
        }
        ccIndex = currentLine.indexOf("CC");
        cityIndex = currentLine.indexOf("City");

    }
}
