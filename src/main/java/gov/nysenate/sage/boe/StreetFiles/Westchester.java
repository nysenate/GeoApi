package gov.nysenate.sage.boe.StreetFiles;

import gov.nysenate.sage.boe.BOEAddressRange;
import gov.nysenate.sage.boe.StreetFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.commons.dbutils.QueryRunner;

public class Westchester extends StreetFile {

    HashMap<Integer, String> townMap;
    HashMap<Integer, String> townCodeMap;

    public Westchester(int county_code, File street_file) throws Exception {
        super(county_code, street_file);

        townCodeMap = new HashMap<Integer, String>();
        townCodeMap.put(1, "BEDFOR");
        townCodeMap.put(2, "CORTLA");
        townCodeMap.put(3, "EASTCH");
        townCodeMap.put(4, "GREENB");
        townCodeMap.put(5, "HARRIS");
        townCodeMap.put(6, "LEWISB");
        townCodeMap.put(7, "MAMARO");
        townCodeMap.put(8, "MT KIS");
        townCodeMap.put(9, "MT PLE");
        townCodeMap.put(10, "NEW CA");
        townCodeMap.put(11, "N CAST");
        townCodeMap.put(12, "N SALE");
        townCodeMap.put(13, "OSSINI");
        townCodeMap.put(14, "PELHAM");
        townCodeMap.put(15, "POUND");
        townCodeMap.put(16, "RYE");
        townCodeMap.put(17, "SCARSD");
        townCodeMap.put(18, "SOMERS");
        townCodeMap.put(19, "YORKTO");
        townCodeMap.put(20, "-MT VE");
        townCodeMap.put(21, "-NEW R");
        townCodeMap.put(22, "-PEEKS");
        townCodeMap.put(23, "-RYE");
        townCodeMap.put(24, "-WHITE");
        townCodeMap.put(25, "-YONKE");

        townMap = new HashMap<Integer, String>();
        townMap.put(1, "Bedford");
        townMap.put(2, "Cortlandt");
        townMap.put(3, "Eastchester");
        townMap.put(4, "Greenburgh");
        townMap.put(5, "Harrison");
        townMap.put(6, "Lewisboro");
        townMap.put(7, "Mamaroneck");
        townMap.put(8, "Mount Kisco");
        townMap.put(9, "Mount Pleasant");
        townMap.put(10, "New Castle");
        townMap.put(11, "North Castle");
        townMap.put(12, "North Salem");
        townMap.put(13, "Ossining");
        townMap.put(14, "Pelham");
        townMap.put(15, "Pound Ridge");
        townMap.put(16, "Rye Town");
        townMap.put(17, "Scarsdale");
        townMap.put(18, "Somers");
        townMap.put(19, "Yorktown");
        townMap.put(20, "Mount Vernon");
        townMap.put(21, "New Rochelle");
        townMap.put(22, "Peekskill");
        townMap.put(23, "Rye City");
        townMap.put(24, "White Plains");
        townMap.put(25, "Yonkers");
    }

    public String getParity(String parityCode) {
        if (parityCode.equals("B") || parityCode.equals("N")) {
            return "ALL";
        } else if (parityCode.equals("O")) {
            return "ODDS";
        } else if (parityCode.equals("E")) {
            return "EVENS";
        } else {
            logger.warn("Parity code "+parityCode+" not one of [BNOE]");
            return "ALL";
        }
    }

    public String getTown(int townId) {
        if (townMap.containsKey(townId)) {
            return townMap.get(townId);
        } else {
            logger.warn("Unrecognized town code "+townId+" in townMap");
            return "";
        }
    }

    public String getTownCode(int townId) {
        if (townCodeMap.containsKey(townId)) {
            return townCodeMap.get(townId);
        } else {
            logger.warn("Unrecognized town code "+townId+" in townCodeMap");
            return "";
        }
    }

    public int getDistrict(String district) {
        return Integer.parseInt(district.substring(3));
    }

    @Override
    public void save(DataSource db) throws Exception {
        logger.info("Starting "+street_file.getName());
        BufferedReader br = new BufferedReader(new FileReader(street_file));
        QueryRunner runner = new QueryRunner(db);
        Pattern townWardPattern = Pattern.compile("([0-9]{1,2})([0-9]{2})([0-9]{2})");

        String line;
        runner.update("BEGIN");
        br.readLine(); // skip header
        while ((line = br.readLine()) != null) {
            String[] parts = line.split("\t");
            BOEAddressRange range = new BOEAddressRange();
            Matcher townWardMatcher = townWardPattern.matcher(parts[1]);
            if (townWardMatcher.find()) {
                int townId = Integer.parseInt(townWardMatcher.group(1));
                range.street = (parts[2].trim()+" "+parts[3].trim()+" "+parts[4].trim()+" "+parts[5].trim()).trim();
                range.town = getTown(townId);
                range.townCode = getTownCode(townId);
                range.wardCode = Integer.parseInt(townWardMatcher.group(2));
                range.electionCode = Integer.parseInt(townWardMatcher.group(3));
                range.congressionalCode = getDistrict(parts[10]);
                range.senateCode = getDistrict(parts[11]);
                range.assemblyCode = getDistrict(parts[12]);
                range.countyCode = county_code;
                range.state = "NY";
                range.zip5 = Integer.parseInt(parts[9]);
                range.bldgLoNum = Integer.parseInt(parts[6]);
                range.bldgHiNum = Integer.parseInt(parts[7]);
                range.bldgParity = getParity(parts[8]);
                save_record(range,db);
            } else {
                logger.warn("TWD code "+parts[1]+" doesn't match "+townWardPattern.toString()+" on:");
                logger.warn(line);
            }
        }
        br.close();
        runner.update("COMMIT");
        logger.info("Done with "+street_file.getName());
    }

}
