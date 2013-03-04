package gov.nysenate.sage.dao.provider;

import gov.nysenate.sage.boe.BOEAddressRange;
import gov.nysenate.sage.boe.BOEStreetAddress;
import gov.nysenate.sage.dao.base.BaseDao;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StreetFileDao extends BaseDao
{
    private Logger logger = Logger.getLogger(StreetFileDao.class);

    public StreetFileDao() {}



    protected List<BOEAddressRange> getRanges(BOEStreetAddress address, boolean useStreet, boolean fuzzy, boolean useHouse) throws SQLException {
        ArrayList<Object> params = new ArrayList<Object>();
        String sql = "SELECT * FROM streetfile WHERE 1=1 \n";

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
            if (true) {
                System.out.println(sql);
                for (Object o : params) {
                    System.out.println(o);
                }
            }
            return null;
            //return runner.query(sql, rangeHandler, params.toArray());
        } else {
            if (true) {
                System.out.println("Skipping address: no identifying information");
            }
            return new ArrayList<BOEAddressRange>();
        }
    }
}
