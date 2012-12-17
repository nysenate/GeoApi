package gov.nysenate.sage.boe.StreetFiles;

import gov.nysenate.sage.boe.BOEAddressRange;
import gov.nysenate.sage.boe.StreetFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.util.HashMap;

import javax.sql.DataSource;

import org.apache.commons.dbutils.QueryRunner;

public class Schoharie extends StreetFile {
	public int currentLine;
    public HashMap<String, String> townMap;

    public Schoharie(int county, File street_file) throws Exception {
        super(county, street_file);

        townMap = new HashMap<String, String>();

    }

    @Override
    public void save(DataSource db) throws Exception {
    	logger.info("Starting Schoharie");
    	currentLine = 0;
        Connection conn = db.getConnection();
        BufferedReader br = new BufferedReader(new FileReader(street_file));


        String line;
        String[] parts;
        br.readLine(); // Skip the header
        new QueryRunner().update(conn, "BEGIN");
        while( (line = br.readLine()) != null) {
            parts = line.replace("\"","").split(",");
            BOEAddressRange range = new BOEAddressRange();
            range.street = parts[0];
            if (!parts[1].isEmpty())
            	range.zip5 = Integer.parseInt(parts[1]);
            range.bldgLoNum = Integer.parseInt(parts[2]);
            range.bldgHiNum = Integer.parseInt(parts[3]);
            range.bldgParity = parts[4];
            range.town = parts[6]; // towns are shortened, no good way about this
            range.townCode =  parts[5];
            range.electionCode = Integer.parseInt(parts[7]);
            range.assemblyCode = Integer.parseInt(parts[8]);
            range.congressionalCode = Integer.parseInt(parts[9]);
            range.senateCode = Integer.parseInt(parts[10]);
            range.countyCode = this.county_code;
            range.state = "NY";

            if (!conn.isValid(1)) {
                conn.close();
                conn = db.getConnection();
            }
            save_record(range, conn);

            if(++currentLine % 5000 == 0) {
            	new QueryRunner().update(conn, "COMMIT");
            	new QueryRunner().update(conn, "BEGIN");
            }
        }
        new QueryRunner().update(conn, "COMMIT");
        br.close();
        logger.info("Done with Schoharie");
    }

    public String getTownCode(String town) {
        String abbrev = town.substring(0, Math.min(town.length(),7));
        if (townMap.containsKey(abbrev)) {
        	return townMap.get(abbrev);
        } else {
        	throw new RuntimeException("Line "+currentLine+": Town "+town+" not found in the town code map.");
        }
    }

    public String getParity(String parity) {
        if(parity.equals("Even and Odd Numbers"))
            return "ALL";
        else if (parity.equals("Even Numbers"))
            return "EVENS";
        else if (parity.equals("Odd Numbers"))
            return "ODDS";
        else {
            logger.error("Line "+currentLine+": Invalid parity input "+parity);
        	return "ALL";
        }
    }
}