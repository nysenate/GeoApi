package gov.nysenate.sage.scripts.StreetFinder.Parsers;

import gov.nysenate.sage.model.address.StreetFinderAddress;
import java.io.IOException;

/**
 * Parses Suffolk County txt file and outputs a tsv file
 */
public class SuffolkParser extends NTSParser{
    //-22 categories seperated by tabs (21 tabs not counting 1st category)
    //- zip, zip+4, pre, street name, street mode, post, low, high, odd/even, secondary name, secondary low, secondary high, secondary odd/even, town, ed, cong, sen, asm, cleg, dist, fire, vill

    private String file;

    /**
     * Calls the super constructor to set up tsv output file
     * @param file
     * @throws IOException
     */
    public SuffolkParser(String file) throws IOException {
        super(file);
        this.file = file;
    }

    /**
     * Parses the file by calling parseLine for each line of data in the file
     * @throws IOException
     */
    public void parseFile() throws IOException {
        super.readFile();
    }

    @Override
    /**
     * Parses the line by calling each helper method necessary for each line
     * @param line
     */
    protected void parseLine(String line) {
        StreetFinderAddress StreetFinderAddress = new StreetFinderAddress();

        //split the line by tabs
        String[] splitLine = line.split("\t");

        getZip(splitLine, StreetFinderAddress);               //splitLine[0]
        //skip zip +4                                   //splitLine[1]
        getPreDirection(splitLine, StreetFinderAddress);      //splitLine[2]
        //getting suffix first because the suffix might be in with the street name
        //by checking the suffix category first you can see if that has to be looked for or not
        getStreetSuffix(splitLine, StreetFinderAddress);      //splitLine[4]
        getStreetName(splitLine, StreetFinderAddress);        //splitLine[3]
        //check for special case where street is given as "DO NOT USE MAIL BOX
        //if it is the special case then just skip
        if (StreetFinderAddress.getStreet().contains("DO NOT USE MAIL"))
            return;
        getPostDirection(splitLine, StreetFinderAddress);     //splitLine[5]
        getLow(splitLine, StreetFinderAddress);               //splitLine[6]
        getHigh(splitLine, StreetFinderAddress);              //splitLine[7]
        getRangeType(splitLine, StreetFinderAddress);         //splitLine[8]
        //skip over secondary name                      //splitLine[9]
        getSecondaryLow(splitLine, StreetFinderAddress);      //splitLine[10]
        getSecondaryHigh(splitLine, StreetFinderAddress);     //splitLine[11]
        getSecondaryRangeType(splitLine, StreetFinderAddress);//splitLine[12]
        getTown(splitLine, StreetFinderAddress);              //splitLine[13]
        getED(splitLine, StreetFinderAddress);              //splitLine[14]
        getCong(splitLine, StreetFinderAddress);              //splitLine[15]
        getSen(splitLine, StreetFinderAddress);               //splitLine[16]
        getAsm(splitLine, StreetFinderAddress);               //splitLine[17]
        getCleg(splitLine, StreetFinderAddress);              //splitLine[18]
        //Next categories don't always exist so checking the size of the line
        if (splitLine.length > 19) {
            getDist(splitLine, StreetFinderAddress);          //splitLine[19]
            if (splitLine.length > 20)
                getFire(splitLine, StreetFinderAddress);          //splitLine[20]
            if (splitLine.length > 21)
                getVill(splitLine, StreetFinderAddress);      //splitLine[21]
        }
        super.writeToFile(StreetFinderAddress);
    }

    /**
     * Gets the zip code. Adds in leading 0 if it was trimed off to make it 5 digits
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getZip(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        StreetFinderAddress.setZip(splitLine[0]);
    }

    /**
     * Gets the pre-direction
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getPreDirection(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        StreetFinderAddress.setPreDirection(splitLine[2]);
    }

    /**
     * Gets the street Name and checks for a street suffix if StreetSuffix is empty
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getStreetName(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        //if street Suffix doesn't already exist in streetFinderAddress
        if(StreetFinderAddress.getStreetSuffix().equals("")) {
            //check if the suffix is in the street name
            //assume that it would be the last thing in the street name
            //split by a single space
            String[] string = splitLine[3].split(" ");
            //if string.length > 1 then there must be a street suffix in there
            if(string.length > 1) {
                StringBuilder stringBuilder = new StringBuilder();
                //add everything except the last index to stringBuilder (Street Name)
                for(int i = 0; i < string.length -1; i++) {
                    stringBuilder.append(string[i] + " ");
                }
                StreetFinderAddress.setStreet(stringBuilder.toString().trim());
                StreetFinderAddress.setStreetSuffix(string[string.length -1]);
            } else {
                //no suffix
                StreetFinderAddress.setStreet(string[0]);
            }
        } else {
            StreetFinderAddress.setStreet(splitLine[3]);
        }
    }

    /**
     * Gets the street suffix
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getStreetSuffix(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        StreetFinderAddress.setStreetSuffix(splitLine[4]);
    }

    /**
     * Gets the post-Direction
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getPostDirection(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        StreetFinderAddress.setPostDirection(splitLine[5]);
    }

    /**
     * Gets the low range
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getLow(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        StreetFinderAddress.setBldg_low(splitLine[6]);
    }

    /**
     * Gets the high range
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getHigh(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        StreetFinderAddress.setBldg_high(splitLine[7]);
    }

    /**
     * Gets the range type and converts it to the correct format
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getRangeType(String[] splitLine, StreetFinderAddress StreetFinderAddress)  {
        if(splitLine[8].equals("E")) {
            StreetFinderAddress.setBldg_parity("EVENS");
        } else if(splitLine[8].equals("O")) {
            StreetFinderAddress.setBldg_parity("ODDS");
        } else if(splitLine[8].equals("B")) {
            StreetFinderAddress.setBldg_parity("ALL");
        }
    }

    /**
     * Gets the secondary low range
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getSecondaryLow(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        StreetFinderAddress.setSecondaryBldg_low(splitLine[10]);
    }

    /**
     * Gets the secondary high range
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getSecondaryHigh(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        StreetFinderAddress.setSecondaryBldg_high(splitLine[11]);
    }

    /**
     * Getst the secondary range type and converts to the correct form
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getSecondaryRangeType(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        if(splitLine[12].equals("E")) {
            StreetFinderAddress.setSecondaryBldg_parity("EVENS");
        } else if(splitLine[12].equals("O")) {
            StreetFinderAddress.setSecondaryBldg_parity("ODDS");
        } else if(splitLine[12].equals("B")) {
            StreetFinderAddress.setSecondaryBldg_parity("ALL");
        }
    }

    /**
     * Gets the town name
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getTown(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        StreetFinderAddress.setTown(splitLine[13]);
    }

    /**
     * Gets the ward code
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getED(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        StreetFinderAddress.setED(splitLine[14]);
    }

    /**
     * Gets the cong code
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getCong(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        StreetFinderAddress.setCong(splitLine[15]);
    }

    /**
     * gets the sen code
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getSen(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        StreetFinderAddress.setSen(splitLine[16]);
    }

    /**
     * Gets the asm code
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getAsm(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        StreetFinderAddress.setAsm(splitLine[17]);
    }

    /**
     * Gets the cleg code
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getCleg(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        StreetFinderAddress.setCle(splitLine[18]);
    }

    /**
     * Gets the dist code
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getDist(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        StreetFinderAddress.setDist(splitLine[19]);
    }

    /**
     * Gets the fire code
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getFire(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        StreetFinderAddress.setFire(splitLine[20]);
    }

    /**
     * Gets the vill code
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getVill(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        StreetFinderAddress.setVill(splitLine[21]);
    }
}
