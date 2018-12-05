package gov.nysenate.sage.service.address;

import gov.nysenate.sage.provider.cityzip.CityZipDB;
import gov.nysenate.sage.provider.cityzip.CityZipService;
import gov.nysenate.sage.service.base.ServiceProviders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service provider definition for the cityZip service.
 */
@Service
public class CityZipServiceProvider extends ServiceProviders<CityZipService> {

    @Autowired
    public CityZipServiceProvider() {
        registerDefaultProvider("cityZipDB", CityZipDB.class);
    }
}
