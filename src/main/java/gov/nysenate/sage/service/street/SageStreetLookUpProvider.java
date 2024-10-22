package gov.nysenate.sage.service.street;

import gov.nysenate.sage.provider.district.StreetLookupService;

public interface SageStreetLookUpProvider {
    StreetLookupService getDefaultProvider();
}
