package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.model.address.StreetFinderAddress;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.util.AddressDictionary;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

import static gov.nysenate.sage.model.district.DistrictType.*;
import static gov.nysenate.sage.model.district.DistrictType.CITY;

/**
 * Parses files in the base NTS format (see Albany, Broome Counties as example)
 * Output is a tsv file
 */
public class NTSParser {
    private static final Map<DistrictType, Pattern> names = new EnumMap<>(DistrictType.class);
    static {
        names.put(SCHOOL, Pattern.compile("Schl"));
        names.put(VILLAGE, Pattern.compile("Vill"));
        names.put(CLEG, Pattern.compile("Cleg|CLE|CLEG|Leg|LEG"));
        names.put(FIRE, Pattern.compile("Fire|FD|FIRE"));
        names.put(CITY_COUNCIL, Pattern.compile("CC"));
        names.put(CITY, Pattern.compile("City"));
    }

    private Map<DistrictType, Integer> indices;
    //These are used to save the variables when the line is a continuation of an above street
    private StreetFinderAddress streetFinderAddressStorage = new StreetFinderAddress();
    protected final String file;
    private String overloadStreetSuf;
    //tsv file writers
    private final FileWriter fileWriter;
    private final PrintWriter outputWriter;

    /**
     * Constructor sets the file name and sets up tsv file
     * @param file
     * @throws IOException
     */
    public NTSParser(String file) throws IOException {
        this.file = file;
        String output = file.replace(".txt", ".tsv");
        output = output.replace(".csv", ".tsv");            //in case its a csv file
        this.fileWriter = new FileWriter(output);
        this.outputWriter = new PrintWriter(fileWriter);
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

        while (scanner.hasNext()) {
            if (inData) {
                // Check if this is the end of data
                var pattern = Pattern.compile("TEAM SQL Version|r_strstd|Total No. of Street|r_ppstreet");
                if (pattern.matcher(currentLine).find()) {
                    inData = false;
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
                    } else {
                        parseLine(currentLine, false);
                    }
                    currentLine = nextLine;
                }
            } else {
                // Signals that the data is starting
                if (currentLine.contains("House Range")) {
                    inData = true;
                    setIndices(currentLine);
                }
                currentLine = scanner.nextLine();
            }
        }
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
        StreetFinderAddress streetFinderAddress = new StreetFinderAddress();
        //split the line by whitespace
        String[] splitLine = line.split("\\s+");

        int zipIndex = 0;
        //check if this is a line w/o street name (meaning street name came before and is stored in StreetFinderAddressStorage)
        //you can tell this by a blank first character

        if (line.contains("E. MAIN ST")) {
            extractDataFromLine(splitLine, zipIndex, line, specialCase, streetFinderAddress);
        }
        else if (line.charAt(0) == ' ') {
            //check that StreetFinderAddressStorage isn't blank. If it is then this is probably just a blank line so just return
            if (streetFinderAddressStorage.getStreet().equals("")) {
                return;
            }
            //for some reason splitLine[0] is whitespace and the zip is at splitLine[1]
            if (splitLine[1].length() == 5) {
                streetFinderAddress.setZip(splitLine[1]);
            }
            //set other fields using StreetFinderAddressStorage
            streetFinderAddress.setPreDirection(streetFinderAddressStorage.getPreDirection().trim());
            streetFinderAddress.setStreet(streetFinderAddressStorage.getStreet());
            streetFinderAddress.setStreetSuffix((streetFinderAddressStorage.getStreetSuffix()));
            //call parseAfterZip
            parseAfterZip(splitLine, specialCase, 2, line, streetFinderAddress);

        } else if (line.charAt(0) == '*') {
            //check for unknown addresses at the start of the file
            //ex "*UNKNOWN*"
            //dont include the UNKNOWN as the street name
            //zip must be after *UNKNOWN*
            if (splitLine[1].length() == 5) {
                streetFinderAddress.setZip(splitLine[1]);
            }
            //call parseAfterZip
            parseAfterZip(splitLine, specialCase, 2, line, streetFinderAddress);

        } else {
            extractDataFromLine(splitLine, zipIndex, line, specialCase, streetFinderAddress);
        }
    }

    private void extractDataFromLine(String[] splitLine, int zipIndex, String line, boolean specialCase,
                                     StreetFinderAddress streetFinderAddress) {
        boolean case1239 = false;
        //first find array index of zip
        //skip first index because that must be street name
        for (int i = 1; i < splitLine.length; i++) {
            //zip must have 5 digits
            //except for 1 special case in Schenectady
            if (splitLine[i].matches("\\d{5}") || (splitLine[i].equals("1239") && file.contains("Schenectady"))) {
                zipIndex = i;
                case1239 = true;
                break;
            }
        }

        //now zip = splitLine[zipIndex]
        //streetSuf = splitline[zipIndex -1]
        //streetName = everything before zipIndex -1
        if (case1239) {
            streetFinderAddress.setZip("12309");
            case1239 = false;
        }
        else {
            streetFinderAddress.setZip(splitLine[zipIndex]);
        }


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
            streetFinderAddress.setPostDirection(splitLine[endOfStreetName]);
            //if a postDirection is found decrement endOfStreetName
            endOfStreetName--;
        }

        //if SplitLine[endOfStreetName] is not numbers then it must be the street suffix
        if (!splitLine[endOfStreetName].matches("\\d+")) {
            streetFinderAddress.setStreetSuffix(splitLine[endOfStreetName]);
            //decrement end of StreetName
            endOfStreetName--;
        }

        int temp = 0;

        //check the beginning of splitLine for a pre-Direction
        //Not converting the beginning because it would convert North Street to just N st which
        //is not a pre-direction
        if (checkForDirection(splitLine[temp])) {

            String potentialPreDirection = splitLine[temp].trim();

            if(potentialPreDirection.equals("E.")) {
                potentialPreDirection = "E";
            }

            streetFinderAddress.setPreDirection(potentialPreDirection);
            //increment temp if one is found
            temp++;
        }

        String streetName = "";
        //concatenate Street Name into one string
        //Street Name is in indexes temp to endOfStreetName
        for (int i = temp; i <= endOfStreetName; i++) {
            streetName = streetName + splitLine[i] + " ";
        }
        streetName = streetName.replace("E.","E");
        streetFinderAddress.setStreet(streetName.trim());
        //increment index for parseAfterZip call
        int index = zipIndex + 1;
        //call parseAfterZip
        parseAfterZip(splitLine, specialCase, index, line, streetFinderAddress);
    }

    /**
     * Parses the split array after the zip to find the rest of the data.
     * index must be the the low range index in splitLine
     * @param splitLine
     * @param specialCase
     * @param index
     */
    public void parseAfterZip(String[] splitLine, boolean specialCase, int index, String line, StreetFinderAddress streetFinderAddress) {
        String low = splitLine[index];
        //low could possibly be something like "1-" or just "1" and the "-" is its own index
        if (low.contains("-")) {
            //trim off "-"
            low = low.replaceFirst("-", "");
        } else {
            index++;
        }
        streetFinderAddress.setBuilding(true, low);
        index++;

        streetFinderAddress.setBuilding(false, splitLine[index++]);
        streetFinderAddress.setBldgParity(splitLine[index++]);

        if (index >= splitLine.length) {
            return;
        }

        // An unnecessary part of the parity
        if (splitLine[index].trim().equalsIgnoreCase("Inclusive")) {
            index++;
        }

        //now get townName which could possibly be multiple words (indexes)
        //find townCodeIndex same way as zipCodeIndex to use to tell when the town name ends
        int townCodeIndex = 0;
        for (int i = index; i < splitLine.length; i++) {
            if (splitLine[i].length() > 3) {
                continue;
            }
            if (splitLine[i].matches("[A-Z0-9]+") && !splitLine[i].matches("LEE|AVA|ANN") &&
                    splitLine[i + 1].matches("\\d+")) {
                townCodeIndex = i;
                break;
            }
        }

        //now get the town Name going from splitLine[index to townCodeIndex - 1]
        String town = String.join(" ", List.of(splitLine).subList(index, townCodeIndex));
        streetFinderAddress.setTown(town.trim().toUpperCase()
                .replaceAll("CITY OF", "").replaceAll("TOWN OF", ""));
        streetFinderAddress.setTownCode(splitLine[townCodeIndex]);
        index = townCodeIndex + 1;

        //check for Ward, Dist / ED, Cong, Sen, and Asm if not at the end of splitLine
        //These will all go in that order if they exist
        if (index < splitLine.length) {
            streetFinderAddress.put(WARD, splitLine[index++]);
            if (index < splitLine.length) {
                streetFinderAddress.setED(splitLine[index++]);

                if (index < splitLine.length) {
                    streetFinderAddress.put(CONGRESSIONAL, splitLine[index++]);
                }

                if (index < splitLine.length) {
                    streetFinderAddress.put(SENATE, splitLine[index++]);
                }

                if (index < splitLine.length) {
                    streetFinderAddress.put(ASSEMBLY, splitLine[index++]);
                }
            }
        }

        //if the index is at the end then there is no more data and parsing is finished for the line
        //otherwise call parseAfterAsm to finish parsing
        if (index <= splitLine.length - 1) {
            parseAfterAsm(line, streetFinderAddress);
        }

        //for the special case the street suf is really part of the street name
        //and overloadStreetSuf contains the real street Suffix
        if (specialCase) {
            streetFinderAddress.setStreet(streetFinderAddress.getStreet() + streetFinderAddress.getStreetSuffix());
            streetFinderAddress.setStreetSuffix(overloadStreetSuf);
        }

        this.writeToFile(streetFinderAddress);
        streetFinderAddressStorage = streetFinderAddress;
    }

    /**
     * Searches after Asm for Sch, vill, cle, fire, cc, and city code
     * This does so by calling helper methods that use the Index fields as
     * the location to search for the info
     * @param line
     * @param streetFinderAddress - StreetFinderAddress to add info too
     */
    protected void parseAfterAsm(String line, StreetFinderAddress streetFinderAddress) {
        getPostAsmDistrict(SCHOOL, 1, 4, line).ifPresent(streetFinderAddress::setSch);
        getPostAsmDistrict(VILLAGE, 3, 4, line).ifPresent(vill -> streetFinderAddress.put(VILLAGE, vill));
        // TODO: checking the entire file here is insane
        getPostAsmDistrict(CLEG, 2, file.contains("Greene") ? 6 : 4, line).ifPresent(cleg -> streetFinderAddress.put(CLEG, cleg));
        getPostAsmDistrict(FIRE, line).ifPresent(fire -> streetFinderAddress.put(FIRE, fire));
        getPostAsmDistrict(CITY_COUNCIL, line).ifPresent(cc -> streetFinderAddress.put(CITY_COUNCIL, cc));
        getPostAsmDistrict(CITY, line).ifPresent(cityCode -> streetFinderAddress.put(CITY, cityCode));
    }

    private Optional<String> getPostAsmDistrict(DistrictType type, String line) {
        return getPostAsmDistrict(type, 2, 4, line);
    }

    private Optional<String> getPostAsmDistrict(DistrictType type, int bottomAdj, int topAdj, String line) {
        // TODO: a match at 0 should be fine?
        int index = indices.getOrDefault(type, -1);
        if (index <= 0) {
            return Optional.empty();
        }
        var district = new StringBuilder();
        for (int i = index - bottomAdj; i < index + topAdj && i < line.length(); i++) {
            if (!Character.isWhitespace(line.charAt(i))) {
                district.append(line.charAt(i));
            }
        }
        return Optional.of(district.toString());
    }

    /**
     * Utility method that checks if the given string is equal to a direction
     * Only checks if equal to "N", "E", "S", and "W"
     * @param string
     * @return true if a direction, false otherwise
     */
    protected static boolean checkForDirection(String string) {
        return string.toUpperCase().matches("[NESW]");
    }

    /**
     * Writes the StreetFinderAddress to the file in StreetFileForm by using the PrintWriter
     * @param streetFinderAddress
     */
    protected void writeToFile(StreetFinderAddress streetFinderAddress) {
        streetFinderAddress = StreetFinderAddress.normalize(streetFinderAddress);
        outputWriter.print(streetFinderAddress.toStreetFileForm());
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
     * @param currentLine
     *  ine - must be a column header line with column titles
     */
    protected void setIndices(String currentLine) {
        indices = new EnumMap<>(DistrictType.class);
        for (var entry : names.entrySet()) {
            var matcher = entry.getValue().matcher(currentLine);
            if (matcher.find()) {
                indices.put(entry.getKey(), matcher.start());
            }
        }
    }

    protected static void handlePrecinct(String precinct, StreetFinderAddress streetFinderAddress) {
        // Add a leading zero if only 5 digits
        if (precinct.length() == 5) {
            precinct = "0" + precinct;
        }
        streetFinderAddress.setTownCode(precinct.substring(0, 2));
        streetFinderAddress.put(DistrictType.WARD, precinct.substring(2, 4));
        streetFinderAddress.setED(precinct.substring(precinct.length() - 2));
    }

    /**
     * Utility method that scans through a file and calls the child classes version of
     * parseLine(String). This method is only intended for classes that extend this class and should not be used within
     * the NTSParser class
     * @throws IOException
     */
    protected void readFile() throws IOException {
        Scanner scanner = new Scanner(new File(file));
        String currentLine = scanner.nextLine();
        //While there is more lines in the file
        while (scanner.hasNext()) {
            currentLine = scanner.nextLine();
            parseLine(currentLine);
        }
        //close all writers/readers
        scanner.close();
        closeWriters();
    }

    /**
     * Utility method required for readFile(). Child classes must override this method
     * to use readFile(). This is not intended for use in NTSParser class
     * @param line
     */
    protected void parseLine(String line) {
        parseLine(line, false);
    }
}
