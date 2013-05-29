package gov.nysenate.sage.dao.log;

import gov.nysenate.sage.dao.base.BaseDao;
import gov.nysenate.sage.model.address.Address;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.log4j.Logger;

public class GeoRequestLogger extends BaseDao
{
    private static Logger logger = Logger.getLogger(GeoRequestLogger.class);
    private QueryRunner runner = getQueryRunner();

    public void logGeoRequest(Address address, String provider, boolean useFallback, boolean useCache)
    {

    }

    public void getGeoRequests()
    {

    }


}
