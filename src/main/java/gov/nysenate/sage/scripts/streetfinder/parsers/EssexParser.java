package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.model.address.StreetFinderAddress;
import gov.nysenate.sage.model.district.DistrictType;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Parses Essex County 2018 csv file and converts to tsv file
 * Looks for town, street, ed, low, high, range type, asm, cong, sen, zip
 */
public class EssexParser extends NTSParser {

    private String file;

    /**
     * Calls the super constructor which sets up the tsv file
     * @param file
     * @throws IOException
     */
    public EssexParser(String file) throws IOException {
        super(file);
        this.file = file;
    }

    /**
     * Parse the file by calling parseLine for each line of data
     * @throws FileNotFoundException
     */
    public void parseFile() throws IOException {

        super.readFile();
    }

    @Override
    /**
     * Parses the line by calling all helper methods and saves data to a StreetFinderAddress
     * @param line
     */
    protected void parseLine(String line) {
        StreetFinderAddress StreetFinderAddress = new StreetFinderAddress();

        //split the line by ","
        String[] splitLine = line.split(",");
        getTown(splitLine, StreetFinderAddress);              //splitLine[0]
        getStreetAndSuffix(splitLine, StreetFinderAddress);   //splitLine[1]
        getED(splitLine, StreetFinderAddress);               //splitLine[2]
        getLow(splitLine, StreetFinderAddress);               //splitLine[3]
        getHigh(splitLine, StreetFinderAddress);              //splitLine[4]
        getRangeType(splitLine, StreetFinderAddress);         //splitLine[5]
        getAsm(splitLine, StreetFinderAddress);               //splitLine[6]
        getCong(splitLine, StreetFinderAddress);              //splitLine[7]
        getSen(splitLine, StreetFinderAddress);               //splitLine[8]
        getZip(splitLine, StreetFinderAddress);               //splitLine[9]

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
     * Gets the street name and street suffix
     * checks for pre-direction
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getStreetAndSuffix(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        int count = 0;
        //split into words by spaces
        String[] string = splitLine[1].split(" ");

        //check for pre-Direction
        if(checkForDirection(string[0])) {
            StreetFinderAddress.setPreDirection(string[0]);
            count++;
        }

        //get street name
        //assume that the last index of string[] is the suffix
        StringBuilder temp = new StringBuilder();
        for(int i = count; i < string.length - 1; i++) {
            temp.append(string[i] + " ");
        }
        StreetFinderAddress.setStreet(temp.toString().trim());
        StreetFinderAddress.setStreetSuffix(string[string.length -1]);
    }

    /**
     * Gets the election district
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getED(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        StreetFinderAddress.setED(splitLine[2]);
    }

    /**
     * Gets the low range
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getLow(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        StreetFinderAddress.setBuilding(true, false, splitLine[3]);
    }

    /**
     * Gets the hish range
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getHigh(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        StreetFinderAddress.setBuilding(false, false, splitLine[4]);
    }

    /**
     * Gets the range type and converts it to correct format
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getRangeType(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        if(splitLine[5].equals("-1") && splitLine[6].equals("0")) {
            StreetFinderAddress.setBldg_parity("ODDS");
        } else if(splitLine[5].equals("0") && splitLine[6].equals("-1")) {
            StreetFinderAddress.setBldg_parity("EVENS");
        } else {
            StreetFinderAddress.setBldg_parity("ALL");
        }
    }

    /**
     * Gets the asmembly code
     * @param splitLine
     * @param StreetFinderAddress
     */
   private void getAsm(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
       StreetFinderAddress.put(DistrictType.ASSEMBLY, splitLine[7]);
   }

    /**
     * Gets the congressional code
     * @param splitLine
     * @param StreetFinderAddress
     */
   private void getCong(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
       StreetFinderAddress.put(DistrictType.CONGRESSIONAL, splitLine[8]);
   }

    /**
     * Gets the senate code
     * @param splitLine
     * @param StreetFinderAddress
     */
   private void getSen(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
       StreetFinderAddress.put(DistrictType.SENATE, splitLine[9]);
   }

    /**
     * Gets the zip code
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getZip(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        //might not be a zip check that splitLine[10] exits
        if(splitLine.length > 10) {
            //skip over a zip of "M"
            //Special case that only occurs a couple of times
            if(!splitLine[10].equals("M")) {
                StreetFinderAddress.setZip(splitLine[10]);
            }
        }
    }
}
