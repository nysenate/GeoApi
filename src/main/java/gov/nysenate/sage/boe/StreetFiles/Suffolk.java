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

public class Suffolk extends StreetFile {

	public int currentLine;
    public HashMap<String, String> townMap;
    public Pattern numberPattern = Pattern.compile("^([0-9]+)?-?([A-Z]+)?-?([0-9]+)?$");

    public Suffolk(int county_code, File street_file) throws Exception {
        super(county_code, street_file);
        townMap = new HashMap<String, String>();
        townMap.put("Shelter Island", "SHELTE");
        townMap.put("Brookhaven", "BROOKH");
        townMap.put("Huntington", "HUNTIN");
        townMap.put("Islip", "ISLIP");
        townMap.put("Babylon", "BABYLO");
        townMap.put("Smithtown", "SMITHT");
        townMap.put("Southampton", "SOUTHA");
        townMap.put("East Hampton", "E HAMP");
        townMap.put("Southold", "SOUTHO");
        townMap.put("Riverhead", "RIVERH");
    }

    @Override
    public void save(DataSource db) throws Exception {
    	logger.info("Starting Suffolk.");
    	QueryRunner runner = new QueryRunner(db);
        BufferedReader br = new BufferedReader(new FileReader(street_file));
        
        currentLine = 0;
        String line;
        String[] parts;
        br.readLine(); // Skip the header
        runner.update("BEGIN");
        while( (line = br.readLine()) != null) {
        	// Remove the quotes and split on tabs
            parts = line.replace("\"", "").split("\t");
            BOEAddressRange range = new BOEAddressRange();
            range.street = (parts[2]+" "+parts[3]+" "+parts[4]+parts[5]).trim();
            range.zip5 = Integer.parseInt(parts[0]);
            range.town = parts[13];
            range.townCode = getTownCode(range.town);
            range.electionCode = Integer.parseInt(parts[14]);
            range.congressionalCode = Integer.parseInt(parts[15]);
            range.senateCode = Integer.parseInt(parts[16]);
            range.assemblyCode = Integer.parseInt(parts[17]);
            if (parts.length > 18)
            	range.clegCode = parts[18];
            if (parts.length > 20)
            	range.schoolCode = parts[20];
            if (parts.length > 21)
            	range.fireCode = parts[21];
            if (parts.length > 22)
            	range.villCode = parts[22];

            range.countyCode = this.county_code;
            range.state = "NY";
            range.bldgParity = getParity(parts[8]);
            if (!parts[12].isEmpty())
            	range.aptParity = getParity(parts[12]);
            Number bldgLo = splitNumber(parts[6]);
            Number bldgHi = splitNumber(parts[7]);
            Number aptLo = splitNumber(parts[10]);
            Number aptHi = splitNumber(parts[11]);
            range.bldgLoNum = bldgLo.num;
            range.bldgLoChr = bldgLo.chr;
            range.bldgHiNum = bldgHi.num;
            range.bldgHiChr = bldgHi.chr;
            range.aptLoNum = aptLo.num;
            range.aptLoChr = aptLo.chr;
            range.aptHiNum = aptHi.num;
            range.aptHiChr = aptHi.chr;

            save_record(range, db);

        	if(++currentLine % 5000 == 0) {
        		runner.update("COMMIT");
        		runner.update("BEGIN");
        	}
        }
        runner.update("COMMIT");
        br.close();
        logger.info("Done with Suffolk");
    }

    public class Number {
        public int num;
        public String chr;
        public Number(int num, String chr) {
            this.num = num;
            this.chr = chr;
        }
    }

    public Number splitNumber(String number) {
    	int num = 0;
        String chr = "";

        // This is kind of complex, but it will throw exceptions for unexpected number patterns
        Matcher numberMatcher = numberPattern.matcher(number);
        if (numberMatcher.find()) {

            if (numberMatcher.group(1) != null && numberMatcher.group(3) != null) {
                logger.error("Line "+currentLine+": Number "+number+" has integers before and after a character group!");
            } else if (numberMatcher.group(1) != null) {
                num = Integer.parseInt(numberMatcher.group(1));
            } else if (numberMatcher.group(3) != null) {
                num = Integer.parseInt(numberMatcher.group(3));
            }

            if (numberMatcher.group(2) != null) {
                chr = numberMatcher.group(2);
            }

        } else {
            logger.error("Line "+currentLine+": Non-empty number "+number+" doesn't match pattern "+numberPattern.pattern());
        }

        return new Number(num, chr);
    }

    public String getTownCode(String town) {
        if (townMap.containsKey(town)) {
            return townMap.get(town);
        } else {
            logger.error("Line "+currentLine+": Town "+town+" not found in the town map.");
            return "";
        }
    }

    public String getParity(String parity) {
        if (parity.equals("N") || parity.equals("B")) {
            return "ALL";
        } else if (parity.equals("O")) {
            return "ODDS";
        } else if (parity.equals("E")) {
            return "EVENS";
        } else {
            logger.error("Line "+currentLine+": Invalid parity string: "+parity);
            return "ALL";
        }
    }
}
