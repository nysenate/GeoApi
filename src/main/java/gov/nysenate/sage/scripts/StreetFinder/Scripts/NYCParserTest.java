package gov.nysenate.sage.scripts.StreetFinder.Scripts;

import gov.nysenate.sage.model.address.StreetFinderAddress;
import gov.nysenate.sage.scripts.StreetFinder.Parsers.NTSParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NYCParserTest extends NTSParser {

    public String location1 = "";
    public String location2 = "";
    public String location3 = "";
    public String town;
    public String file;

    public NYCParserTest(String file) throws IOException {
        super(file);
        this.file = file;
        town = this.getTown();
    }

    public void parseFile() throws IOException {
        Scanner scanner = new Scanner(new File(file));
        String currentLine;



        while (scanner.hasNext()) {
            currentLine = scanner.nextLine();

            int start1, start2, start3;
            start1 = currentLine.indexOf("__________________________________________");
            start2 = currentLine.indexOf("__________________________________________", start1 + 1);
            start3 = currentLine.lastIndexOf("__________________________________________");

            String[] columns = new String[3];

            columns[0] = currentLine.substring(0, start2 - 1);
            columns[1] = currentLine.substring(start2, start3 - 1);
            columns[2] = currentLine.substring(start3, currentLine.length() - 1);

            for (int i=0; i<columns.length; i++) {
                parseColumn(columns[i],i+1);
            }
        }
    }

    public void parseColumn(String column, int colNum) throws NullPointerException {
        column = column.trim();

        StreetFinderAddress streetFinderAddress = new StreetFinderAddress();

        //check for bad data
        if (checkForBadData(column)) {
            return;
        }

        //-1 is a placeholder. If the var has that later on we know it wasnt set
        int FROM = -1, TO = -1, ED = -1, AD = -1, ZIP = -1, CD = -1, SD = -1, MC = -1, CO = -1;

        // Regex to be matched
        String locationRegex = "[a-zA-Z]+";
        Pattern locationPattern = Pattern.compile(locationRegex);

        Matcher locationMatcher = locationPattern.matcher(column.trim());
        if (locationMatcher.find()) {

            switch (colNum) {
                case 1: location1 = formatLocation(column.trim());
                    break;
                case 2: location2 = formatLocation(column.trim());
                    break;
                case 3: location3 = formatLocation(column.trim());
                    break;
            }

            //Parse information out of the matched String for StreetFinderAddress
            //set information for StreetFinderAddress
        }
        else { //9 total || 7 otherwise
            String[] districts = column.trim().split("\\s+");

            handleBuildingNumbers(districts.length,colNum, streetFinderAddress);

            if (districts.length > 1) {
                if (districts.length == 9) {
                    FROM = Integer.parseInt( districts[0] );
                    TO = Integer.parseInt( districts[1] );
                    ED = Integer.parseInt( districts[2] );
                    AD = Integer.parseInt( districts[3] );
                    ZIP = Integer.parseInt( districts[4] );
                    CD = Integer.parseInt( districts[5] );
                    SD = Integer.parseInt( districts[6] );
                    //We dont use the following peices of info currently
                    MC = Integer.parseInt( districts[7] );
                    CO = Integer.parseInt( districts[8] );

                    //Handle FROM and TO
                    streetFinderAddress.setBldg_low(String.valueOf( FROM ));
                    streetFinderAddress.setBldg_high(String.valueOf( TO ));

                    //Bldg Parity
                    if (FROM % 2 == 0) { //is even
                        streetFinderAddress.setBldg_parity("EVENS");
                    }
                    else { //is odd
                        streetFinderAddress.setBldg_parity("ODDS");
                    }

                }
                else { //This means all of the address on this street belong to these districts
                    ED = Integer.parseInt( districts[0] );
                    AD = Integer.parseInt( districts[1] );
                    ZIP = Integer.parseInt( districts[2] );
                    CD = Integer.parseInt( districts[3] );
                    SD = Integer.parseInt( districts[4] );
                    //We dont use the following peices of info currently
                    MC = Integer.parseInt( districts[5] );
                    CO = Integer.parseInt( districts[6] );

                    //Handle Bldg Parity
                    streetFinderAddress.setBldg_parity("ALL");
                }
            }

            //set districts for street address
            streetFinderAddress.setED(String.valueOf( ED ));
            streetFinderAddress.setAsm(String.valueOf( AD) );
            streetFinderAddress.setZip(String.valueOf( ZIP ));
            streetFinderAddress.setCong(String.valueOf( CD ));
            streetFinderAddress.setSen(String.valueOf( SD ));
            System.out.println(streetFinderAddress.toStreetFileForm());
        }
        //set town
        //write StreetFinderAddress to File
        streetFinderAddress.setTown(town);
        super.writeToFile(streetFinderAddress);
    }

    private boolean checkForBadData(String column) {
        boolean badData = false;

        if (column.isEmpty() || column == null) {
            badData = true;
        }
        if (column.trim().isEmpty()) {
            badData = true;
        }
        if (column.contains("FROM") || column.contains("PAGE") || column.contains("STREET FINDER")) {
            badData = true;
        }
        if (column.contains("TOTIN") || column.contains("Information") || column.contains("Reproduction")) {
            badData = true;
        }
        if (column.matches("\\d+/\\d+/\\d+")) {
            badData = true;
        }
        if(column.matches("\\s*V\\s+TE\\s+NYC\\s*")) {
            badData = true;
        }
        if (column.matches("\\s*Board\\s+of\\s+Elections\\s*")) {
            badData = true;
        }
        if(column.matches("\\s*STATEN\\s+ISLAND\\s*")) {
            badData = true;
        }
        return badData;
    }

    private void handleBuildingNumbers(int districtLength, int colNum, StreetFinderAddress streetFinderAddress) {
        String curColLocation = "";
        switch (colNum) {
            case 1: curColLocation = location1;
                break;
            case 2: curColLocation = location2;
                break;
            case 3: curColLocation = location3;
                break;
        }

        if (districtLength == 9) {
            streetFinderAddress.setStreet(curColLocation);
        }
        else if (districtLength == 7) {
            if (curColLocation.toUpperCase().contains("BLDG")) {
                int index = curColLocation.indexOf("BLDG");
                index = index + 4;
                String restOfLoc = curColLocation.substring(index, curColLocation.length()-1);
                streetFinderAddress.setStreet(curColLocation.substring(0, index).trim());
                determineBuildingNumOrDigit(restOfLoc, streetFinderAddress);
            }
            else if (curColLocation.toUpperCase().contains("BUILDING")) {
                int index = curColLocation.indexOf("BUILDING");
                index = index + 8;
                String restOfLoc = curColLocation.substring(index, curColLocation.length()-1);
                streetFinderAddress.setStreet(curColLocation.substring(0, index).trim());
                determineBuildingNumOrDigit(restOfLoc, streetFinderAddress);
            }
        }

    }

    private void determineBuildingNumOrDigit(String restOfLoc, StreetFinderAddress streetFinderAddress) {
        String charRegex = "[A-Z]";
        String digitRegex = "\\d+";

        Pattern charPattern = Pattern.compile(charRegex, Pattern.CASE_INSENSITIVE);
        Pattern digitPattern = Pattern.compile(digitRegex);

        Matcher charMatcher = charPattern.matcher(restOfLoc);
        Matcher digitMatcher = digitPattern.matcher(restOfLoc);

        if (charMatcher.find()) {
            streetFinderAddress.setBldg_low_char( charMatcher.group(0) );
            streetFinderAddress.setBldg_high_char( charMatcher.group(0) );
        }
        else if (digitMatcher.find()) {
            streetFinderAddress.setBldg_low( digitMatcher.group(0) );
            streetFinderAddress.setBldg_high( digitMatcher.group(0) );
        }

    }

    private String formatLocation(String location) {
        return location.replaceAll("1/2","").trim();
    }

    /**
     * Gets the town by using the file name. All data in the file has the same town
     * @return
     */
    private String getTown() {
        if(file.contains("Bronx")) {
            return "BRONX";
        } else if(file.contains("Brooklyn")) {
            return "BROOKLYN";
        } else if(file.contains("Manhattan")) {
            return "MANHATTAN";
        } else if(file.contains("Queens")) {
            return "QUEENS";
        } else {
            return "STATEN ISLAND";
        }
    }
}
