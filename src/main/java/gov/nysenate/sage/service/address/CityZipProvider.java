package gov.nysenate.sage.service.address;

import gov.nysenate.sage.provider.cityzip.CityZipService;

import java.util.Map;

public interface CityZipProvider {

    /**
     * Retuns the default CityZipService as configured in the App properties
     * @return
     */
    public CityZipService getDefaultProvider();

    /**
     * Returns a map of CityZip service names to the City zip service
     * @return
     */
    public Map<String, CityZipService> getProviders();
}
