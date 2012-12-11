package gov.nysenate.sage.boe.StreetFiles;

import gov.nysenate.sage.boe.BOEAddressRange;
import gov.nysenate.sage.boe.StreetFile;

import java.io.File;
import java.sql.Connection;

import javax.sql.DataSource;

public class Fulton extends StreetFile {

    public int[] zipcodes;

    public Fulton(int county_code, File street_file) throws Exception {
        super(county_code, street_file);

        // No Street File for this county so we'll use this.
        // 12010 - [min] - Perth
        // 12025 - [most] - Broadalbin
        // 12032 - [some] - Caroga
        // 12068 - [min]
        // 12070 - [some]
        // 12078 - [full] - City of Gloversville / Town of Johnstown / Bleeker
        // 12095 - [most] - City of Johnstown / Town of Johnstown
        // 12117 - [full] - Mayfield
        // 12134 - [min] - Northampton
        // 13329 - [some] - Oppenheim
        // 13339 - [min]
        // 13452 - [most] - Ephratah
        // 13470 - [most] - Stratford
        zipcodes = new int[] {12010,12025,12032,12068,12070,12078,12095,12117,12134,13329,13452,13470};
    }

    @Override
    public void save(DataSource db) throws Exception {
    	logger.info("Starting Fulton");
        Connection conn = db.getConnection();

        BOEAddressRange range = new BOEAddressRange();
        range.state = "NY";
        range.countyCode = 17;
        range.senateCode = 49;
        range.assemblyCode = 118;
        range.congressionalCode = 21;

        for (int zip5 : zipcodes) {
            range.zip5 = zip5;
            save_record(range, conn);
        }
        logger.info("Done with Fulton");
    }

}
