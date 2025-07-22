package gov.nysenate.sage.provider.district;

import gov.nysenate.sage.BaseTests;
import gov.nysenate.sage.annotation.IntegrationTest;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.provider.geocode.Geocoder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertFalse;

@Category(IntegrationTest.class)
public class ShapefileServiceTest extends BaseTests {
    @Autowired private DistrictService districtService;

    @Ignore
    @Test
    public void shouldNotDistrictAssignCityGeocodeQuality() {
        var addr = new Address("", "Delmar", "NY", "");
        Geocode geo = new Geocode(new Point("42.6220235", "-73.8326232"), GeocodeQuality.CITY, Geocoder.GOOGLE, false);
        GeocodedAddress geoAddr = new GeocodedAddress(addr, geo);
        DistrictResult res = districtService.assignDistricts(List.of(LocalSource.SHAPEFILE), geoAddr, Set.of(DistrictType.SENATE));
        assertFalse(res.isSuccess());
    }
}
