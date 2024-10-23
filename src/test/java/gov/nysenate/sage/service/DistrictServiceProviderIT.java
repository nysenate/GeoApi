package gov.nysenate.sage.service;

import gov.nysenate.sage.BaseTests;
import gov.nysenate.sage.annotation.IntegrationTest;
import gov.nysenate.sage.config.DatabaseConfig;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.provider.geocode.Geocoder;
import gov.nysenate.sage.service.district.DistrictServiceProvider;
import gov.nysenate.sage.service.geo.SageGeocodeServiceProvider;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.assertNotNull;

@Category(IntegrationTest.class)
public class DistrictServiceProviderIT extends BaseTests {

    @Autowired
    SageGeocodeServiceProvider geocodeServiceProvider;

    @Autowired
    DistrictServiceProvider districtServiceProvider;

    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void assignDistrictsDefaultTest() {
        GeocodeResult geocodeResult =
                geocodeServiceProvider.geocode(
                        new Address("3 Tyron St", "Albany", "NY", "12203"),
                        List.of(Geocoder.GOOGLE), true);
        assertNotNull(districtServiceProvider.assignDistricts(geocodeResult.getGeocodedAddress(), null,
                DistrictType.getStandardTypes(), DistrictServiceProvider.DistrictStrategy.neighborMatch));
    }

    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void assignNeighborsTest() {
        GeocodeResult geocodeResult =
                geocodeServiceProvider.geocode(
                        new Address("350 5th Ave", "New York", "NY", "10118"),
                        List.of(Geocoder.GOOGLE), true);
        assertNotNull(geocodeResult);
    }
}
