package gov.nysenate.sage.provider.district;

import gov.nysenate.sage.BaseTests;
import gov.nysenate.sage.annotation.IntegrationTest;
import gov.nysenate.sage.config.DatabaseConfig;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.model.result.ResultStatus;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertNotEquals;

@Category(IntegrationTest.class)
public class DistrictShapefileTest extends BaseTests {

    @Autowired private DistrictShapefile districtShapefile;

    private static final List<DistrictType> types = Arrays.asList(DistrictType.ASSEMBLY, DistrictType.CONGRESSIONAL,
            DistrictType.COUNTY, DistrictType.SENATE, DistrictType.SCHOOL, DistrictType.TOWN, DistrictType.ZIP);

    @Ignore
    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void shouldNotDistrictAssignCityGeocodeQuality() {
        Address addr = new Address("", "Delmar", "NY", "");
        addr.setUspsValidated(false);
        Geocode geo = new Geocode(new Point(42.6220235, -73.8326232), GeocodeQuality.CITY);
        geo.setCached(false);
        geo.setRawQuality(0);
        geo.setMethod("HttpGoogleDao");
        GeocodedAddress geoAddr = new GeocodedAddress(addr, geo);
        DistrictResult res = districtShapefile.assignDistricts(geoAddr, types, false, false);
        System.out.println(res);
        assertNotEquals(ResultStatus.SUCCESS, res.getStatusCode());

        // ResultStatus.INSUFFICIENT_GEOCODE
    }
}