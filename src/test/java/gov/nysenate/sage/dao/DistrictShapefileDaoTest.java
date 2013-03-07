package gov.nysenate.sage.dao;

import gov.nysenate.sage.TestBase;
import gov.nysenate.sage.dao.provider.DistrictShapefileDao;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.geo.Point;
import org.junit.Before;
import org.junit.Test;

public class DistrictShapefileDaoTest extends TestBase
{
    DistrictShapefileDao dsDao;

    @Before
    public void setUp()
    {
        dsDao = new DistrictShapefileDao();
    }

    @Test
    public void getDistrictInfoFromPoint()
    {
        dsDao.getDistrictInfo(new Point(40.72460669, -73.732267), DistrictType.getStandardTypes(), false);
    }
}
