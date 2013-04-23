package gov.nysenate.sage.service;

import gov.nysenate.sage.TestBase;
import gov.nysenate.sage.factory.ApplicationFactory;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.result.GeocodeResult;
import gov.nysenate.sage.service.district.DistrictServiceProvider;
import gov.nysenate.sage.service.geo.GeocodeServiceProvider;
import gov.nysenate.sage.util.FormatUtil;
import org.junit.Test;

public class DistrictServiceProviderTest extends TestBase
{
    GeocodeServiceProvider gsp = ApplicationFactory.getGeocodeServiceProvider();
    DistrictServiceProvider dsp = ApplicationFactory.getDistrictServiceProvider();

    @Test
    public void assignDistrictsDefaultTest() {
        GeocodeResult geocodeResult = gsp.geocode(new Address("3 Tyron St", "Albany", "NY", "12203"));
        FormatUtil.printObject(dsp.assignDistricts(geocodeResult.getGeocodedAddress()));
    }

    @Test
    public void assignNeighborsTest() {
        GeocodeResult geocodeResult = gsp.geocode(new Address("84-50 169st", "Jamaica", "NY", "11432"));
        //FormatUtil.printObject(dsp.assignNeighbors(dsp.newInstance("shapefile"), dsp.assignDistricts(geocodeResult.getGeocodedAddress())));
    }
}
