package gov.nysenate.sage.dao.logger;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.base.ReturnIdHandler;
import gov.nysenate.sage.model.address.Address;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

@Repository
public class AddressLogger extends BaseDao
{
    private static Logger logger = Logger.getLogger(AddressLogger.class);
    private static String SCHEMA = "log";
    private static String TABLE = "address";
    private QueryRunner run = getQueryRunner();

    /**
     * Inserts an address into the address table and returns the address id. If an exact match already exists
     * that address id is returned instead of inserting an identical entry.
     * @param address
     * @return int address id or -1 if not found
     */
    public int logAddress(Address address)
    {
        if (address != null) {
            try {
                int retrievedId = getAddressId(address);
                if (retrievedId > 0) return retrievedId;

                String sql = "INSERT INTO " + SCHEMA + "." + TABLE + "(addr1, addr2, city, state, zip5, zip4) \n" +
                             "VALUES (?, ?, ?, ?, ?, ?) \n" +
                             "RETURNING id";
                return run.query(sql, new ReturnIdHandler(), address.getAddr1(), address.getAddr2(), address.getCity(),
                                                             address.getState(), address.getZip5(), address.getZip4());
            }
            catch (SQLException ex) {
                logger.error("Failed to log address!", ex);
            }
        }
        return 0;
    }

    /**
     * Attempts to retrieve the address id of the given address by
     * @param address
     * @return int address id or -1 if not found
     */
    public int getAddressId(Address address)
    {
        if (address != null) {
            String sql = "SELECT * FROM " + SCHEMA + "." + TABLE + "\n" +
                         "WHERE addr1 ILIKE ? AND addr2 ILIKE ? AND city ILIKE ? AND state ILIKE ? AND " +
                               "zip5 ILIKE ? AND zip4 ILIKE ?" +
                         "LIMIT 1";
            try {
                return run.query(sql, new ReturnIdHandler(), address.getAddr1(), address.getAddr2(), address.getCity(),
                                                             address.getState(), address.getZip5(), address.getZip4());
            }
            catch (SQLException ex) {
                logger.error("Failed to get address id!", ex);
            }
        }
        return 0;
    }
}
