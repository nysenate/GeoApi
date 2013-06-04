package gov.nysenate.sage.dao.logger;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.base.ReturnIdHandler;
import gov.nysenate.sage.model.address.Address;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.log4j.Logger;

import java.sql.SQLException;

public class AddressLogger extends BaseDao
{
    private static Logger logger = Logger.getLogger(AddressLogger.class);
    private static String SCHEMA = "log";
    private static String TABLE = "addresses";
    private QueryRunner run = getQueryRunner();

    public int logAddress(Address address)
    {
        if (address != null) {
            try {
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
        return -1;
    }
}
