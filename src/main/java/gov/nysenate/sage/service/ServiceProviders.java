package gov.nysenate.sage.service;

import org.apache.log4j.Logger;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is used for registering and obtaining implementation instances for a
 * particular service identified by the template parameter.
 *
 * For example, using ExampleService as the service and exampleImpl as an instance:
 * <code>
 * ServiceProviders<ExampleService> exampleServiceProvider = new ServiceProviders<>();
 * exampleServiceProvider.registerDefaultProvider("impl", exampleImpl); // Register
 * ExampleService impl = exampleServiceProvider.newServiceInstance();   // Get new instance
 * </code>
 *
 * So essentially it's a simple way to keep track of which classes can serve as an
 * implementation of a given service and instantiate them.
 * @param <T>   T is the Service to provide implementations for.
 */
public class ServiceProviders<T>
{
    private Logger logger = Logger.getLogger(this.getClass());
    private Map<String,T> providers = new HashMap<>();
    private String defaultProvider = "default";

    /**
     * Registers the default service as an instance of the given provider.
     * @param provider  The service implementation that should be default.
     */
    public void registerDefaultProvider(String providerName, T provider)
    {
        defaultProvider = providerName;
        providers.put(defaultProvider, provider);
    }

    /**
     * Registers an instance of a service implementation.
     * @param providerName  Key that will be used to reference this provider.
     * @param provider      An instance of the provider.
     */
    public void registerProvider(String providerName, T provider)
    {
        providers.put(providerName.toLowerCase(), provider);
    }

    /**
     * Returns a new instance of the default T implementation.
     * @return   T if default provider is set.
     *           null if default provider not set.
     */
    public T newServiceInstance()
    {
        if (providers.containsKey(defaultProvider)){
            return newServiceInstance(defaultProvider);
        }
        else {
            logger.debug("Default address provider not registered!");
            return null;
        }
    }

    /**
     * Returns a new instance of the provider that has been registered
     * with the given providerName.
     * @param providerName
     * @return  T instance specified by providerName.
     *          null if provider is not registered.
     */
    public T newServiceInstance(String providerName)
    {
        if (providers.containsKey(providerName.toLowerCase())){
            try {
                return (T) providers.get(providerName.toLowerCase()).getClass().newInstance();
            }
            catch (InstantiationException ie){
                logger.error(ie.getMessage());
            }
            catch (IllegalAccessException iea){
                logger.error(iea.getMessage());
            }
        }
        else {
            logger.debug(providerName + " is not a registered address provider!");
        }
        return null;
    }

    /**
     * Allows for fallback to a default service if the provider does not exist.
     * @param providerName
     * @param useFallback - Set true to use the default registered provider if
     *                      providerName does not exist.
     * @return  T if providerName found or useFallback:true
     *          null otherwise
     */
    public T newServiceInstance(String providerName, boolean useFallback)
    {
        if (providerName != null){
            T a = newServiceInstance(providerName);
            if (a != null){
                return a;
            }
        }
        else if (providerName == null || useFallback){
            return newServiceInstance();
        }
        return null;
    }
}
