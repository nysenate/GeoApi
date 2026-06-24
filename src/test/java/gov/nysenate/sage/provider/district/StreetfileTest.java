package gov.nysenate.sage.provider.district;

import gov.nysenate.sage.BaseTests;
import gov.nysenate.sage.annotation.IntegrationTest;
import gov.nysenate.sage.config.DatabaseConfig;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.Accuracy;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.provider.geocode.Geocoder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Category(IntegrationTest.class)
public class StreetfileTest extends BaseTests {

    @Autowired
    private DistrictService districtService;

    private static final Set<DistrictType> types = Set.of(DistrictType.ASSEMBLY, DistrictType.CONGRESSIONAL,
            DistrictType.COUNTY, DistrictType.SENATE, DistrictType.SCHOOL, DistrictType.TOWN_CITY, DistrictType.ZIP);

    @Ignore
    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void shouldNotDistrictAssignCityGeocodeQuality() {
        var addr = new Address("", "Delmar", "NY", "");
        var geo = new Geocode(new Point("42.6220235", "-73.8326232"), Accuracy.REGION, Geocoder.GOOGLE, false);
        var geoAddr = new GeocodedAddress(addr, geo);
        DistrictResult districtResult = districtService.assignDistricts(List.of(LocalSource.STREETFILE), geoAddr, types);
        System.out.println(districtResult);
    }
}
