package gov.nysenate.sage.boe;

import gov.nysenate.sage.util.Resource;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.BeanProcessor;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DistrictLookup {
    public MysqlDataSource db;
    public Connection conn;
    public QueryRunner runner;
    public ResultSetHandler<List<BOEAddressRange>> rangeHandler;

    public boolean DEBUG = false;

    public static void main(String[] args) throws Exception {
        System.out.println("Starting up...");

        Resource config = new Resource();
        MysqlDataSource db = new MysqlDataSource();
        db.setServerName(config.fetch("db.host"));
        db.setUser(config.fetch("db.user"));
        db.setPassword(config.fetch("db.pass"));
        db.setDatabaseName(config.fetch("db.name"));
        System.out.println(config.fetch("db.host")+"|"+config.fetch("db.user")+"|"+config.fetch("db")+"|"+config.fetch("db.name"));

        DistrictLookup streetData = new DistrictLookup(db);
        BOEStreetAddress address = AddressUtils.parseAddress("1204 10th Ave, Colonie NY 12189");
        System.out.println(address);

        for (BOEAddressRange range : streetData.getRanges(address)) {
            System.out.println(range.getId());
        }
    }

    public DistrictLookup(MysqlDataSource db) {
        runner = new QueryRunner(db);
        Map<String, String> column_map = new HashMap<String, String>();
        column_map.put("congressional_code", "congressionalCode");
        column_map.put("senate_code", "senateCode");
        column_map.put("election_code","electionCode");
        column_map.put("county_code","countyCode");
        column_map.put("assembly_code","assemblyCode");
        column_map.put("bldg_lo_num","bldgLoNum");
        column_map.put("bldg_hi_num","bldgHiNum");
        column_map.put("apt_lo_num", "aptLoNum");
        column_map.put("apt_hi_num", "aptHiNum");
        column_map.put("town_code","townCode");
        column_map.put("ward_code", "wardCode");
        column_map.put("school_code", "schoolCode");
        column_map.put("cleg_code","clegCode");
        column_map.put("fire_code","fireCode");
        column_map.put("city_code","cityCode");
        column_map.put("vill_code","villCode");
        BeanProcessor rowProcessor = new BeanProcessor(column_map);
        rangeHandler = new BeanListHandler<BOEAddressRange>(BOEAddressRange.class, new BasicRowProcessor(rowProcessor));
        
        try {
            conn = db.getConnection();
        } catch (SQLException ex) {
            Logger.getLogger(DistrictLookup.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public List<BOEAddressRange> getRangesByZip(BOEStreetAddress address) throws SQLException {
        System.out.println("SELECT * FROM street_data WHERE zip5="+address.zip5);
        return runner.query("SELECT * FROM street_data WHERE zip5=?", rangeHandler, address.zip5);
    }

    public List<BOEAddressRange> getRanges(BOEStreetAddress address, boolean use_building) throws SQLException {
        ArrayList<Object> a = new ArrayList<Object>();
        String sql = "SELECT * \n"
                   + "FROM street_data \n"
                   + "WHERE 1=1 \n";

        if (address.zip5 != 0) {
            sql += "  AND zip5=? \n";
            a.add(address.zip5);
        }

        if (address.state != null && !address.state.equals("")) {
            sql += "  AND state=? \n";
            a.add(address.state);
        }

        if (address.street != null && !address.street.equals("")) {
            // Pretty sure this doesn't cause issues..
            address.street = address.street.replaceAll(" EXT$", "");

            // Sometimes the bldg_chr is actually the tail end of the street name
            if (address.bldg_chr != null && !address.bldg_chr.equals("") && use_building==true) {
//              // Sometimes the apt_chr is actually the tail end of the street name
//              if (!address.apt_chr.equals("")) {
//                  // TODO
//              } else {
//                  // TODO
//              }
              sql += "  AND (street LIKE ? OR (street LIKE ? AND (bldg_lo_chr='' OR bldg_lo_chr <= ?) AND (bldg_hi_chr='' OR ? <= bldg_hi_chr))) \n";
              a.add(address.bldg_chr+" "+address.street+"%");
              a.add(address.street+"%");
              a.add(address.bldg_chr);
              a.add(address.bldg_chr);

            } else {
                sql += "  AND (street LIKE ?) \n";
                a.add(address.street+"%");
//              // Sometimes the apt_chr is actually the tail end of the street name
//                if (address.apt_chr.equals("")) {
//                    sql += "  AND (street LIKE ?) \n";
//                    a.add(address.street+"%");
//
//                } else {
//                    sql += "  AND (street LIKE ? OR street LIKE ?)";
//                    a.add(address.street+"%");
//                    a.add(address.street+" "+address.apt_chr+"%");
//                }

            }

            if (address.bldg_num != 0 && use_building==true) {
                sql += "  AND (bldg_lo_num <= ? AND ? <= bldg_hi_num AND (bldg_parity='ALL' or bldg_parity=? )) \n";
                a.add(address.bldg_num);
                a.add(address.bldg_num);
                a.add((address.bldg_num % 2 == 0 ? "EVENS" : "ODDS"));
            }
        }

        if (DEBUG) {
            System.out.println(sql);
            for (Object o : a) {
                System.out.println(o);
            }
        }
        
        if ( !conn.isValid(0) ) {
            conn.close();
            conn = db.getConnection();
        }
        
        return runner.query(conn, sql, rangeHandler, a.toArray());
    }

    public List<BOEAddressRange> getRanges(BOEStreetAddress address) throws SQLException {
        return getRanges(address, true);
    }
}
