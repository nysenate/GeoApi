package gov.nysenate.sage.scripts.streetfinder.parsers;

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
    private String file;

    /**
     * Calls the super constructor to setup tsv file
     *
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
     *
     * @throws FileNotFoundException
     */
    public void parseFile() throws IOException {
        super.readFile();
    }

    @Override
    protected void parseLine(String line) {

        if (!line.contains("House Range") || !line.contains("This conflicts") || !line.contains("Ricks Test")
                || !line.contains("Dist")) {
            //split the line by ,
            String[] splitLine = line.split(",", Integer.MAX_VALUE);
//            System.out.println(splitLine.length);

            if (!splitLine[6].isEmpty() || !splitLine[1].isEmpty() || splitLine.length >= 11) {


                StreetFinderAddress streetFinderAddress = new StreetFinderAddress();

                getStreet(splitLine[0] , streetFinderAddress);   //splitLine[0] StreetName
                getRanges(splitLine[1], streetFinderAddress);   //splitLine[1] House Range
                getTown(splitLine[2], streetFinderAddress);     //splitLine[2] Town
                // splitLine[3] Ward     USELESS Not used in the 2018 file
                getED(splitLine[4], streetFinderAddress);      //splitLine[4] ED
                getCong(splitLine[5], streetFinderAddress);     //splitLine[5] Cong
                getSen(splitLine[6], streetFinderAddress);      //splitLine[6] Sen
                getAsm(splitLine[7], streetFinderAddress);       //splitLine[7] Asm
                getSchool(splitLine[8],streetFinderAddress);    //splitLine[8] School
                //splitLine[9] Cleg     USELESS Not used in the 2018 file
                //splitLine[10] City    USELESS Not used in the 2018 file
                getVillage(splitLine[11], streetFinderAddress);    //splitLine[11] Village
                //splitLine[12] fire    USELESS Not used in the 2018 file

                if (!streetFinderAddress.getSenateDistrict().equals("\\N")
                        && !streetFinderAddress.getSenateDistrict().isEmpty()
                        && !streetFinderAddress.getBldg_parity().equals("\\N")) {
                    super.writeToFile(streetFinderAddress);
                }
            }
        }
    }

    private void getStreet(String data, StreetFinderAddress streetFinderAddress) {
        if (!data.equalsIgnoreCase("street name")) {
            streetFinderAddress.setStreet(data);
        }
    }

    private void getRanges(String data, StreetFinderAddress streetFinderAddress) {

        if(data.isEmpty() || data.contains("House Range")) {
            return;
        }

        String[] splitData = data.split("-", Integer.MAX_VALUE);

        if (splitData.length == 3) {
            streetFinderAddress.setBldg_low(splitData[0]);
            streetFinderAddress.setBldg_high(splitData[1]);

            if(splitData[2].equals("O")) {
                streetFinderAddress.setBldg_parity("ODDS");
            } else if(splitData[2].equals("E")) {
                streetFinderAddress.setBldg_parity("EVENS");
            } else {
                streetFinderAddress.setBldg_parity("ALL");
            }
        }
    }

    private void getTown(String data, StreetFinderAddress streetFinderAddress) {
        if (!data.equalsIgnoreCase("Town")) {
            streetFinderAddress.setTown(data);
        }
    }

    private void getED(String data, StreetFinderAddress streetFinderAddress) {
        if (!data.equalsIgnoreCase("Dist")) {
            streetFinderAddress.setED(data);
        }
    }

    private void getCong(String data, StreetFinderAddress streetFinderAddress) {
        if (!data.equalsIgnoreCase("Cong")) {
            streetFinderAddress.setCong(data);
        }
    }

    private void getSen(String data, StreetFinderAddress streetFinderAddress) {
        if (!data.equalsIgnoreCase("Sen")) {
            streetFinderAddress.setSen(data);
        }
    }

    private void getAsm(String data, StreetFinderAddress streetFinderAddress) {
        if (!data.equalsIgnoreCase("Asm")) {
            streetFinderAddress.setAsm(data);
        }
    }

    private void getSchool(String data, StreetFinderAddress streetFinderAddress) {
        if (!data.equalsIgnoreCase("School")) {
            streetFinderAddress.setSch(data);
        }
    }

    private void getVillage(String data, StreetFinderAddress streetFinderAddress) {
        if (!data.equalsIgnoreCase("Village")) {
            streetFinderAddress.setVill(data);
        }
    }
}
