package gov.nysenate.sage.service.street;

import gov.nysenate.sage.provider.district.StreetLookupService;

import java.util.Map;

public interface SageStreetLookUpProvider {

    /**
     * Returns the default StreetLookUpService as configured in app properties
     * @return
     */
    public StreetLookupService getDefaultProvider();

    /**
     * Return a map containing a StreetLookupService and its keyword
     * @return
     */
    public Map<String, StreetLookupService> getProviders();
}
