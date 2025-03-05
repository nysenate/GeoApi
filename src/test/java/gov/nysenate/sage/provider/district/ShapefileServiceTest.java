package gov.nysenate.sage.provider.district;

import gov.nysenate.sage.BaseTests;
import gov.nysenate.sage.annotation.IntegrationTest;
import gov.nysenate.sage.config.DatabaseConfig;
import gov.nysenate.sage.model.address.BuildingAddress;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.geo.Geocode;
import gov.nysenate.sage.model.geo.GeocodeQuality;
import gov.nysenate.sage.model.geo.Point;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.provider.geocode.Geocoder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.assertNotEquals;

@Category(IntegrationTest.class)
public class ShapefileServiceTest extends BaseTests {
    @Autowired private DistrictService districtService;

    @Ignore
    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void shouldNotDistrictAssignCityGeocodeQuality() {
        var addr = new BuildingAddress("", "Delmar", "NY", "");
        addr.setUspsValidated(false);
        Geocode geo = new Geocode(new Point("42.6220235", "-73.8326232"), GeocodeQuality.CITY, Geocoder.GOOGLE, false);
        GeocodedAddress geoAddr = new GeocodedAddress(addr, geo);
        DistrictResult res = districtService.assignDistricts(List.of(DistrictSource.SHAPEFILE), geoAddr);
        assertNotEquals(ResultStatus.SUCCESS, res.getStatusCode());
    }
}
