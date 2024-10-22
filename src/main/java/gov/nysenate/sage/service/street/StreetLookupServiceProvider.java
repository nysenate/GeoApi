package gov.nysenate.sage.service.street;

import gov.nysenate.sage.provider.district.StreetLookupService;
import gov.nysenate.sage.provider.district.Streetfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Point of access for street lookup requests.
*/
@Service
public class StreetLookupServiceProvider implements SageStreetLookUpProvider {
    private final StreetLookupService defaultProvider;
    private final Map<StreetData, StreetLookupService> providers;

    @Autowired
    public StreetLookupServiceProvider(Streetfile streetFile) {
        this.defaultProvider = streetFile;
        // TODO: can add in street shapefile as source
        this.providers = Map.of(StreetData.STREETFILE, defaultProvider);
    }

    public StreetLookupService getDefaultProvider() {
        return defaultProvider;
    }
}
