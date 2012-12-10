package gov.nysenate.sage.boe.StreetFiles;

import gov.nysenate.sage.boe.BOEAddressRange;
import gov.nysenate.sage.boe.StreetFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;


public abstract class NTS extends StreetFile {
    public final Pattern street_pattern = Pattern.compile("^(.*?)\\s+([0-9]+)\\s+(\\d+)\\s*-\\s*(\\d+)\\s+(ODDS|EVENS|)\\s*(INCLUSIVE|EXCLUSIVE|)\\s+((?:[*'.A-Z-/()]+ {0,8})+)\\s+(?:\\s{1,6}(\\w{1,3}|   ))(?:\\s{1,6}(\\w{1,3}|   ))(?:\\s{1,6}(\\w{1,3}|   ))?(?:\\s{1,6}(\\w{1,3}|   ))?(?:\\s{1,6}(\\w{1,3}|   ))?(?:\\s{1,6}(\\w{1,3}|   ))?(?:\\s{1,6}(\\w{1,3}|   ))?(?:\\s{1,6}(\\w{1,3}|   ))?(?:\\s{1,6}(\\w{1,3}|   ))?(?:\\s{1,6}(\\w{1,3}|   ))?(?:\\s{1,9}(\\w{1,3}|   ))?$");

    public NTS(int countyCode,File street_file) throws Exception {
        super(countyCode, street_file);
    }

    public void skipToStart(BufferedReader br) throws IOException {
        String line = "";
        while (line!=null && !line.contains("Street Name")) { line = br.readLine(); }
    }

    public boolean isEndPage(String line) {
        return line.contains("r_strstd") || line.contains("Total No. of Street Segments");
    }

    public abstract void store_extra_districts(BOEAddressRange addressRange, Matcher matcher);

    @Override
    public void save(DataSource db) throws Exception {
        Connection conn = db.getConnection();
        BufferedReader br = new BufferedReader(new FileReader(street_file));

        int count = 0;
        String line;
        BOEAddressRange addressRange;
        while( (line = br.readLine()) != null) {
            skipToStart(br);
            addressRange = new BOEAddressRange();

            while ((line = br.readLine())!= null) {
                if (!conn.isValid(1)) {
                    conn.close();
                    conn = db.getConnection();
                }

                if (isEndPage(line)) {
                    save_record(addressRange, conn);
                    break;
                }

                // A blank line indicates a new street block
                if (line.trim().length()==0) {
                    save_record(addressRange, conn);
                    addressRange = new BOEAddressRange();
                    continue;
                }

                Matcher great = street_pattern.matcher(line.toUpperCase());
                if(!great.find()) {
                    // If the format doesn't match it must be a street name overflow
                    if (!line.matches("\\(.*\\)")) {
                        addressRange.setStreet(addressRange.getStreet().trim()+" "+line.trim());
                        System.out.println("Extended Street: "+addressRange.getStreet());
                    }

                } else {
                    save_record(addressRange, conn);

                    addressRange.setStreet(great.group(1).length() != 0 ? great.group(1) : addressRange.getStreet());
                    addressRange.setZip5(Integer.parseInt(great.group(2)));

                    Matcher lo = number_pattern.matcher(great.group(3));
                    if (lo.find()) {
                        addressRange.setBldgLoNum(Integer.parseInt(lo.group(1)));
                        addressRange.setBldgLoChr(lo.group(2));
                    }
                    Matcher hi = number_pattern.matcher(great.group(4));
                    if (hi.find()) {
                        addressRange.setBldgHiNum(Integer.parseInt(hi.group(1)));
                        addressRange.setBldgHiChr(hi.group(2));
                    }

                    addressRange.setBldgParity(great.group(5).length() == 0 ? "ALL" : great.group(5));
                    // bounds = great.group(6); // This is always absent or inclusive
                    addressRange.setTown(great.group(7).replaceAll("\\s{2,}", " "));
                    addressRange.setTownCode(great.group(8) != null ? great.group(8).trim() : "000");
                    addressRange.setWardCode(great.group(9) != null ? great.group(9).trim() : "000");
                    addressRange.setElectionCode(great.group(10) != null ? Integer.parseInt(great.group(10).trim()) : 0);
                    addressRange.setCongressionalCode(great.group(11) != null ? Integer.parseInt(great.group(11).trim()) : 0);
                    addressRange.setSenateCode(great.group(12) != null ? Integer.parseInt(great.group(12).trim()) : 0);
                    addressRange.setAssemblyCode(great.group(13) != null ? Integer.parseInt(great.group(13).trim()) : 0);
                    addressRange.setSchoolCode(great.group(14) != null ? great.group(14).trim() : "000");
                    addressRange.setCountyCode(county_code);
                    store_extra_districts(addressRange, great);
                    //AddressUtils.normalizeAddress(addressRange);
                }
            }
            System.out.println("Done with Page "+count);
            count++;
        }
    }
}