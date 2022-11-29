package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.model.address.StreetFinderAddress;

import java.io.*;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Pattern;

import static gov.nysenate.sage.model.address.StreetFileField.*;

/**
 * Parses files similar to Saratoga County. Uses very similar logic to NTSParser but is separated for
 * simplicity of NTSParser. The largest difference is in the spacing and the town name being at the top of each
 * Data segment
 */
public class SaratogaParser extends NTSParser {
    private final Pattern containsPattern = Pattern.compile("Ward ,|Segments {4}|r_strtdd|Ward Ward");
    private String overloadStreetSuf;

    /**
     * Calls super method to set up tsv file
     * @param file
     * @throws IOException
     */
    public SaratogaParser(String file) throws IOException {
        super(file);
    }

    /**
     * Finds where the data starts and ends. When in data it calls parseLine to parse the line
     * and create a StreetFinderAddress Object
     * @throws FileNotFoundException
     */
    public void parseFile() throws IOException {
        Scanner scanner = new Scanner(new File(file));
        String currentLine = scanner.nextLine();
        boolean inData = false;

        //these files put the town on the first line of data
        //so it needs to be saved until inData becomes false
        String town = "";

        //While there is more lines in the file
        while (scanner.hasNext()) {
            //if inData then check for special case then call parseLine
            if (inData) {
                //check if this is the end of data
                if (currentLine.contains("TEAM SQL Version")) {
                    inData = false;
                // check to see if this is a line to just skip over based on some keywords/phrases
                } else if (containsPattern.matcher(currentLine).find()) {;
                    currentLine = scanner.nextLine();
                //Must be actual data otherwise
                } else {
                    //get the nextLine to check for special case where
                    //the line is just the street suffix for the previous line
                    String nextLine = scanner.nextLine();
                    //with leading spaces a line with just the suffix should be less than about 16
                    if (nextLine.length() < 16 && nextLine.length() > 1) {
                        //save the street Suffix
                        overloadStreetSuf = nextLine.trim();
                        parseLine(currentLine, true, town);
                        //then skip over the line with just the suffix
                        nextLine = scanner.nextLine();
                    } else {
                        int size = currentLine.split("\\s+").length;
                        // Some random lines in the file that are just a couple numbers ex. "         11"
                        if (size >= 3) {
                            parseLine(currentLine, false, town);
                        }
                    }
                    currentLine = nextLine;
                }
            } else {
                //look for "Housing Range" to signal that data is starting
                if (currentLine.contains("House Range")) {
                    //in these types of files the town name is the first thing on this line
                    //split the line by whitespace
                    String[] getTown = currentLine.split("\\s+");
                    town = "";
                    //go through and check for town name with spaces ex. "Clifton Park"
                    for (int i = 0; i < getTown.length; i++) {
                        //if we find Street and then Name then it is past the town Name
                        if (getTown[i].equals("Street") && getTown[i + 1].equals("Name")) {
                            break;
                        } else {
                            //otherwise add this to the town name
                            town += getTown[i] + " ";
                        }
                    }
                    town = town.trim();
                    inData = true;
                    setIndices(currentLine);
                }
                currentLine = scanner.nextLine();
            }
        }
        scanner.close();
        closeWriters();
    }

    /**
     * Parses through the line to find the street name, street suffix, zip then calls parseAfterZip to further parse
     * @param line
     * @param specialCase
     * @param town
     */
    public void parseLine(String line, boolean specialCase, String town) {
        StreetFinderAddress streetFinderAddress = new StreetFinderAddress();
        streetFinderAddress.setTown(town);
        String[] splitLine = line.split("\\s+");
        int zipIndex = 1;
        // Skip first index because that must be street name
        for (; zipIndex < splitLine.length; zipIndex++) {
            if (splitLine[zipIndex].matches("\\d{5}")) {
                break;
            }
        }
        streetFinderAddress.setZip(splitLine[zipIndex]);
        streetFinderAddress.setStreetSuffix(splitLine[zipIndex - 1]);
        String streetName = String.join(" ", Arrays.copyOfRange(splitLine, 0, zipIndex));
        streetFinderAddress.setStreet(streetName.trim());
        parseAfterZip(splitLine, specialCase, zipIndex + 1, line, streetFinderAddress);
    }

    /**
     * Parses the split array after the zip to find more data.
     * Calls parseAfterAsm if there is any data after Asm field
     * index must be the the low range (of the splitLine array)
     * @param splitLine
     * @param specialCase
     * @param index
     * @param line
     * @param streetFinderAddress
     */
    @Override
     public void parseAfterZip(String[] splitLine, boolean specialCase, int index, String line, StreetFinderAddress streetFinderAddress) {
        String low = splitLine[index];
        //low could possibly be something like "1-" or just "1" and the "-" is its own index
        if (low.contains("-")) {
            low = low.replaceFirst("-", "");
        } else {
            index++;
        }

        streetFinderAddress.setBuilding(true, low);
        index++;
        streetFinderAddress.setBuilding(false, splitLine[index++]);
        streetFinderAddress.setBldgParity(splitLine[index++]);
        // An unnecessary part of the parity
        if (splitLine[index].trim().equalsIgnoreCase("Inclusive")) {
            index++;
        }

        streetFinderAddress.setTownCode(splitLine[index++]);
//        StreetFinderAddress.setWard(splitLine[index]);
        index++;
        streetFinderAddress.setED(splitLine[index++]);
        streetFinderAddress.put(CONGRESSIONAL, splitLine[index++]);
        streetFinderAddress.put(SENATE, splitLine[index++]);
        streetFinderAddress.put(ASSEMBLY, splitLine[index++]);
        //cant assume that there is any more data
        //if the index is at the end then there is no more data and parsing is finished for the line
        if (index <= splitLine.length - 1) {
            parseAfterAsm(line, streetFinderAddress);
        }

        if (specialCase) {
            streetFinderAddress.setStreet(streetFinderAddress.getStreet() + streetFinderAddress.getStreetSuffix());
            streetFinderAddress.setStreetSuffix(overloadStreetSuf);
        }
        writeToFile(streetFinderAddress);
    }
}
