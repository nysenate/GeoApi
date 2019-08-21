package gov.nysenate.sage.service.street;

import gov.nysenate.sage.provider.district.StreetFile;
import gov.nysenate.sage.provider.district.StreetLookupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Point of access for street lookup requests.
*/
@Service
public class StreetLookupServiceProvider implements SageStreetLookUpProvider { //Streetfile

    protected StreetLookupService defaultProvider;
    protected Map<String,StreetLookupService> providers = new HashMap<>();

    @Autowired
    public StreetLookupServiceProvider(StreetFile streetFile) {
        this.defaultProvider = streetFile;
        providers.put("streetfile", this.defaultProvider);
    }

    public StreetLookupService getDefaultProvider() {
        return defaultProvider;
    }

    public Map<String, StreetLookupService> getProviders() {
        return providers;
    }
}
