package gov.nysenate.sage.scripts.streetfinder.parsers;

import gov.nysenate.sage.model.address.StreetFinderAddress;
import gov.nysenate.sage.model.district.DistrictType;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Parses Wyoming County 2018 .txt
 * Looks for townCode, pre-Direction, Street, street suffix, post-direction, city, low,high, range type, and zip
 * Parses using the location of each column in the line
 */
public class WyomingParser extends NTSParser{

    //location indexes are used to determine where data is
    private String file;

    /**
     * Calls the super constructor which sets up the tsv file
     * @param file
     * @throws IOException
     */
    public WyomingParser(String file) throws IOException {
        super(file);
        this.file = file;
    }


    /**
     * Parses the file for the needed information. Finds where the actual data is and then
     * parseLine is called to parse the line
     * @throws FileNotFoundException
     */
    public void parseFile() throws IOException {

        super.readFile();
    }

    /**
     * Parses the line by calling all necessary helper methods
     * @param line
     */
    protected void parseLine(String line) {
        StreetFinderAddress streetFinderAddress = new StreetFinderAddress();

        if (line.contains("ODD_EVEN")) {
            return;
        }

        //split the line by ,
        String[] splitLine = line.split(",");

        //splitLine[0] Town
        //splitLine[1] District
        //splitLine[2] Dir
        //splitLine[3] Street
        //splitLine[4] Type
        //splitLine[5] P_Dir
        //splitLine[6] Lo
        //splitLine[7] Hi
        //splitLine[8] Odd_Even
        //splitLine[9] City
        //splitLine[10] Zip
        //splitLine[11] Precinct
        //splitLine[12] CD
        //splitLine[13] SD
        //splitLine[14] AD
        //splitLine[15] TW

        getPreDirection(splitLine, streetFinderAddress);
        getStreet(splitLine, streetFinderAddress);
        getStreetType(splitLine,streetFinderAddress);
        getPostDirection(splitLine, streetFinderAddress);
        getLowRange(splitLine, streetFinderAddress);
        getHighRange(splitLine, streetFinderAddress);
        getRangeType(splitLine, streetFinderAddress);
        getTown(splitLine, streetFinderAddress);
        getZip(splitLine, streetFinderAddress);
        handlePrecinct(splitLine, streetFinderAddress);
        getCongongressional(splitLine, streetFinderAddress);
        getSenate(splitLine,streetFinderAddress);
        getAssembly(splitLine,streetFinderAddress);

        super.writeToFile(streetFinderAddress);

    }

    private void handlePrecinct(String[] splitLine, StreetFinderAddress streetFinderAddress) {
        //always 6 digits
        //leading zero if only 5 digits
        //first 2 digits are town code
        //second 2 digits are the ward
        //third 2 digits are the ED

        String precinct = splitLine[11];
        if (precinct.length() == 5) {
            precinct = "0" + precinct;
        }
        getTownCode(precinct.substring(0,2), streetFinderAddress);
        getWard(precinct.substring(2,4), streetFinderAddress);
        getED(precinct.substring(precinct.length() - 2), streetFinderAddress);

    }


    private void getTownCode(String townCode, StreetFinderAddress streetFinderAddress) {
        streetFinderAddress.setTownCode(townCode);
    }

    private void getWard(String ward, StreetFinderAddress streetFinderAddress) {
        streetFinderAddress.put(DistrictType.WARD, ward);
    }

    private void getED(String electionDistrict, StreetFinderAddress streetFinderAddress) {
        streetFinderAddress.setED(electionDistrict);
    }

    /**
     * Gets the pre-Direction if there is one
     * @param streetFinderAddress
     * @param splitLine
     */
    private void getPreDirection(String[] splitLine, StreetFinderAddress streetFinderAddress) {
        streetFinderAddress.setPreDirection(splitLine[2]);
    }

    /**
     * Gets the street name
     * @param streetFinderAddress
     * @param splitLine
     */
    private void getStreet(String[] splitLine, StreetFinderAddress streetFinderAddress) {
        streetFinderAddress.setStreet(splitLine[3]);
    }

    /**
     * Gets the street suffix
     * @param streetFinderAddress
     * @param splitLine
     */
    private void getStreetType(String[] splitLine, StreetFinderAddress streetFinderAddress) {
        streetFinderAddress.setStreetSuffix(splitLine[4]);
    }

    /**
     * Gets post-Direction if there is one
     * @param streetFinderAddress
     * @param splitLine
     */
    private void getPostDirection(String[] splitLine, StreetFinderAddress streetFinderAddress) {
        streetFinderAddress.setPostDirection(splitLine[5]);
    }

    /**
     * Gets the Town
     * @param streetFinderAddress
     * @param splitLine
     */
    private void getTown(String[] splitLine, StreetFinderAddress streetFinderAddress) {
        streetFinderAddress.setTown(splitLine[9]);
    }

    /**
     * gets the low range
     * @param streetFinderAddress
     * @param splitLine
     */
    private void getLowRange(String[] splitLine, StreetFinderAddress streetFinderAddress) {
        streetFinderAddress.setBuilding(true, false, splitLine[6]);
    }

    /**
     * Gets the high Range
     * @param streetFinderAddress
     * @param splitLine
     */
    private void getHighRange(String[] splitLine, StreetFinderAddress streetFinderAddress) {
        streetFinderAddress.setBuilding(false, false, splitLine[7]);
    }

    /**
     * Gets the range type and converts to standard format
     * @param streetFinderAddress
     * @param splitLine
     */
    private void getRangeType(String[] splitLine, StreetFinderAddress streetFinderAddress) {
        if(splitLine[8].equals("O")) {
            streetFinderAddress.setBldg_parity("ODDS");
        } else if(splitLine[8].equals("E")) {
            streetFinderAddress.setBldg_parity("EVENS");
        } else {
            streetFinderAddress.setBldg_parity("ALL");
        }
    }

    /**
     * Gets the zip
     * @param streetFinderAddress
     * @param splitLine
     */
    private void getZip(String[] splitLine, StreetFinderAddress streetFinderAddress) {
        streetFinderAddress.setZip(splitLine[10]);
    }

    /**
     * Gets the Cong. Also gets rid of the "CD-"
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getCongongressional(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        String cd = splitLine[12].substring(splitLine[12].length() - 2);
        StreetFinderAddress.put(DistrictType.CONGRESSIONAL, cd);
    }

    /**
     * Gets the sen. Also gets rid of the "SD-"
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getSenate(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        String sd = splitLine[13].substring(splitLine[13].length() - 2);
        StreetFinderAddress.put(DistrictType.SENATE, sd);
    }

    /**
     * Gets the asm. Also gets rid of the "AD-"
     * @param splitLine
     * @param StreetFinderAddress
     */
    private void getAssembly(String[] splitLine, StreetFinderAddress StreetFinderAddress) {
        String ad = splitLine[14].substring(splitLine[14].length() - 3);
        StreetFinderAddress.put(DistrictType.ASSEMBLY, ad);
    }
}
