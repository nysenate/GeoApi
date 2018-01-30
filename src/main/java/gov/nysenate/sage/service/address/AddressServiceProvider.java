package gov.nysenate.sage.service.address;

import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.dao.logger.AddressLogger;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.provider.USPSAIS;
import gov.nysenate.sage.provider.USPSAMS;
import gov.nysenate.sage.service.base.ServiceProviders;
import gov.nysenate.sage.util.AddressUtil;
import gov.nysenate.sage.util.TimeUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AddressServiceProvider extends ServiceProviders<AddressService>
{
    private static Logger logger = LogManager.getLogger(AddressServiceProvider.class);
    private AddressLogger addressLogger;
    private Environment env;

    private boolean API_LOGGING_ENABLED = false;
    private boolean SINGLE_LOGGING_ENABLED = false;
    private boolean BATCH_LOGGING_ENABLED = false;

    @Autowired
    public AddressServiceProvider(AddressLogger addressLogger, Environment env) {
        this.addressLogger = addressLogger;
        this.env = env;
        API_LOGGING_ENABLED = env.isApiLoggingEnabled();
        SINGLE_LOGGING_ENABLED = API_LOGGING_ENABLED && env.isDetailedLoggingEnabled();
        BATCH_LOGGING_ENABLED = API_LOGGING_ENABLED && env.isBatchDetailedLoggingEnabled();

        String defaultUspsProvider = env.getUspsDefault();
        Map<String, Class<? extends AddressService>> addressProviders = new HashMap<>();
        addressProviders.put("usps", USPSAMS.class);
        addressProviders.put("uspsais", USPSAIS.class);
        for (String key : addressProviders.keySet()) {
            registerProvider(key, addressProviders.get(key));
        }
        setDefaultProvider(defaultUspsProvider);
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
            if (this.isRegistered(provider)) {
                addressResult = this.getInstance(provider).validate(address);
            }
            else {
                return new AddressResult(this.getClass(), ResultStatus.ADDRESS_PROVIDER_NOT_SUPPORTED);
            }
        }
        /** Use USPS if address is eligible */
        else if (address.isEligibleForUSPS()) {
            addressResult = this.getInstance().validate(address);
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

            if (provider == null || provider.isEmpty()) {
                provider = this.defaultProvider;
            }
            if (this.isRegistered(provider)) {
                addressResults = this.getInstance(provider).validate(addresses);
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
                        addressLogger.logAddress(addressResult.getAddress());
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
        if (provider == null || provider.isEmpty()) {
            provider = this.defaultProvider;
        }
        AddressResult addressResult = this.getInstance(provider).lookupCityState(address);
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
                    addressLogger.logAddress(addressResult.getAddress());
                }
            }
            catch (Exception e) {
                logger.warn("Failed to insert address result in the DB " + e.getMessage());
            }
        }
    }
}