package gov.nysenate.sage.boe.StreetFiles;

import gov.nysenate.sage.boe.BOEAddressRange;
import gov.nysenate.sage.boe.StreetFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

public class Suffolk extends StreetFile {

    public HashMap<String, String> townMap;
    public Pattern numberPattern = Pattern.compile("^([0-9]*)([A-Z]*)([0-9]*)$");

    public Suffolk(int county_code, File street_file) throws Exception {
        super(county_code, street_file);
        townMap = new HashMap<String, String>();
        townMap.put("Shelter Island", "SHELTE");
        townMap.put("Brookhaven", "BROOKH");
        townMap.put("Huntington", "HUNTIN");
        townMap.put("Islip", "ISLIP");
        townMap.put("Babylon", "BABYLO");
        townMap.put("Smithtown", "SMITHT");
        townMap.put("Southaven", "SOUTHA");
        townMap.put("East Hampton", "E HAMP");
        townMap.put("Southold", "SOUTHO");
        townMap.put("Riverhead", "RIVERH");
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
            range.street = (parts[2]+" "+parts[3]+" "+parts[4]+parts[5]).trim();
            range.zip5 = Integer.parseInt(parts[0]);
            range.town = parts[13];
            range.townCode = getTownCode(range.town);
            range.electionCode = Integer.parseInt(parts[14]);
            range.assemblyCode = Integer.parseInt(parts[17]);
            range.congressionalCode = Integer.parseInt(parts[15]);
            range.senateCode = Integer.parseInt(parts[16]);
            range.countyCode = this.county_code;
            range.state = "NY";
            range.bldgParity = getParity(parts[8]);
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

            if(!conn.isValid(1)) {
                conn.close();
                conn = db.getConnection();
            }
            save_record(range, conn);
        }

        br.close();
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
        if (number.trim().equals(""))
            return new Number(0, "");

        // This is kind of complex, but it will throw exceptions for unexpected number patterns
        Matcher numberMatcher = numberPattern.matcher(number);
        if (numberMatcher.find()) {
            int num = 0;
            String chr = "";

            if (numberMatcher.group(1) != null && numberMatcher.group(3) != null) {
                throw new RuntimeException("Number "+number+" has integers before and after a character group!");
            } else if (numberMatcher.group(1) != null) {
                num = Integer.parseInt(numberMatcher.group(1));
            } else if (numberMatcher.group(3) != null) {
                num = Integer.parseInt(numberMatcher.group(3));
            }

            if (numberMatcher.group(2) != null) {
                chr = numberMatcher.group(2);
            }

            return new Number(num, chr);

        } else {
            throw new RuntimeException("Non-empty number "+number+" doesn't match pattern "+numberPattern.pattern());
        }
    }

    public String getTownCode(String town) {
        if (townMap.containsKey(town)) {
            return townMap.get(town);
        } else {
            throw new RuntimeException("Town "+town+" not found in the town map.");
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
            throw new RuntimeException("Invalid parity string: "+parity);
        }
    }
}
