package gov.nysenate.sage.service.street;

import gov.nysenate.sage.provider.StreetFile;
import gov.nysenate.sage.service.base.ServiceProviders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Point of access for street lookup requests.
*/
@Service
public class StreetLookupServiceProvider extends ServiceProviders<StreetLookupService> {

    @Autowired
    public StreetLookupServiceProvider() {
        registerDefaultProvider("streetfile", StreetFile.class);
    }
}
