package gov.nysenate.sage.service.address;

import gov.nysenate.sage.provider.cityzip.CityZipDB;
import gov.nysenate.sage.provider.cityzip.CityZipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service provider definition for the cityZip service.
 */
@Service
public class CityZipServiceProvider { //cityzipdb

    protected CityZipService defaultProvider;
    protected Map<String,CityZipService> providers = new HashMap<>();

    @Autowired
    public CityZipServiceProvider(CityZipDB cityZipDB) {
        this.defaultProvider = cityZipDB;
        this.providers.put("cityZipDB", this.defaultProvider);
    }

    public CityZipService getDefaultProvider() {
        return defaultProvider;
    }

    public Map<String, CityZipService> getProviders() {
        return providers;
    }
}
