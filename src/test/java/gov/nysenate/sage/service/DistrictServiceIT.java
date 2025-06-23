package gov.nysenate.sage.service;

import gov.nysenate.sage.BaseTests;
import gov.nysenate.sage.annotation.IntegrationTest;
import gov.nysenate.sage.config.DatabaseConfig;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.provider.district.DistrictService;
import gov.nysenate.sage.provider.geocode.GeocodeService;
import gov.nysenate.sage.provider.geocode.Geocoder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static gov.nysenate.sage.provider.district.LocalSource.SHAPEFILE;
import static gov.nysenate.sage.provider.district.LocalSource.STREETFILE;
import static org.junit.Assert.assertNotNull;

@Category(IntegrationTest.class)
public class DistrictServiceIT extends BaseTests {

    @Autowired
    private GeocodeService geocodeService;

    @Autowired
    private DistrictService districtService;

    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void assignDistrictsDefaultTest() {
        GeocodeResult geocodeResult =
                geocodeService.geocode(List.of(Geocoder.GOOGLE),
                        new Address("3 Tyron St", "Albany", "NY", "12203"));
        assertNotNull(districtService.assignDistricts(List.of(STREETFILE, SHAPEFILE),
                geocodeResult.getGeocodedAddress(), DistrictType.getStandardTypes()));
    }

    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void assignTest() {
        GeocodeResult geocodeResult =
                geocodeService.geocode(List.of(Geocoder.GOOGLE),
                        new Address("350 5th Ave", "New York", "NY", "10118"));
        assertNotNull(geocodeResult);
    }
}
