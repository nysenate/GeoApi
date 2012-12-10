package gov.nysenate.sage.boe.StreetFiles;

import gov.nysenate.sage.boe.BOEAddressRange;
import gov.nysenate.sage.boe.StreetFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.util.HashMap;

import javax.sql.DataSource;

public class Essex extends StreetFile {

    public HashMap<String, String> townMap;

    public Essex(int county, File street_file) throws Exception {
        super(county, street_file);

        townMap = new HashMap<String, String>();
        townMap.put("CHESTER", "CHESTF");
        townMap.put("CROWN P", "CROWN");
        townMap.put("ELIZABE", "ELIZAB");
        townMap.put("ESSEX", "ESSEX");
        townMap.put("JAY", "JAY");
        townMap.put("KEENE", "KEENE");
        townMap.put("LEWIS", "LEWIS");
        townMap.put("MINERVA", "MINERV");
        townMap.put("MORIAH", "MORIAH");
        townMap.put("NEWCOMB", "NEWCOM");
        townMap.put("NORTH E", "N ELBA");
        townMap.put("NORTH H", "N HUDS");
        townMap.put("SCHROON", "SCHROO");
        townMap.put("ST. ARM", "ST ARM");
        townMap.put("TICONDE", "TICOND");
        townMap.put("WESTPOR", "WESTPO");
        townMap.put("WILLSBO", "WILLSB");
        townMap.put("WILMING","WILMIN");
    }

    @Override
    public void save(DataSource db) throws Exception {
        Connection conn = db.getConnection();
        BufferedReader br = new BufferedReader(new FileReader(street_file));

        String line;
        String[] parts;
        br.readLine(); // Skip the header
        while( (line = br.readLine()) != null) {
            parts = line.split(",");
            BOEAddressRange range = new BOEAddressRange();
            range.street = parts[0];
            range.zip5 = Integer.parseInt(parts[1]);
            range.bldgLoNum = Integer.parseInt(parts[2]);
            range.bldgHiNum = Integer.parseInt(parts[3]);
            range.bldgParity = getParity(parts[4]);
            range.town = parts[5];
            range.townCode = getTownCode(range.town);
            range.electionCode = Integer.parseInt(parts[6]);
            range.assemblyCode = Integer.parseInt(parts[7]);
            range.congressionalCode = Integer.parseInt(parts[8]);
            range.senateCode = Integer.parseInt(parts[9]);
            range.countyCode = this.county_code;
            range.state = "NY";

            if (!conn.isValid(1)) {
                conn.close();
                conn = db.getConnection();
            }
            save_record(range, conn);
        }

        br.close();
    }

    public String getTownCode(String town) {
        String abbrev = town.substring(0, 7);
        if (townMap.containsKey(abbrev)) {
            throw new RuntimeException("Town "+town+" not found in the town code map.");
        } else {
            return townMap.get(abbrev);
        }
    }

    public String getParity(String parity) {
        if(parity == "Evan and Odd Numbers")
            return "ALL";
        else if (parity == "Even Numbers")
            return "EVENS";
        else if (parity == "Odd Numbers")
            return "ODDS";
        else
            throw new RuntimeException("Invalid parity input "+parity);
    }
}