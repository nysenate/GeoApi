package gov.nysenate.sage.dao.logger;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.base.ReturnIdHandler;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.DistrictResult;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.log4j.Logger;

import java.sql.SQLException;

public class DistrictResultLogger extends BaseDao
{
    private static Logger logger = Logger.getLogger(DistrictResultLogger.class);
    private static String SCHEMA = "log";
    private static String TABLE = "districtResults";
    private QueryRunner run = getQueryRunner();

    /**
     * Log a district result into the database
     * @param districtRequestId Id of the logged district request
     * @param dr DistrictResult
     * @return id of the inserted DistrictResult
     */
    public int logDistrictResult(int districtRequestId, DistrictResult dr)
    {
        logger.debug("Starting to log district result");
        if (dr != null) {
            String sd = null, ad = null, cd = null, cc = null;
            if (dr.isSuccess() || dr.isPartialSuccess()) {
                DistrictInfo dinfo = dr.getDistrictedAddress().getDistrictInfo();
                sd = dinfo.getDistCode(DistrictType.SENATE);
                ad = dinfo.getDistCode(DistrictType.ASSEMBLY);
                cd = dinfo.getDistCode(DistrictType.CONGRESSIONAL);
                cc = dinfo.getDistCode(DistrictType.COUNTY);
            }
            String sql = "INSERT INTO " + SCHEMA + "." + TABLE + "(districtrequestid, assigned, status, senatecode, assemblycode," +
                                                                  "congressionalcode, countycode, resulttime) \n" +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?) \n" +
                         "RETURNING id";
            try {
                int id = run.query(sql, new ReturnIdHandler(), districtRequestId, dr.isSuccess() || dr.isPartialSuccess(), dr.getStatusCode().name(),
                                                             sd, ad, cd, cc, dr.getResultTime());
                logger.debug("Saved district result id: " + id);
                return id;
            }
            catch (SQLException ex) {
                logger.error("Failed to log district result!", ex);
            }
        }
        return -1;
    }
}