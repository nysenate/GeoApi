package gov.nysenate.sage.service.base;

import org.apache.log4j.Logger;

import java.util.*;

/**
 * This class is essentially a simple way to keep track of which classes can serve as an
 * implementation of a given service and instantiate them.
 * @param <T> the Service to provide implementations for.
 */
public class ServiceProviders<T>
{
    private Logger logger = Logger.getLogger(this.getClass());
    protected Map<String,Class<? extends T>> providers = new HashMap<>();
    protected String defaultProvider = "default";
    protected LinkedList<String> defaultFallback = new LinkedList<>();

    /**
     * Registers the default service as an instance of the given provider.
     * @param provider  The provider that should be default.
     */
    public void registerDefaultProvider(String providerName, Class<? extends T> provider)
    {
        registerProvider(providerName, provider);
        setDefaultProvider(providerName);
    }

    /**
     * If providerName has already been registered, set it as the default provider.
     * @param providerName The provider that should be default
     */
    public void setDefaultProvider(String providerName)
    {
        if (providerName != null && providers.containsKey(providerName.toLowerCase())) {
            defaultProvider = providerName.toLowerCase();
        }
    }

    /**
     * Registers an instance of a service implementation.
     * @param providerName  Key that will be used to reference this provider.
     * @param provider      The provider class.
     */
    public void registerProvider(String providerName, Class<? extends T> provider)
    {
        providers.put(providerName.toLowerCase(), provider);
    }

    /**
     * Sets the default fallback chain which is an ordered list of providers to
     * use upon failure.
     * @param fallbackChain List of provider names
     */
    public void setProviderFallbackChain(List<String> fallbackChain)
    {
        defaultFallback = new LinkedList<>(fallbackChain);
    }

    /**
     * Returns the set of mapped keys.
     * @return
     */
    public Set<String> getProviderNames()
    {
        return providers.keySet();
    }

    /**
     * Determines if given providerName is registered
     * @param providerName
     * @return
     */
    public boolean isRegistered(String providerName)
    {
        return (providerName != null && !providerName.isEmpty() && this.providers.containsKey(providerName.toLowerCase()));
    }

    /**
     * Returns a new instance of the default T implementation.
     * @return   T if default provider is set.
     *           null if default provider not set.
     */
    public T newInstance()
    {
        if (providers.containsKey(defaultProvider)){
            return newInstance(defaultProvider);
        }
        else {
            logger.warn("Default service provider not registered!");
            return null;
        }
    }

    /**
     * Returns a new instance of the provider that has been registered
     * with the given providerName.
     * @param providerName
     * @return  T instance specified by providerName.
     *          null if provider is not specified/registered.
     */
    public T newInstance(String providerName)
    {
        if (providerName != null && !providerName.isEmpty()) {
            if (providers.containsKey(providerName.toLowerCase())){
                try {
                    return (T) providers.get(providerName.toLowerCase()).newInstance();
                }
                catch (InstantiationException ie){
                    logger.error(ie.getMessage());
                }
                catch (IllegalAccessException iea){
                    logger.error(iea.getMessage());
                }
            }
            else {
                logger.debug(providerName + " is not a registered provider!");
            }
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
    public T newInstance(String providerName, boolean useFallback)
    {
        if (providerName != null){
            T a = newInstance(providerName);
            if (a != null){
                return a;
            }
        }
        else if (useFallback){
            return newInstance();
        }
        return null;
    }

    /**
     * Allows for a specified fallback service if the provider does not exist.
     * @param providerName
     * @param fallbackProviderName
     * @return T if providerName or fallbackProviderName valid
     *         null otherwise
     */
    public T newInstance(String providerName, String fallbackProviderName)
    {
        T a = newInstance(providerName);
        return (a != null) ? a : newInstance(fallbackProviderName);
    }
}
