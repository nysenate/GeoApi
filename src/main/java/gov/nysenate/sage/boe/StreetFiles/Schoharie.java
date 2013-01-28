package gov.nysenate.sage.boe.StreetFiles;

import gov.nysenate.sage.boe.AddressUtils;
import gov.nysenate.sage.boe.BOEAddressRange;
import gov.nysenate.sage.boe.BOEStreetAddress;
import gov.nysenate.sage.boe.StreetFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

import javax.sql.DataSource;

import org.apache.commons.dbutils.QueryRunner;

public class Schoharie extends StreetFile {
	public int currentLine;
	public File voter_file;
    public HashMap<String, String> townMap;

    public Schoharie(int county, File street_file, File voter_file) throws Exception {
        super(county, street_file);
        this.voter_file = voter_file;
        townMap = new HashMap<String, String>();
        townMap.put("sha", "SHARON");
        townMap.put("sew", "SEWARD");
        townMap.put("car", "CARLIS");
        townMap.put("esp", "ESPERA");
        townMap.put("cob", "COBLES");
        townMap.put("sch", "SCHOHA");
        townMap.put("wri", "WRIGHT");
        townMap.put("ric", "RICHMV");
        townMap.put("mid", "MIDDLB");
        townMap.put("ful", "FULTON");
        townMap.put("sum", "SUMMIT");
        townMap.put("bro", "BROOME");
        townMap.put("gil", "GILBOA");
        townMap.put("jef", "JEFFER");
        townMap.put("ble", "BLENHE");
        townMap.put("con", "CONESV");
    }

    public String getTownCode(String town) {
        String abbrev = town.substring(0, Math.min(town.length(),3));
        if (townMap.containsKey(abbrev)) {
        	return townMap.get(abbrev);
        } else {
        	throw new RuntimeException("Line "+currentLine+": Town "+town+" not found in the town code map.");
        }
    }

    public String getParity(String parity) {
        if(parity.equals("B"))
            return "ALL";
        else if (parity.equals("E"))
            return "EVENS";
        else if (parity.equals("O"))
            return "ODDS";
        else {
            //logger.error("Line "+currentLine+": Invalid parity input "+parity);
        	return "ALL";
        }
    }

    public void save(DataSource db) throws Exception {
        logger.info("Starting Schoharie");
        QueryRunner runner = new QueryRunner(db);

        BufferedReader br = new BufferedReader(new FileReader(voter_file));
        String line;
        br.readLine(); // Skip the header
        HashMap<String, ArrayList<BOEStreetAddress>> zipTownLookup = new HashMap<String, ArrayList<BOEStreetAddress>>();
        while( (line = br.readLine()) != null) {
            String[] parts = line.split(",");

            BOEStreetAddress address = new BOEStreetAddress();
            address.bldg_num = Integer.parseInt(parts[0]);
            address.street = parts[1]+" "+parts[2]+" "+parts[3]+" "+parts[4];

            // Attempt to recover a town
            if (!parts[8].trim().isEmpty()) {
                address.town = parts[8].substring(0, parts[8].length()-1);
            } else if (!parts[5].trim().isEmpty()){
                address.town = parts[5];
            } else {
                logger.error("No town info available: "+line);
                continue;
            }

            // Attempt to recover a zipcode
            try {
                address.zip5 = Integer.parseInt(parts[6]);
            } catch (NumberFormatException e) {
                logger.error("Bad Zipcode format on line: "+line);
                continue;
            }

            AddressUtils.normalizeAddress(address);
            String key = (address.street+" "+address.town.substring(0, 3)).toLowerCase();
            if (!zipTownLookup.containsKey(key)) {
                zipTownLookup.put(key, new ArrayList<BOEStreetAddress>());
            }
            ArrayList<BOEStreetAddress> houseList = zipTownLookup.get(key);
            houseList.add(address);
            zipTownLookup.put(key, houseList);

        }
        br.close();


        currentLine = 0;
        br = new BufferedReader(new FileReader(street_file));
        br.readLine(); // read the header
        while( (line = br.readLine()) != null) {
            String[] parts = line.trim().split(",");
            BOEAddressRange range = new BOEAddressRange();
            range.street = parts[0];
            AddressUtils.normalizeAddress(range);

            String[] bldgParts = parts[1].split("-| ");
            range.bldgLoNum = Integer.parseInt(bldgParts[0]);
            range.bldgHiNum = Integer.parseInt(bldgParts[1]);
            range.bldgParity = getParity(bldgParts[2]);
            range.townCode = getTownCode(parts[2]);
            range.wardCode = Integer.parseInt(parts[3]);
            range.electionCode = Integer.parseInt(parts[4]);
            range.congressionalCode = Integer.parseInt(parts[5]);
            range.senateCode = Integer.parseInt(parts[6]);
            range.assemblyCode = Integer.parseInt(parts[7]);
            range.schoolCode = parts[8];
            range.clegCode = Integer.parseInt(parts[9]);
            range.cityCode = parts[10];
            range.villCode = parts[11];
            range.fireCode = parts[12];
            range.countyCode = county_code;
            range.state = "NY";

            String key = (range.street+" "+parts[2]).toLowerCase();
            if (zipTownLookup.containsKey(key)) {
                boolean saved = false;
                for (BOEStreetAddress address : zipTownLookup.get(key)) {
                    String addressParity = address.bldg_num % 2 == 0 ? "EVENS" : "ODDS";
                    if (address.bldg_num >= range.bldgLoNum && address.bldg_num <= range.bldgHiNum && (range.bldgParity=="ALL" || range.bldgParity.equals(addressParity))) {
                        range.zip5 = address.zip5;
                        range.town = address.town;
                        //System.out.println(range);
                        save_record(range, db);
                        saved = true;
                        if(++currentLine % 5000 == 0) {
                            logger.info("Done with "+currentLine);
                            runner.update("COMMIT");
                            runner.update("BEGIN");
                        }
                        break;
                    }
                }

                if (!saved) {
                    logger.error("Non-voting house: "+line);
                }
            } else {
                logger.error("Non-voting street: "+line);
            }
        }
        runner.update("COMMIT");
        br.close();
        logger.info("Done with Schoharie");
    }
}
