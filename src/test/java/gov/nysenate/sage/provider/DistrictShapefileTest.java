package gov.nysenate.sage.provider;

import gov.nysenate.sage.TestBase;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.MapResult;
import gov.nysenate.sage.model.result.ResultStatus;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DistrictShapefileTest extends TestBase
{
   DistrictShapefile districtShapefile = new DistrictShapefile();

   @Test
   public void testGetDistrictMap()
   {
       MapResult mapResult;
       mapResult = districtShapefile.getDistrictMap(DistrictType.SENATE, "44");
       assertEquals(DistrictType.SENATE, mapResult.getDistrictMap().getDistrictType());
       assertEquals("44", mapResult.getDistrictMap().getDistrictCode());
       assertTrue(mapResult.getDistrictMap().getPolygons().size() > 0);
       assertEquals(ResultStatus.SUCCESS, mapResult.getStatusCode());
   }

    @Test
    public void testGetDistrictMaps()
    {
        MapResult mapResult;
        mapResult = districtShapefile.getDistrictMaps(DistrictType.SENATE);
        assertEquals(ResultStatus.SUCCESS, mapResult.getStatusCode());
        assertEquals(63, mapResult.getDistrictMaps().size());
    }

    @Test
    public void testGetDistrictMapWithEmptyCode()
    {
        MapResult mapResult;
        mapResult = districtShapefile.getDistrictMap(DistrictType.SENATE, null);
        assertEquals(ResultStatus.MISSING_DISTRICT_CODE, mapResult.getStatusCode());
    }

    @Test
    public void testGetDistrictOverlap()
    {
        districtShapefile.getMultiMatchResult(null, null);
    }

}
