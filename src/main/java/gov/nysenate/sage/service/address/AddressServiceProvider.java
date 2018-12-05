package gov.nysenate.sage.service.address;

import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.dao.logger.address.SqlAddressLogger;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.provider.address.AddressService;
import gov.nysenate.sage.provider.address.USPSAIS;
import gov.nysenate.sage.provider.address.USPSAMS;
import gov.nysenate.sage.util.AddressUtil;
import gov.nysenate.sage.util.TimeUtil;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;

@Service
public class AddressServiceProvider
{
    private static Logger logger = LoggerFactory.getLogger(AddressServiceProvider.class);
    private SqlAddressLogger sqlAddressLogger;
    private Environment env;

    protected AddressService defaultProvider;
    protected Map<String,AddressService> providers = new HashMap<>();
    protected LinkedList<String> defaultFallBack = new LinkedList<>();

    private boolean API_LOGGING_ENABLED = false;
    private boolean SINGLE_LOGGING_ENABLED = false;
    private boolean BATCH_LOGGING_ENABLED = false;

    @Autowired
    public AddressServiceProvider(SqlAddressLogger sqlAddressLogger, Environment env,
                                  USPSAMS uspsams, USPSAIS uspsais) {
        this.sqlAddressLogger = sqlAddressLogger;
        this.env = env;
        API_LOGGING_ENABLED = env.isApiLoggingEnabled();
        SINGLE_LOGGING_ENABLED = API_LOGGING_ENABLED && env.isDetailedLoggingEnabled();
        BATCH_LOGGING_ENABLED = API_LOGGING_ENABLED && env.isBatchDetailedLoggingEnabled();

        providers.put("usps",uspsams);
        providers.put("uspsais", uspsais);

        String defaultUspsProvider = env.getUspsDefault();
        if (defaultUspsProvider.equals("usps")) {
            this.defaultProvider = uspsams;
            defaultFallBack.add("uspsais");
        }
        else {
            this.defaultProvider = uspsais;
            defaultFallBack.add("usps");
        }
    }

    /**
     * Validates an address using USPS or another provider if available.
     * @param address  Address to validate
     * @param provider Provide to use (if null, defaults will be used)
     * @param usePunct If true, validated address will have periods after abbreviations.
     * @return AddressResult
     */
    public AddressResult validate(Address address, String provider, Boolean usePunct)
    {
        AddressResult addressResult;
        Timestamp startTime = TimeUtil.currentTimestamp();

        /** Perform null check */
        if (address == null) {
            return new AddressResult(this.getClass(), ResultStatus.MISSING_ADDRESS);
        }
        /** Use provider if specified */
        if (provider != null && !provider.isEmpty()) {
            if (this.providers.containsKey(provider)) {
                addressResult = this.providers.get(provider).validate(address);
            }
            else {
                return new AddressResult(this.getClass(), ResultStatus.ADDRESS_PROVIDER_NOT_SUPPORTED);
            }
        }
        /** Use USPS if address is eligible */
        else if (address.isEligibleForUSPS()) {
            addressResult = this.providers.get(provider).validate(address);
        }
        /** Otherwise address is insufficient */
        else {
            addressResult = new AddressResult(this.getClass(), ResultStatus.INSUFFICIENT_ADDRESS);
        }

        if (addressResult != null && addressResult.isValidated()) {
            logger.info(String.format("USPS validate time: %d ms", TimeUtil.getElapsedMs(startTime)));
            /** Apply punctuation to the address if requested */
            if (usePunct) {
                addressResult.setAddress(AddressUtil.addPunctuation(addressResult.getAddress()));
            }
        }
        logAddressResult(addressResult);
        return addressResult;
    }

    /**
     * Validates addresses using USPS or another provider if available.
     * @param addresses List of Addresses to validate
     * @param provider Provider to use, default provider if left null or empty
     * @param usePunct Apply address punctuation to each result.
     * @return List<AddressResult>
     */
    public List<AddressResult> validate(List<Address> addresses, String provider, Boolean usePunct)
    {
        List<AddressResult> addressResults = new ArrayList<>();
        Timestamp startTime = TimeUtil.currentTimestamp();
        if (addresses != null && !addresses.isEmpty()) {
            logger.info(String.format("Performing USPS correction on %d addresses.", addresses.size()));

            if (this.providers.containsKey(provider)) {
                addressResults = this.providers.get(provider).validate(addresses);
                logger.info(String.format("USPS validate time: %d ms.", TimeUtil.getElapsedMs(startTime)));
                if (usePunct) {
                    for (AddressResult addressResult : addressResults) {
                        if (addressResult != null && addressResult.isValidated()) {
                            addressResult.setAddress(AddressUtil.addPunctuation(addressResult.getAddress()));
                        }
                    }
                }
            }
            else {
                for (int i = 0; i < addresses.size(); i++) {
                    addressResults.add(new AddressResult(this.getClass(), ResultStatus.ADDRESS_PROVIDER_NOT_SUPPORTED));
                }
            }
        }
        //log batch results here
        if (BATCH_LOGGING_ENABLED) {
            for (AddressResult addressResult: addressResults) {
                try {
                    if (addressResult.getAddress() != null) {
                        sqlAddressLogger.logAddress(addressResult.getAddress());
                    }
                }
                catch (Exception e) {
                    logger.warn("Failed to insert address result in the DB " + e.getMessage());
                }
            }
        }
        return addressResults;
    }

    /**
     * Use USPS for a city state lookup by default.
     */
    public AddressResult lookupCityState(Address address, String provider)
    {
        AddressResult addressResult = this.providers.get(provider).lookupCityState(address);
        logAddressResult(addressResult);
        return addressResult;
    }

    /**
     * Zipcode lookup is the same as a validate request with less output.
     */
    public AddressResult lookupZipcode(Address address, String provider)
    {
        return validate(address, provider, false);
    }


    private void logAddressResult(AddressResult addressResult) {
        if (SINGLE_LOGGING_ENABLED) {
            try {
                if (addressResult.getAddress() != null) {
                    sqlAddressLogger.logAddress(addressResult.getAddress());
                }
            }
            catch (Exception e) {
                logger.warn("Failed to insert address result in the DB " + e.getMessage());
            }
        }
    }

    public AddressService getDefaultProvider() {
        return defaultProvider;
    }

    public Map<String, AddressService> getProviders() {
        return providers;
    }

    public LinkedList<String> getDefaultFallBack() {
        return defaultFallBack;
    }
}