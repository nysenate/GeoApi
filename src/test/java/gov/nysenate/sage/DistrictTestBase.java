package gov.nysenate.sage;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.DistrictedAddress;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.service.district.DistrictService;
import gov.nysenate.sage.util.FormatUtil;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public abstract class DistrictTestBase
{
    private static Logger logger = Logger.getLogger(DistrictTestBase.class);

    public static List<DistrictedAddress> expected = new ArrayList<>(Arrays.asList(
            new DistrictedAddress(new GeocodedAddress(new Address("44 Fairlawn Ave", "Albany", "NY", "12203"), new Geocode(new Point(42.670583, -73.799606))),
                    new DistrictInfo(20, 1, 44, 109, "-ALBAN", "005")),
            new DistrictedAddress(new GeocodedAddress(new Address("2025 County Road 4", "Stanley", "NY", "14561"), new Geocode(new Point(42.885892, -77.0916801))),
                    new DistrictInfo(23, 32, 54, 131, "SENECA", "493")),
            new DistrictedAddress(new GeocodedAddress(new Address("8450 169st", "Jamaica", "NY", "11432"), new Geocode(new Point(40.71515, -73.795274))),
                    new DistrictInfo(6, 63, 11, 24, "-NYC", "519")),
            new DistrictedAddress(new GeocodedAddress(new Address("45 3rd St", "Troy", "NY", "12180"), new Geocode(new Point(42.730677, -73.690341))),
                    new DistrictInfo(20, 38, 44, 108, "-TROY", "642"))
    ));;

    /** Test basic method functionality */
    public static void assertSingleAddressDistrictAssign(DistrictService districtService)
    {
        DistrictResult districtResult = districtService.assignDistricts(expected.get(0).getGeocodedAddress());
        FormatUtil.printObject(districtResult);
        assertDistrictInfoEquals(expected.get(0).getDistrictInfo(), districtResult.getDistrictedAddress().getDistrictInfo());
    }

    public static void assertMultipleAddressDistrictAssign(DistrictService districtService)
    {
        List<GeocodedAddress> geocodedAddresses = new ArrayList<>();
        for (DistrictedAddress d : expected ) { geocodedAddresses.add(d.getGeocodedAddress()); }
        List<DistrictResult> districtResults = districtService.assignDistricts(geocodedAddresses);

        for (int i = 0; i < expected.size(); i++) {
            assertDistrictInfoEquals(expected.get(i).getDistrictInfo(), districtResults.get(i).getDistrictInfo());
        }
    }

    public static void assertSingleAddressDistrictAssignWithTypes(DistrictService districtService)
    {
        ArrayList<DistrictType> types = new ArrayList<>(Arrays.asList(DistrictType.CONGRESSIONAL));
        DistrictResult districtResult = districtService.assignDistricts(expected.get(0).getGeocodedAddress(), types);
        FormatUtil.printObject(districtResult);
        assertEquals(expected.get(0).getDistrictInfo().getCongressionalCode(), districtResult.getDistrictInfo().getCongressionalCode());
        assertEquals(0, districtResult.getDistrictInfo().getAssemblyCode());
        assertEquals("", districtResult.getDistrictInfo().getSchoolCode());
    }

    public static void assertMultipleAddressDistrictAssignWithTypes(DistrictService districtService)
    {
        ArrayList<DistrictType> types = new ArrayList<>(Arrays.asList(DistrictType.CONGRESSIONAL, DistrictType.COUNTY));
        List<GeocodedAddress> geocodedAddresses = new ArrayList<>();
        for (DistrictedAddress d : expected ) { geocodedAddresses.add(d.getGeocodedAddress()); }

        List<DistrictResult> districtResults = districtService.assignDistricts(geocodedAddresses, types);

        for (int i = 0; i < expected.size(); i++){
            assertEquals(expected.get(i).getDistrictInfo().getCongressionalCode(), districtResults.get(i).getDistrictInfo().getCongressionalCode());
            assertEquals(expected.get(i).getDistrictInfo().getCountyCode(), districtResults.get(i).getDistrictInfo().getCountyCode());
            assertEquals(0, districtResults.get(i).getDistrictInfo().getAssemblyCode());
            assertEquals("", districtResults.get(i).getDistrictInfo().getSchoolCode());
        }
    }

    /** Checks to see the the assignedDistricts collection contains the assigned districts */
    public static void assertAssignedDistrictsSet_IsPopulated(DistrictService districtService)
    {
        ArrayList<DistrictType> types = new ArrayList<>(Arrays.asList(DistrictType.CONGRESSIONAL, DistrictType.COUNTY));
        DistrictResult districtResult = districtService.assignDistricts(expected.get(0).getGeocodedAddress(), types);
        assertEquals(2, districtResult.getAssignedDistricts().size());
        assertTrue(districtResult.getAssignedDistricts().contains(DistrictType.CONGRESSIONAL));
        assertTrue(districtResult.getAssignedDistricts().contains(DistrictType.COUNTY));
        assertFalse(districtResult.getAssignedDistricts().contains(DistrictType.ASSEMBLY));
        assertFalse(districtResult.getAssignedDistricts().contains(DistrictType.SENATE));
        assertFalse(districtResult.getAssignedDistricts().contains(DistrictType.SCHOOL));
    }

    /** Test invalid input cases */
    public static void assertDistrictAssign_MissingInput_SetsStatus(DistrictService districtService)
    {
        GeocodedAddress geocodedAddress = null;
        DistrictResult districtResult = districtService.assignDistricts(geocodedAddress);
        assertEquals(ResultStatus.MISSING_INPUT_PARAMS, districtResult.getStatusCode());
    }

    public static void assertDistrictAssign_MissingGeocode_SetsStatus(DistrictService districtService)
    {
        GeocodedAddress geocodedAddress = new GeocodedAddress(new Address());
        DistrictResult districtResult = districtService.assignDistricts(geocodedAddress);
        assertEquals(ResultStatus.MISSING_GEOCODE, districtResult.getStatusCode());
    }

    public static void assertDistrictAssign_MissingAddress_SetsStatus(DistrictService districtService)
    {
        GeocodedAddress geocodedAddress = new GeocodedAddress();
        geocodedAddress.setGeocode(new Geocode());
        DistrictResult districtResult = districtService.assignDistricts(geocodedAddress);
        assertEquals(ResultStatus.MISSING_ADDRESS, districtResult.getStatusCode());
    }

    /** Helpers / Comparators */
    public static void assertDistrictInfoEquals(DistrictInfo expected, DistrictInfo actual)
    {
        assertEquals(expected.getSchoolCode(), actual.getSchoolCode());
        assertEquals(expected.getAssemblyCode(), actual.getAssemblyCode());
        assertEquals(expected.getCongressionalCode(), actual.getCongressionalCode());
        assertEquals(expected.getSenateCode(), actual.getSenateCode());
        assertEquals(expected.getCountyCode(), actual.getCountyCode());
        assertEquals(expected.getTownCode(), actual.getTownCode());
    }

}
