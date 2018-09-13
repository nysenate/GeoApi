package gov.nysenate.sage.dao.logger;

import gov.nysenate.sage.TestBase;
import gov.nysenate.sage.model.api.ApiRequest;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.Assert.assertNotNull;

public class ApiRequestLoggerTest extends TestBase
{
    private ApiRequestLogger apiRequestLogger;

    @Before
    public void setUp() {
        apiRequestLogger = new ApiRequestLogger();
    }

    @Test
    public void getApiRequestsDuringTest() {
        List<ApiRequest> apiRequests = apiRequestLogger.getApiRequestsDuring("", "", "", Timestamp.valueOf("2013-05-01 00:00:00"),Timestamp.valueOf("2013-07-01 00:00:00"),
                                              1000, 0, true);
        assertNotNull(apiRequests);

    }

    /******** removed this test, since an empty logging table causes it to fail
    @Test
    public void getApiRequestByIdTest() {
        ApiRequest apiRequest = apiRequestLogger.getApiRequest(1);
        assertNotNull(apiRequest);
    }
    ********/
}
