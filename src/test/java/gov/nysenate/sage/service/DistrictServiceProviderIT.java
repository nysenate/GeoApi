package gov.nysenate.sage.service;

import gov.nysenate.sage.BaseTests;
import gov.nysenate.sage.annotation.IntegrationTest;
import gov.nysenate.sage.config.DatabaseConfig;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.service.district.DistrictServiceProvider;
import gov.nysenate.sage.service.geo.GeocodeServiceProvider;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertNotNull;

@Category(IntegrationTest.class)
public class DistrictServiceProviderIT extends BaseTests {

    @Autowired
    GeocodeServiceProvider geocodeServiceProvider;

    @Autowired
    DistrictServiceProvider districtServiceProvider;

    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void assignDistrictsDefaultTest() {
        GeocodeResult geocodeResult =
                geocodeServiceProvider.geocode(
                        new Address("3 Tyron St", "Albany", "NY", "12203"));
        assertNotNull(districtServiceProvider.assignDistricts(geocodeResult.getGeocodedAddress()));
    }

    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void assignNeighborsTest() {
        GeocodeResult geocodeResult =
                geocodeServiceProvider.geocode(
                        new Address("350 5th Ave", "New York", "NY", "10118"));
        assertNotNull(geocodeResult);
    }
}
