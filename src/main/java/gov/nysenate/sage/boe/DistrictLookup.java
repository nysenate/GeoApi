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

public class DistrictLookup {
    public MysqlDataSource db;
    public QueryRunner runner;
    public ResultSetHandler<List<BOEAddressRange>> rangeHandler;

    public boolean DEBUG = false;

    public static void main(String[] args) throws Exception {
        System.out.println("Starting up...");

        Resource config = new Resource();
        MysqlDataSource db = new MysqlDataSource();
        db.setServerName(config.fetch("street_db.host"));
        db.setUser(config.fetch("street_db.user"));
        db.setPassword(config.fetch("street_db.pass"));
        db.setDatabaseName(config.fetch("street_db.name"));
        System.out.println(config.fetch("street_db.host")+"|"+config.fetch("street_db.user")+"|"+config.fetch("street_db.pass")+"|"+config.fetch("street_db.name"));

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
        BeanProcessor rowProcessor = new BeanProcessor(column_map);
        rangeHandler = new BeanListHandler<BOEAddressRange>(BOEAddressRange.class, new BasicRowProcessor(rowProcessor));
    }

    public List<BOEAddressRange> getRanges(BOEStreetAddress address) throws SQLException {
        ArrayList<Object> a = new ArrayList<Object>();
        String sql = "SELECT * \n"
                   + "FROM street_data \n"
                   + "WHERE 1=1 \n";

        if (address.zip5 != 0) {
            sql += "  AND zip5=? \n";
            a.add(address.zip5);
        }

        if (!address.street.equals("")) {

            if (address.bldg_chr.equals("")) {
                if (address.apt_chr.equals("")) {
                    sql += "  AND (street=?) \n";
                    a.add(address.street);

                } else {
                    sql += "  AND (street=? OR street=?)";
                    a.add(address.street);
                    a.add(address.street+" "+address.apt_chr);
                }

            } else {
                if (!address.apt_chr.equals("")) {

                } else {

                }
                sql += "  AND (street=? OR (street=? AND (bldg_lo_chr='' OR bldg_lo_chr <= ?) AND (bldg_hi_chr='' OR ? <= bldg_hi_chr))) \n";
                a.add(address.bldg_chr+" "+address.street);
                a.add(address.street);
                a.add(address.bldg_chr);
                a.add(address.bldg_chr);
            }

            if (address.bldg_num != 0) {
                sql += "  AND (bldg_lo_num <= ? AND ? <= bldg_hi_num AND (bldg_parity='ALL' or bldg_parity=? )) \n";
                a.add(address.bldg_num);
                a.add(address.bldg_num);
                a.add((address.bldg_num % 2 == 0 ? "EVENS" : "ODDS"));
            }
        }

        if (!address.town.equals("")) {
            sql += "  AND town=? \n";
            a.add(address.town);
        }

        if (!address.state.equals("")) {
            sql += "  AND state=? \n";
            a.add(address.state);
        }

        if (DEBUG) {
            System.out.println(sql);
            for (Object o : a) {
                System.out.println(o);
            }
        }
        return runner.query(sql, rangeHandler, a.toArray());
    }
}
