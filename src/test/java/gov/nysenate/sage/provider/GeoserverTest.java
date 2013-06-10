package gov.nysenate.sage.provider;

import static gov.nysenate.sage.DistrictTestBase.*;
import gov.nysenate.sage.TestBase;
import org.apache.log4j.Logger;

import org.junit.Before;
import org.junit.Test;

public class GeoserverTest extends TestBase
{
    private static Logger logger = Logger.getLogger(GeoserverTest.class);
    private Geoserver geoserver;

    @Before
    public void setUp()
    {
        geoserver = new Geoserver();
    }

    @Test
    public void singleAddressDistrictAssign_ReturnsDistrictResult()
    {
        assertSingleAddressDistrictAssign(geoserver);
    }

    @Test
    public void multipleAddressDistrictAssign_ReturnsDistrictResults()
    {
        assertMultipleAddressDistrictAssign(geoserver);
    }

    @Test
    public void assignWithTypesTest()
    {
        assertSingleAddressDistrictAssignWithTypes(geoserver);
        assertMultipleAddressDistrictAssignWithTypes(geoserver);
    }

    /** When the input is null make sure that the correct errors are propagated */
    @Test
    public void invalidInputTest()
    {
        assertDistrictAssign_MissingInput_SetsStatus(geoserver);
        assertDistrictAssign_MissingGeocode_SetsStatus(geoserver);
        assertDistrictAssign_MissingAddress_SetsStatus(geoserver);
    }

    @Test
    public void assignedDistrictsIsSetProperly()
    {
        assertAssignedDistrictsSet_IsPopulated(geoserver);
    }
}
