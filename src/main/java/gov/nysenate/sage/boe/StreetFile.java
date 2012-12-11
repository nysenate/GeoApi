package gov.nysenate.sage.boe;


import gov.nysenate.sage.Address;
import gov.nysenate.sage.Result;
import gov.nysenate.sage.service.AddressService;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.log4j.Logger;

public abstract class StreetFile {
    public final File street_file;
    protected final Logger logger;
    public final int county_code;
    public final AddressService addressService;
    public final Pattern number_pattern = Pattern.compile("(\\d+|)(.*)");

    public StreetFile(int countyCode, File street_file) throws Exception  {
        if (false && !street_file.canRead()) {
            throw new IOException("Cannot read "+street_file.getAbsolutePath());
        } else {
            this.street_file = street_file;
        }
        this.county_code = countyCode;
        logger = Logger.getLogger(this.getClass());
        this.addressService = new AddressService();
    }

    public abstract void save(DataSource db) throws Exception;

    public static void main(String[] args) {
        System.out.println("1  10th ABBEY CT".replaceAll("^[0-9]+\\s+", ""));
    }

    public void save_record(BOEAddressRange addressRange, Connection conn) throws Exception {
//        if (!addressRange.isValid()) {
//            return;
//        }
        AddressUtils.normalizeAddress(addressRange);
        new QueryRunner().update(conn, "INSERT INTO street_data (street,town,state,zip5,bldg_lo_num, bldg_lo_chr, bldg_hi_num, bldg_hi_chr, bldg_parity, apt_lo_num, apt_lo_chr, apt_hi_num, apt_hi_chr, apt_parity, election_code, county_code, assembly_code, senate_code, congressional_code, town_code, ward_code, school_code, cleg_code, fire_code, city_code, vill_code) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",addressRange.getStreet(), addressRange.getTown(), "NY", addressRange.getZip5(), addressRange.getBldgLoNum(), addressRange.getBldgLoChr(), addressRange.getBldgHiNum(), addressRange.getBldgHiChr(), addressRange.getBldgParity(), addressRange.getAptLoNum(), addressRange.getAptLoChr(), addressRange.getAptHiNum(), addressRange.getAptHiChr(), addressRange.getAptParity(), addressRange.getElectionCode(), addressRange.getCountyCode(), addressRange.getAssemblyCode(), addressRange.getSenateCode(), addressRange.getCongressionalCode(), addressRange.getTownCode(), addressRange.getWardCode(), addressRange.getSchoolCode(), addressRange.getClegCode(), addressRange.getFireCode(), addressRange.getCityCode(), addressRange.getVillCode());
    }

    public void clear(DataSource db) throws SQLException {
        QueryRunner run = new QueryRunner(db);
        run.update("DELETE FROM street_data WHERE county_code=?",county_code);
    }
}