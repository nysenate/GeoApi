package gov.nysenate.sage.service.geo;

import gov.nysenate.sage.service.ServiceProviders;
import gov.nysenate.sage.service.geo.GeoCodeService;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class GeoCodeServiceProviders implements ServiceProviders
{
    private static Logger logger = Logger.getLogger(GeoCodeServiceProviders.class);
    private static Map<String,GeoCodeService> providers = new HashMap<>();
    private static final String DEFAULT_PROVIDER = "default";

    private GeoCodeServiceProviders() {}

    /**
     * Registers the default GeoCodeService as an instance of the given provider.
     * @param provider  The GeoCodeService implementation that should be default.
     */
    public static void registerDefaultProvider(GeoCodeService provider)
    {
        providers.put(DEFAULT_PROVIDER, provider);
    }

    /**
     * Registers an instance of an GeoCodeService implementation.
     * @param providerName  Key that will be used to reference this provider.
     * @param provider      An instance of the provider.
     */
    public static void registerProvider(String providerName, GeoCodeService provider)
    {
        providers.put(providerName.toLowerCase(), provider);
    }

    /**
     * Returns a new instance of the default GeoCodeService implementation.
     * @return   GeoCodeService if default provider is set.
     *           null if default provider not set.
     */
    public static GeoCodeService newServiceInstance()
    {
        if (providers.containsKey(DEFAULT_PROVIDER)){
            return providers.get(DEFAULT_PROVIDER).newInstance();
        }
        else {
            logger.debug("Default address provider not registered!");
            return null;
        }
    }

    /**
     * Returns a new instance of the GeoCodeService that has been registered
     * with the given providerName.
     * @param providerName
     * @return  GeoCodeService instance specified by providerName.
     *          null if provider is not registered.
     */
    public static GeoCodeService newServiceInstance(String providerName)
    {
        if (providers.containsKey(providerName.toLowerCase())){
            return providers.get(providerName.toLowerCase()).newInstance();
        }
        else {
            logger.debug(providerName + " is not a registered address provider!");
            return null;
        }
    }
}
