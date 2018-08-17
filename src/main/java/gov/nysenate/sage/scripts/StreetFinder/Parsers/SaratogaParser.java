package gov.nysenate.sage.scripts.StreetFinder.Parsers;

import gov.nysenate.sage.model.address.StreetFinderAddress;
import java.io.*;
import java.util.Scanner;

/**
 * Parses files similar to Saratoga County. Uses very similar logic to NTSParser but is separated for
 * simplicity of NTSParser. The largest difference is in the spacing and the town name being at the top of each
 * Data segment
 */
public class SaratogaParser extends NTSParser {

    private String overloadStreetSuf;
    private String file;

    /**
     * Calls super method to set up tsv file
     * @param file
     * @throws IOException
     */
    public SaratogaParser(String file) throws IOException {
        super(file);
        this.file = file;
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
                } else if (currentLine.contains("Ward ,") || currentLine.contains("Segments    ")  || currentLine.contains("r_strtdd")
                        || currentLine.contains("Ward Ward")) {
                    currentLine = scanner.nextLine();
                //Must be actual data otherwise
                } else {
                    //get the nextLine to check for special case where
                    //the line is just the street suffix for the previous line
                    String nextLine = scanner.nextLine();
                    //with leading spaces a line with just the suffix should be less than about 16
                    if(nextLine.length() < 16 && nextLine.length() > 1) {
                        //save the street Suffix
                        overloadStreetSuf = nextLine.trim();
                        parseLine(currentLine, true, town);
                        //then skip over the line with just the suffix
                        nextLine = scanner.nextLine();
                        currentLine = nextLine;
                    } else {
                        String[] size = currentLine.split("\\s+");
                        //Some random lines in the file that are just a couple numbers ex. "         11"
                        //skip those using the size of the line split by whitespace
                        if(size.length < 3) {
                            currentLine = nextLine;
                        } else {
                            parseLine(currentLine, false, town);
                            //get next line for parsing
                            currentLine = nextLine;
                        }
                    }
                }
            } else {
                //look for "Housing Range" to signal that data is starting
                if (currentLine.contains("House Range")) {
                    //in these types of files the town name is the first thing on this line
                    //split the line by whitespace
                    String[] getTown = currentLine.split("\\s+");
                    town = "";
                    //go through and check for town name with spaces ex. "Clifton Park"
                    for(int i = 0; i < getTown.length; i++) {
                        //if we find Street and then Name then it is past the town Name
                        if(getTown[i].equals("Street") && getTown[i+1].equals("Name")) {
                            break;
                        } else {
                            //otherwise add this to the town name
                            town = town + getTown[i] + " ";
                        }
                    }
                    //trim off extra whitespace on town
                    town = town.trim();
                    inData = true;
                    //get the index locations
                    super.getIndexes(currentLine);
                }
                //gets the next line
                currentLine = scanner.nextLine();
            }
        }
        //close writers/readers
        scanner.close();
        super.closeWriters();
    }

    /**
     * Parses through the line to find the street name, street suffix, zip then calls parseAfterZip to further parse
     * @param line
     * @param specialCase
     * @param town
     */
    public void parseLine(String line, boolean specialCase, String town) {

        //create new StreetFinderAddress object
        StreetFinderAddress StreetFinderAddress = new StreetFinderAddress();
        //set the town
        StreetFinderAddress.setTown(town);

        //split the line into an array by whitespace
        String[] splitLine = line.split("\\s+");
        int zipIndex = 0;
        int index = 0;
        //first find array index of zip
        //skip first index because that must be street name
        for (int i = 1; i < splitLine.length; i++) {
            //zip must have 5 digits
            if (splitLine[i].length() == 5) {
                if (splitLine[i].matches("\\d+")) {
                    zipIndex = i;
                    break;
                }
            }
        }

        //now zip = splitLine[zipIndex]
        //streetSuf = splitline[zipIndex -1]
        //streetName = everything before zipIndex -1
        StreetFinderAddress.setZip(splitLine[zipIndex]);
        StreetFinderAddress.setStreetSuffix(splitLine[zipIndex -1]);
        String streetName = "";
        //concatenate into one string
        for (int i = 0; i < zipIndex - 1; i++) {
            streetName = streetName + splitLine[i] + " ";
        }
        StreetFinderAddress.setStreet(streetName.trim());
        //increment index for parseAfterZip call
        index = zipIndex + 1;
        parseAfterZip(splitLine, specialCase, index, line, StreetFinderAddress);
    }

    /**
     * Parses the split array after the zip to find more data.
     * Calls parseAfterAsm if there is any data after Asm field
     * index must be the the low range (of the splitLine array)
     * @param splitLine
     * @param specialCase
     * @param index
     * @param line
     * @param StreetFinderAddress
     */
    @Override
     public void parseAfterZip(String[] splitLine, boolean specialCase, int index, String line, StreetFinderAddress StreetFinderAddress) {

        String low = splitLine[index];
        //low could possibly be something like "1-" or just "1" and the "-" is its own index
        if(low.contains("-")) {
            //trim off "-"
            low = low.substring(0,low.length()-1);
        } else {
            //otherwise skip over the "-"
            index++;
        }

        StreetFinderAddress.setBldg_low(low);
        index ++;

        StreetFinderAddress.setBldg_high(splitLine[index]);
        index++;

        //get the range type by calling setBldgParity which could change the index
        index = super.setBldgParity(splitLine, index, StreetFinderAddress);
        //increment to townCode location
        index++;

        StreetFinderAddress.setTownCode(splitLine[index]);
        index++;

        StreetFinderAddress.setWard(splitLine[index]);
        index++;

        StreetFinderAddress.setDist(splitLine[index]);
        index++;

        StreetFinderAddress.setCong(splitLine[index]);
        index++;

        StreetFinderAddress.setSen(splitLine[index]);
        index++;

        StreetFinderAddress.setAsm(splitLine[index]);
        index++;

        //cant assume that there is any more data
        //if the index is at the end then there is no more data and parsing is finished for the line
        if(index <= splitLine.length -1) {
            super.parseAfterAsm(line, StreetFinderAddress);
        }

        //for the special case the street suf is really part of the street name
        //and the real suffix is overloadStreetSuf
        if(specialCase) {
            StreetFinderAddress.setStreet(StreetFinderAddress.getStreet() + StreetFinderAddress.getStreetSuffix());
            StreetFinderAddress.setStreetSuffix(overloadStreetSuf);
        }

        super.writeToFile(StreetFinderAddress);
    }
}
