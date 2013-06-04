package gov.nysenate.sage.dao.logger;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.dao.base.ReturnIdHandler;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.result.GeocodeResult;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.log4j.Logger;

import java.sql.SQLException;

public class GeocodeResultLogger extends BaseDao
{
    private static Logger logger = Logger.getLogger(GeocodeResultLogger.class);
    private static AddressLogger addressLogger = new AddressLogger();
    private static String SCHEMA = "log";
    private static String TABLE = "geocodeResults";
    private QueryRunner run = getQueryRunner();

    /**
     * Log a GeocodeResult to the database. Requires the id handle of the subsequent GeocodeRequest.
     * @param geocodeResult
     * @return int id of the logged geocode result.
     */
    public int logGeocodeResult(int geocodeRequestId, GeocodeResult geocodeResult)
    {
        if (geocodeResult != null && geocodeRequestId > -1) {
            boolean success =  geocodeResult.isSuccess();
            int addressId = (success) ? addressLogger.logAddress(geocodeResult.getAddress()) : -1;

            String sql =
                    "INSERT INTO " + SCHEMA + "." + TABLE + "(geocodeRequestId, success, addressId, method, quality, latlon, resultTime) \n" +
                    "VALUES (?, ?, ?, ?, ?, ST_GeomFromText(?), ?) \n" +
                    "RETURNING id";
            try {
                Geocode geocode = geocodeResult.getGeocode();
                String method = (success) ? geocode.getMethod() : null;
                String quality = (success) ? geocode.getQuality().name() : null;
                String point = (success) ? "POINT(" + geocode.getLat() + " " + geocode.getLat() + ")" : null;
                return run.query(sql, new ReturnIdHandler(), geocodeRequestId, success, addressId, method, quality, point, geocodeResult.getResultTime());
            }
            catch (SQLException ex) {
                logger.error("Failed to log geocode result!", ex);
            }
        }
        return -1;
    }
}
