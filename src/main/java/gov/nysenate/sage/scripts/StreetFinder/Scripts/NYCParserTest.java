package gov.nysenate.sage.scripts.StreetFinder.Scripts;

import gov.nysenate.sage.model.address.StreetFinderAddress;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NYCParserTest  {

    public NYCParserTest() throws IOException {}

    public String location1 = "";
    public String location2 = "";
    public String location3 = "";
    public String town;
    public String file;



    public void parseColumn(String column, int colNum) throws NullPointerException {

        StreetFinderAddress streetFinderAddress = new StreetFinderAddress();

        //-1 is a placeholder. If the var has that later on we know it wasnt set
        int FROM = -1, TO = -1, ED = -1, AD = -1, ZIP = -1, CD = -1, SD = -1, MC = -1, CO = -1;
        // Regex to be matched
        String locationRegex = "\\d+[-]?\\d*[A-Z]?";
        Pattern locationPattern = Pattern.compile(locationRegex, Pattern.CASE_INSENSITIVE);

        Matcher locationMatcher = locationPattern.matcher(column);
        if (locationMatcher.matches()) {

            switch (colNum) {
                case 1: location1 = formatLocation(column);
                    break;
                case 2: location2 = formatLocation(column);
                    break;
                case 3: location3 = formatLocation(column);
                    break;
            }

            //Parse information out of the matched String for StreetFinderAddress
            //set information for StreetFinderAddress
        }
        else { //9 total || 7 otherwise
            String[] districts = column.split("\\s+");

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
        }

        //set town
        //write StreetFinderAddress to File
    }

    public void handleBuildingNumbers(int districtLength, int colNum, StreetFinderAddress streetFinderAddress) {
        if (districtLength == 9) {
            switch (colNum) {
                case 1: streetFinderAddress.setStreet(location1);
                    break;
                case 2: streetFinderAddress.setStreet(location2);
                    break;
                case 3: streetFinderAddress.setStreet(location3);
                    break;
            }
        }
        else if (districtLength == 7) {
            //check for building
            //set digits from and to
            //set rest of street
        }

    }

    public String formatLocation(String location) {
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

    public static void main(String[] args) {
        String titleLine = "FROM        TO    ED AD  ZIP   CD SD MC CO       FROM        TO   ED AD   ZIP  CD SD  MC CO       FROM        TO    ED AD  ZIP   CD SD MC CO";
        String text = "__________________________________________       __________________________________________       __________________________________________\n";
        String txt2 = "                                                  2 AVENUE                                         3 AVENUE\n\n";
        String txt3 = "                  3 50 11222  12 18  3 33         69        87    95 52 11215  7  25  1 39          1        93   33 52  11217  8 25   1 33\n";
        String txt4 = "                                                  89       179    44 51 11215  7  25  1 39          2        10   27 52  11217  8 25   1 33\n";
        String txt5 = "1 AVENUE                                         181       195    45 51 11215 10  25  1 39         12        44   32 52  11217  8 25   1 33\n";
        String txt6 = "3901     4999    56 51 11232   7 23  5  38       410       698    48 51 11232 10  25  5 38         98       118  108 52  11217  7 25   1 33\n";
        String txt7 = "1 COURT                                         4101      4899    50 51 11232  7  25  5 38        251       319   91 52  11215  7 25   1 39\n";
        String txt8 = "900      944    15 45 11223  11 22  8 48        5000      5298    57 51 11232  7  23  5 38        321       455   93 52  11215  7 20   1 39\n";
        String txt9 = "63      103    45 52 11231   7 26  1  39          2 PLACE                                         611       631   23 51  11232  7 20   5 38\n";


        //Experimental work

        //Effectively gets each column of data
        int start1, start2, start3;
        start1 = text.indexOf("__________________________________________");
        start2 = text.indexOf("__________________________________________", start1 + 1);
        start3 = text.lastIndexOf("__________________________________________");

        String column1, column2, column3;




        column1 = txt2.substring(0, start2 - 1);
        column2 = txt2.substring(start2, start3 - 1);
        column3 = txt2.substring(start3, txt2.length() - 1);

        System.out.println(column1);
        System.out.println(column2);
        System.out.println(column3);
        System.out.println("--------------------------------------------------");

        column1 = txt5.substring(0, start2 - 1);
        column2 = txt5.substring(start2, start3 - 1);
        column3 = txt5.substring(start3, txt5.length() - 1);

        System.out.println(column1);
        System.out.println(column2);
        System.out.println(column3);
        System.out.println("--------------------------------------------------");

    }
}
