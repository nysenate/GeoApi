package gov.nysenate.sage.boe;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.BeanProcessor;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.log4j.Logger;

public class DistrictLookup {
    public QueryRunner runner;
    public ResultSetHandler<List<BOEAddressRange>> rangeHandler;
    public final Logger logger = Logger.getLogger(DistrictLookup.class);
    public boolean DEBUG = false;

    public DistrictLookup(DataSource db) throws Exception {
        runner = new QueryRunner(db);
        Map<String, String> column_map = new HashMap<String, String>();
        column_map.put("congressional_code", "congressionalCode");
        column_map.put("senate_code", "senateCode");
        column_map.put("election_code","electionCode");
        column_map.put("county_code","countyCode");
        column_map.put("assembly_code","assemblyCode");
        column_map.put("bldg_lo_num","bldgLoNum");
        column_map.put("bldg_hi_num","bldgHiNum");
        column_map.put("bldg_parity","bldgParity");
        column_map.put("apt_lo_num", "aptLoNum");
        column_map.put("apt_hi_num", "aptHiNum");
        column_map.put("apt_parity","aptParity");
        column_map.put("town_code","townCode");
        column_map.put("ward_code", "wardCode");
        column_map.put("school_code", "schoolCode");
        column_map.put("cleg_code","clegCode");
        column_map.put("fire_code","fireCode");
        column_map.put("city_code","cityCode");
        column_map.put("vill_code","villCode");
        BeanProcessor rowProcessor = new BeanProcessor(column_map);
        rangeHandler = new BeanListHandler<BOEAddressRange>(BOEAddressRange.class, new BasicRowProcessor(rowProcessor));
    }

    public List<BOEAddressRange> getRangesByZip(BOEStreetAddress address) throws SQLException {
        return getRanges(address, false, false, false);
    }

    public List<BOEAddressRange> getRangesByHouse(BOEStreetAddress address) throws SQLException {
        List<BOEAddressRange> ranges = getRanges(address, true, false, true);
        if (ranges.size()==0) {
            return getRanges(address, true, true, true);
        } else {
            return ranges;
        }
    }

    public List<BOEAddressRange> getRangesByStreet(BOEStreetAddress address) throws SQLException {
        List<BOEAddressRange> ranges = getRanges(address, true, false, false);
        if (ranges.size()==0) {
            return getRanges(address, true, true, false);
        } else {
            return ranges;
        }
    }

    public List<BOEAddressRange> getRanges(BOEStreetAddress address, boolean useStreet, boolean fuzzy, boolean useHouse) throws SQLException {
        ArrayList<Object> params = new ArrayList<Object>();
        String sql = "SELECT * \n"
                   + "FROM street_data \n"
                   + "WHERE 1=1 \n";

        boolean whereZip = (address.zip5 != 0);
        boolean whereState = (address.state != null && !address.state.isEmpty());
        boolean whereStreet = (useStreet && address.street != null && !address.street.isEmpty());
        boolean whereBldg = (useHouse && address.bldg_num != 0);
        boolean whereBldgChr = (useHouse && address.bldg_chr != null && !address.bldg_chr.isEmpty());

        if (whereZip) {
            sql += "  AND zip5=? \n";
            params.add(address.zip5);
        }

        if (whereState) {
            sql += "  AND state=? \n";
            params.add(address.state);
        }

        if (whereStreet) {
            // Pretty sure this doesn't cause issues..
            address.street = address.street.replaceAll(" EXT$", "");

            // Sometimes the bldg_chr is actually the tail end of the street name
            if (whereBldgChr) {
                // Handle dashed NYC buildings by collapsing on the dash
                if (address.bldg_chr.startsWith("-"))  {
                    try {
                        address.bldg_num = Integer.parseInt(String.valueOf(address.bldg_num)+address.bldg_chr.substring(1));
                        address.bldg_chr = null;
                    } catch (NumberFormatException e) {
                        logger.warn("bldg_chr `"+address.bldg_chr+"` not as expected.");
                    }
                }

                // Every one else gets a range check; sometimes the suffix is actually part of the street prefix.
                if (address.bldg_chr != null) {
                    if (fuzzy) {
                        sql += "  AND (street LIKE ? OR (street LIKE ? AND (bldg_lo_chr='' OR bldg_lo_chr <= ?) AND (bldg_hi_chr='' OR ? <= bldg_hi_chr))) \n";
                        params.add(address.bldg_chr+" "+address.street+"%");
                        params.add(address.street+"%");
                    } else {
                        sql += "  AND (street = ? OR (street = ? AND (bldg_lo_chr='' OR bldg_lo_chr <= ?) AND (bldg_hi_chr='' OR ? <= bldg_hi_chr))) \n";
                        params.add(address.bldg_chr+" "+address.street);
                        params.add(address.street);
                    }
                    params.add(address.bldg_chr);
                    params.add(address.bldg_chr);
                }

            } else {
                if (fuzzy) {
                    sql += "  AND (street LIKE ?) \n";
                    params.add(address.street+"%");
                } else {
                    sql += "  AND (street = ?) \n";
                    params.add(address.street);
                }
            }

            if (whereBldg) {
                sql += "  AND (bldg_lo_num <= ? AND ? <= bldg_hi_num AND (bldg_parity='ALL' or bldg_parity=? )) \n";
                params.add(address.bldg_num);
                params.add(address.bldg_num);
                params.add((address.bldg_num % 2 == 0 ? "EVENS" : "ODDS"));
            }
        }

        // Only do a lookup if we have meaningful filters on the query
        if (whereZip || whereStreet) {
            if (DEBUG) {
                System.out.println(sql);
                for (Object o : params) {
                    System.out.println(o);
                }
            }

            return runner.query(sql, rangeHandler, params.toArray());
        } else {
            if (DEBUG) {
                System.out.println("Skipping address: no identifying information");
            }
            return new ArrayList<BOEAddressRange>();
        }
    }
}
