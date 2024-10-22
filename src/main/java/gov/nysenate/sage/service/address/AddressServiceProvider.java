package gov.nysenate.sage.service.address;

import gov.nysenate.sage.config.Environment;
import gov.nysenate.sage.dao.logger.address.SqlAddressLogger;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.provider.address.AddressService;
import gov.nysenate.sage.provider.address.AddressSource;
import gov.nysenate.sage.provider.address.USPSAIS;
import gov.nysenate.sage.provider.address.USPSAMS;
import gov.nysenate.sage.util.AddressUtil;
import gov.nysenate.sage.util.TimeUtil;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AddressServiceProvider implements AddressProvider {
    private static final Logger logger = LoggerFactory.getLogger(AddressServiceProvider.class);
    private final SqlAddressLogger sqlAddressLogger;
    private final AddressService defaultProvider;
    private final Map<AddressSource, AddressService> providers;
    private final boolean singleLoggingEnabled;
    private final boolean batchLoggingEnabled;

    @Autowired
    public AddressServiceProvider(SqlAddressLogger sqlAddressLogger, Environment env,
                                  USPSAMS uspsams, USPSAIS uspsais,
                                  @Value("${usps.default:usps}") String defaultUsps) {
        this.sqlAddressLogger = sqlAddressLogger;
        this.singleLoggingEnabled = env.isApiLoggingEnabled() && env.isDetailedLoggingEnabled();
        this.batchLoggingEnabled = env.isApiLoggingEnabled() && env.isBatchDetailedLoggingEnabled();
        this.providers = Map.of(AddressSource.AMS, uspsams, AddressSource.AIS, uspsais);
        this.defaultProvider = providers.get(AddressSource.fromString(defaultUsps));
    }

    /**
     * Validates an address using USPS or another provider if available.
     * @param address  Address to validate
     * @param providerStr Provide to use (if null, defaults will be used)
     * @param usePunct If true, validated address will have periods after abbreviations.
     * @return AddressResult
     */
    public AddressResult validate(Address address, String providerStr, boolean usePunct) {
        Timestamp startTime = TimeUtil.currentTimestamp();
        AddressResult addressResult = internalValidate(address, providerStr);
        if (addressResult.isValidated()) {
            logger.info("USPS validate time: {} ms", TimeUtil.getElapsedMs(startTime));
            // Apply punctuation to the address if requested
            if (usePunct) {
                addressResult.setAddress(AddressUtil.addPunctuation(addressResult.getAddress()));
            }
        }
        logAddressResult(addressResult);
        return addressResult;
    }

    private AddressResult internalValidate(Address address, String providerStr) {
        if (address == null) {
            return new AddressResult(null, ResultStatus.MISSING_ADDRESS);
        }
        if (!address.isEligibleForUSPS()) {
            return new AddressResult(null, ResultStatus.INSUFFICIENT_ADDRESS);
        }
        // Use provider if specified
        AddressService addressService;
        if (providerStr == null || providerStr.isEmpty()) {
            addressService = defaultProvider;
        }
        else {
            addressService = providers.get(AddressSource.fromString(providerStr));
        }
        if (addressService == null) {
            return new AddressResult(null, ResultStatus.ADDRESS_PROVIDER_NOT_SUPPORTED);
        }
        else {
            return addressService.validate(address);
        }
    }

    /**
     * Validates addresses using USPS or another provider if available.
     * @param addresses List of Addresses to validate
     * @param provider Provider to use, default provider if left null or empty
     * @param usePunct Apply address punctuation to each result.
     * @return List<AddressResult>
     */
    @Override
    public List<AddressResult> validate(List<Address> addresses, String provider, boolean usePunct) {
        if (CollectionUtils.isEmpty(addresses)) {
            return new ArrayList<>();
        }

        logger.info("Performing USPS correction on {} addresses.", addresses.size());
        Timestamp startTime = TimeUtil.currentTimestamp();

        AddressService providerService = providers.getOrDefault(AddressSource.fromString(provider), defaultProvider);
        List<AddressResult> addressResults = providerService.validate(addresses);
        logger.info("USPS validate time: {} ms.", TimeUtil.getElapsedMs(startTime));

        if (usePunct) {
            for (AddressResult addressResult : addressResults) {
                if (addressResult != null && addressResult.isValidated()) {
                    addressResult.setAddress(AddressUtil.addPunctuation(addressResult.getAddress()));
                }
            }
        }
        logAddressResults(addressResults);
        return addressResults;
    }

    /**
     * Use USPS for a city state lookup by default.
     */
    @Nonnull
    @Override
    public AddressResult lookupCityState(Address address, String providerName) {
        AddressService provider = providers.getOrDefault(AddressSource.fromString(providerName), defaultProvider);
        AddressResult addressResult = provider.lookupCityState(address);
        logAddressResult(addressResult);
        return addressResult;
    }

    @Override
    public List<AddressResult> lookupCityState(List<Address> addresses, String providerName) {
        AddressService provider = providers.getOrDefault(AddressSource.fromString(providerName), defaultProvider);
        List<AddressResult> addressResults = provider.lookupCityState(addresses);
        logAddressResults(addressResults);
        return addressResults;
    }

    /**
     * Zipcode lookup is the same as a validate request with less output.
     */
    public AddressResult lookupZipcode(Address address, String provider) {
        return validate(address, provider, false);
    }

    private void logAddressResult(AddressResult addressResult) {
        if (singleLoggingEnabled) {
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

    private void logAddressResults(List<AddressResult> addressResults) {
        if (batchLoggingEnabled) {
            for (AddressResult addressResult : addressResults) {
                try {
                    if (addressResult.getAddress() != null) {
                        sqlAddressLogger.logAddress(addressResult.getAddress());
                    }
                } catch (Exception e) {
                    logger.warn("Failed to insert address result in the DB " + e.getMessage());
                }
            }
        }
    }
}