package gov.nysenate.sage.service.address;

import gov.nysenate.sage.service.ServiceProviders;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class AddressServiceProviders implements ServiceProviders
{
    private static Logger logger = Logger.getLogger(AddressServiceProviders.class);
    private static Map<String,AddressService> providers = new HashMap<>();
    private static final String DEFAULT_PROVIDER = "default";

    private AddressServiceProviders() {}

    /**
     * Registers the default AddressService as an instance of the given provider.
     * @param provider  The AddressService implementation that should be default.
     */
    public static void registerDefaultProvider(AddressService provider)
    {
        providers.put(DEFAULT_PROVIDER, provider);
    }

    /**
     * Registers an instance of an AddressService implementation.
     * @param providerName  Key that will be used to reference this provider.
     * @param provider      An instance of the provider.
     */
    public static void registerProvider(String providerName, AddressService provider)
    {
        providers.put(providerName.toLowerCase(), provider);
    }

    /**
     * Returns a new instance of the default AddressService implementation.
     * @return   AddressService if default provider is set.
     *           null if default provider not set.
     */
    public static AddressService newServiceInstance()
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
     * Returns a new instance of the AddressProvider that has been registered
     * with the given providerName.
     * @param providerName
     * @return  AddressService instance specified by providerName.
     *          null if provider is not registered.
     */
    public static AddressService newServiceInstance(String providerName)
    {
        if (providers.containsKey(providerName.toLowerCase())){
            return providers.get(providerName.toLowerCase()).newInstance();
        }
        else {
            logger.debug(providerName + " is not a registered address provider!");
            return null;
        }
    }

    /**
     * Allows for fallback to a default service if the provider does not exist.
     * @param providerName
     * @param useFallback - Set true to use the default registered provider if
     *                      providerName does not exist.
     * @return  AddressService if providerName found or useFallback:true
     *          null otherwise
     */
    public static AddressService newServiceInstance(String providerName, boolean useFallback)
    {
        AddressService a;
        if (providerName != null){
            a = newServiceInstance(providerName);
            if (a != null){
                return a;
            }
        }
        return (useFallback) ? newServiceInstance() : null;
    }
}
