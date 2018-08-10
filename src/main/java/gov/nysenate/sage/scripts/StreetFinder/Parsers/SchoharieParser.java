package gov.nysenate.sage.scripts.StreetFinder.Parsers;

import gov.nysenate.sage.model.address.StreetFinderAddress;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

/**
 * Parses Schohaire County.txt file
 * Looks for street name, town, low, high, range type, sch, townCode, vill
 */
public class SchoharieParser extends NTSParser {

    private String overloadLineSaver;
    private int schoolLocation = 0;
    private int townLocation = 0;
    private String file;

    /**
     * Calls the super constructor to setup tsv file
     * @param file
     * @throws IOException
     */
    public SchoharieParser(String file) throws IOException {
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
        String currentLine;
        boolean inData = false;

        while(scanner.hasNext()) {
            currentLine = scanner.nextLine();
            //if inData
            if(inData) {
                //"Elections Street Districts" Signals the end of data
                if(currentLine.contains("Elections Street Districts")) {
                    inData = false;
                    //skip over other possible errant lines
                } else if(currentLine.contains("WARNING") || currentLine.contains("This conflicts") || currentLine.contains("Ricks Test Please Keep")) {
                    //skip
                    if(currentLine.contains("Esperance")) {
                        //This line causes errors
                        //skip an extra line because of these tests placed in the file
                        //Thanks Rick
                        currentLine = scanner.nextLine();
                    }
                    //if the length od the line is less than 45 then it must be a special case
                    //Where the street Name is too long and bumped other data to a new line
                } else if(currentLine.length() < 45) {
                    //special case
                    overloadLineSaver = currentLine;
                    currentLine = scanner.nextLine();
                    //Two types of special cases
                    //One is just the street name and suffix
                    //the other has more information (and a comma)
                    if(overloadLineSaver.contains(",")) {
                        parseLine(currentLine, true, false);
                    } else {
                        parseLine(currentLine, false, true);
                    }
                } else {
                    parseLine(currentLine, false, false);
                }
            }
            else {
                //look for this to signal that data is starting
                if(currentLine.contains("Street Numbers:")) {
                    inData = true;
                    townLocation = currentLine.indexOf("Town:");
                    schoolLocation = currentLine.indexOf("School:");
                }
                //gets the next line
            }
        }
        //close writers/readers
        scanner.close();
        super.closeWriters();
    }

    /**
     * Parses the line by splitting the line into a string array by whitespaces
     * Calls parseAfterZip after getting the street name, street suf, and zip
     * @param line
     * @param specialCase
     */
    protected void parseLine(String line, boolean specialCase, boolean specialCaseNoComma) {

        StreetFinderAddress StreetFinderAddress = new StreetFinderAddress();

        //split the line by whitespace
        String[] splitLine = line.trim().split("\\s+");
        int index = 0;

        //Handling a special case in which Mineral Springs RD has a run on and thus has no space inbetween the town and low_range
        //Also so does MOUNTAIN POND VIEW RD
        //ex. MINERAL SPRINGS RD, Richmondville100 - 103
        //need to seperate Richmondville and the 100
        if((splitLine[index].equals("MINERAL") && splitLine[index + 1].equals("SPRINGS")) ||
                (splitLine[index].equals("MOUNTAIN") && splitLine[index + 1].equals("POND") && splitLine[index + 2].equals("VIEW"))) {
            //location in the splitLine[] are different for both cases
            int splitLocation;
            if(splitLine[index].equals("MINERAL")) {
                //occurs at 3 for MINERAL
                splitLocation = index + 3;
            } else {
                //occurs at 4 for MOUNTAIN
                splitLocation = index + 4;
            }
            //get the run-on String of the town + low_range
            String temp = splitLine[splitLocation];
            //split it by the location where letters occur before and numbers occur afterwords
            String[] tempSplitArray = temp.split("(?<=\\D)(?=\\d)");
            //make sure that the split worked
            if(tempSplitArray.length > 1) {
                //Need to create a new array to replace splitLine (but be 1 spot longer)
                String[] replaceArray = new String[splitLine.length + 1];
                //counter keeps the spot within splitLine
                int counter = 0;
                //copy values from splitLine over to replaceArray
                for(int i = 0; i < replaceArray.length; i++) {
                    //if at the split index then add both values of tempSplitArray into their own indexes
                    if(i == (splitLocation)) {
                        replaceArray[i] = tempSplitArray[0];
                        replaceArray[i + 1] = tempSplitArray[1];
                        i++;
                        //increase splitLine counter to skip over run-on index
                        counter++;
                    } else {
                        replaceArray[i] = splitLine[counter];
                        counter++;
                    }
                }
                //set splitLine to the new Array
                splitLine = replaceArray;
            }
        }

        //if Special case with a comma
        //Means that the overloadLineSaver has Street name, street suffix, and town
        if(specialCase) {
            String street[] = overloadLineSaver.split("\\s+");

            //first check for a pre-Direction
            if (checkForDirection(street[0])) {
                StreetFinderAddress.setPreDirection(splitLine[index]);
                index++;
            }

            //now find where the , is. this will mark the street suffix
            //ex CountyParserMatcher ST,
            int temp = 0;
            for (int i = index; i < 5; i++) {
                if (street[i].contains(",")) {
                    temp = i;
                    break;
                }
            }

            //street name goes from street[index to temp -1]
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = index; i < temp; i++) {
                stringBuilder.append(street[i] + " ");
            }
            //set street
            StreetFinderAddress.setStreet(stringBuilder.toString().trim());
            //set street suffix
            //get rid of the comma first
            String streetSuffix = street[temp].replace(",", "");
            StreetFinderAddress.setStreetSuffix(streetSuffix);
            //set index to location after temp
            index = temp + 1;

            //town goes from street[index to street.length-1]
            stringBuilder = new StringBuilder();
            for (int i = index; i < street.length; i++) {
                stringBuilder.append(street[i] + " ");
            }
            //set Town
            StreetFinderAddress.setTown(stringBuilder.toString().trim());
            index = 0;

            //Special case without a comma
            //Means that the overLoadLineSaver just has some of the Street Name
        } else if(specialCaseNoComma) {
            //save the overloaded line into an array split by whitespace
            String street[] = overloadLineSaver.split("\\s+");

            //first check for a pre-Direction
            if(checkForDirection(street[index])) {
                StreetFinderAddress.setPreDirection(street[index]);
                index++;
            }

            //street name goes from street[index to street.length -1]
            StringBuilder stringBuilder = new StringBuilder();
            for(int i = index; i < street.length; i++) {
                stringBuilder.append(street[i] + " ");
            }

            //Switching back to regular line
            //splitLine

            //now find where the , is. this will mark the street suffix
            //ex CountyParserMatcher ST,
            int temp = 0;
            for(int i = 0; i < 5; i++) {
                if(splitLine[i].contains(",")) {
                    temp = i;
                    break;
                }
            }

            //Add in the rest of the street name to the StringBuilder
            //That has the part of the street name from the overloaded line
            for(int i = 0; i < temp; i++) {
                stringBuilder.append(splitLine[i] + " ");
            }
            //set street
            StreetFinderAddress.setStreet(stringBuilder.toString().trim());
            //set index to be the indices after the street suffix/comma
            index = temp + 1;

            //get town next
            //to find multiple words check for location of "-"
            //And then subtract one to bring it to the location of the low range
            for (int i = index; i < index + 5; i++) {
                if (splitLine[i].matches("-")) {
                    temp = i - 1;
                }
            }
            //town goes from splitLine[index to temp - 1]
            stringBuilder = new StringBuilder();
            for (int i = index; i < temp; i++) {
                stringBuilder.append(splitLine[i] + " ");
            }
            //set Town
            StreetFinderAddress.setTown(stringBuilder.toString().trim());
            //set index to the low_range
            index = temp;

        } else {
            //Normal case
            //first check for a pre-Direction
            if (super.checkForDirection(splitLine[index])) {
                StreetFinderAddress.setPreDirection(splitLine[index]);
                index++;
            }

            //now find where the , is. this will mark the street suffix
            //ex CountyParserMatcher ST,
            int temp = 0;
            for (int i = index; i < 5; i++) {
                if (splitLine[i].contains(",")) {
                    temp = i;
                    break;
                }
            }

            //street name goes from splitline[index to temp -1]
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = index; i < temp; i++) {
                stringBuilder.append(splitLine[i] + " ");
            }
            //set street
            StreetFinderAddress.setStreet(stringBuilder.toString().trim());
            //set street suffix and remove the ,
            String streetSuffix = splitLine[temp].replace(",", "");
            StreetFinderAddress.setStreetSuffix(streetSuffix);
            //skip index over the comma location
            index = temp + 1;

            //get town next
            //to find multiple words check for location of "-"
            //And then subtract one to bring it to the location of the low range
            for (int i = index; i < index + 5; i++) {
                if (splitLine[i].matches("-")) {
                    temp = i - 1;
                }
            }
            //town goes from splitLine[index to temp - 1]
            stringBuilder = new StringBuilder();
            for (int i = index; i < temp; i++) {
                stringBuilder.append(splitLine[i] + " ");
            }
            //set Town
            StreetFinderAddress.setTown(stringBuilder.toString().trim());
            //set index to the low_range
            index = temp;
        }

        //Continue parsing after getting the Street name, street suffix, and the town name
        //index should be at splitLine[index] = low range

        //set low range
        StreetFinderAddress.setBldg_low(splitLine[index]);
        index += 2; //skip over "-"

        //set high range
        StreetFinderAddress.setBldg_high(splitLine[index]);
        index++;

        //set Range type
        if(splitLine[index].contains("O")) {
            StreetFinderAddress.setBldg_parity("ODDS");
        } else if(splitLine[index].contains("E")) {
            StreetFinderAddress.setBldg_parity("EVENS");
        } else {
            StreetFinderAddress.setBldg_parity("ALL");
        }
        index++;

        //check for school, townCode, and village which might not be there
        if(index < splitLine.length) {
            //check at school location (+1) for any non-whitespace character
            if(!Character.isWhitespace(line.charAt(schoolLocation)) || !Character.isWhitespace(line.charAt(schoolLocation + 1))) {
                StreetFinderAddress.setSch(splitLine[index]);
                index++;
            }
        }
        //Check for townCode next
        if(index < splitLine.length) {
            //check at townCode location (+1) for any non-whitespace character
            if (!Character.isWhitespace(line.charAt(townLocation)) || !Character.isWhitespace(line.charAt(townLocation + 1))) {
                StreetFinderAddress.setTownCode(splitLine[index]);
                index++;
            }
        }

        //check for village
        //This is the last column so if there is more in splitLine then it must be a vill_code
        if(index < splitLine.length) {
            StreetFinderAddress.setVill(splitLine[index]);
        }
        super.writeToFile(StreetFinderAddress);
    }
}
