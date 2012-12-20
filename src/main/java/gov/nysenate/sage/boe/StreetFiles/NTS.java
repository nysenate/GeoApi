package gov.nysenate.sage.boe.StreetFiles;

import gov.nysenate.sage.boe.AddressUtils;
import gov.nysenate.sage.boe.BOEAddressRange;
import gov.nysenate.sage.boe.StreetFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.commons.dbutils.QueryRunner;


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
        return line.contains("r_strstd") || line.contains("-------------------") || line.contains("Total No. of Street Segments");
    }

    public abstract void store_extra_districts(BOEAddressRange addressRange, Matcher matcher);

    public void nts_save_record(BOEAddressRange range, DataSource db, String line) throws Exception{
        if (range.street != null && !range.street.isEmpty()) {
            save_record(range, db);
        }
    }

    @Override
    public void save(DataSource db) throws Exception {
        logger.info("Starting "+street_file.getName());
        BufferedReader br = new BufferedReader(new FileReader(street_file));
        QueryRunner runner = new QueryRunner(db);
        int count = 0;
        String line;
        BOEAddressRange addressRange;

        runner.update("BEGIN");
        while( (line = br.readLine()) != null) {
            skipToStart(br);
            addressRange = new BOEAddressRange();

            while ((line = br.readLine())!= null) {
                line = line.replace("`", ""); // Sometimes I find random backticks that don't belong

                if (isEndPage(line)) {
                    nts_save_record(addressRange, db, line);
                    break;
                }

                // A blank line indicates a new street block
                if (line.trim().length()==0) {
                    nts_save_record(addressRange, db, line);
                    addressRange = new BOEAddressRange();
                    continue;
                }

                Matcher great = street_pattern.matcher(line.toUpperCase());
                if(!great.find()) {
                    // If the format doesn't match it must be a street name overflow
                    if (!line.matches("\\(.*\\)")) {
                        addressRange.setStreet(addressRange.getStreet().trim()+" "+line.trim());
                        logger.warn("Extended Street: "+addressRange.getStreet());
                    }

                } else {
                    nts_save_record(addressRange, db, line);

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
                    addressRange.setWardCode(great.group(9) != null && !great.group(9).trim().isEmpty() ? Integer.parseInt(great.group(9).trim()) : 0);
                    addressRange.setElectionCode(great.group(10) != null ? Integer.parseInt(great.group(10).trim()) : 0);
                    addressRange.setCongressionalCode(great.group(11) != null ? Integer.parseInt(great.group(11).trim()) : 0);
                    addressRange.setSenateCode(great.group(12) != null ? Integer.parseInt(great.group(12).trim()) : 0);
                    addressRange.setAssemblyCode(great.group(13) != null ? Integer.parseInt(great.group(13).trim()) : 0);
                    addressRange.setSchoolCode(great.group(14) != null ? great.group(14).trim() : "000");
                    addressRange.setCountyCode(county_code);
                    store_extra_districts(addressRange, great);
                    AddressUtils.normalizeAddress(addressRange);
                }
            }

            if (++count % 10 == 0) {
	            logger.info("Done with Page "+count);
	            runner.update("COMMIT");
	            runner.update("BEGIN");
            }
        }
        runner.update("COMMIT");
        logger.info("Done with "+street_file.getName());
    }
}