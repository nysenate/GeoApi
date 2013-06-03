package gov.nysenate.sage.dao.log;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.result.GeocodeResult;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.log4j.Logger;

public class GeocodeResultLogger extends BaseDao
{
    private static Logger logger = Logger.getLogger(GeocodeResultLogger.class);
    private QueryRunner run = getQueryRunner();

    public void logGeocodeResponse(GeocodeResult geocodeResult)
    {

    }

}
