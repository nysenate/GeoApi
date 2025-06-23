package gov.nysenate.sage.service.address;

import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.Zip5;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.model.result.CityStateResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.provider.address.AddressDao;
import gov.nysenate.sage.provider.address.AddressSource;
import gov.nysenate.sage.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AddressService {
    private static final Logger logger = LoggerFactory.getLogger(AddressService.class);
    private final Map<AddressSource, AddressDao> providerMap = new HashMap<>();

    @Autowired
    public AddressService(List<AddressDao> addressDaos, @Value("${usps.default:AMS}") String defaultProvider) {
        for (AddressDao addressDao : addressDaos) {
            providerMap.put(addressDao.source(), addressDao);
        }
        AddressSource defaultSource = AddressSource.valueOf(defaultProvider.toUpperCase());
        providerMap.put(null, providerMap.get(defaultSource));
    }

    /**
     * Validates an address using USPS or another provider if available.
     * @param address Address to validate
     * @param source Source to use
=     * @return AddressResult
     */
    public AddressResult validate(Address address, AddressSource source) {
        return internalValidate(address, source);
    }

    public Address validateOrDefault(Address address) {
        return getOrDefault(validate(address, null), address);
    }

    private AddressResult internalValidate(Address address, AddressSource source) {
        if (address == null) {
            return new AddressResult(null, ResultStatus.INVALID_ADDRESS);
        }
        if (!address.isValid()) {
            return new AddressResult(null, ResultStatus.INSUFFICIENT_ADDRESS);
        }
        AddressResult result = providerMap.get(source).validate(address);
        if (result == null) {
            return new AddressResult(source, ResultStatus.NO_ADDRESS_VALIDATE_RESULT);
        }
        return result;
    }

    /**
     * Validates addresses using USPS or another provider if available.
     * @param addresses List of Addresses to validate
     * @param source Source to use
     * @return List<AddressResult>
     */
    public List<AddressResult> validate(List<Address> addresses, AddressSource source) {
        if (addresses.isEmpty()) {
            return List.of();
        }

        logger.info("Performing USPS correction on {} addresses.", addresses.size());
        Timestamp startTime = TimeUtil.currentTimestamp();

        List<AddressResult> addressResults = providerMap.get(source).validate(addresses);
        if (addressResults == null) {
            addressResults = new ArrayList<>();
            for (int i = 0; i < addresses.size(); i++) {
                addressResults.add(new AddressResult(source, ResultStatus.NO_ADDRESS_VALIDATE_RESULT));
            }
        }
        logger.info("USPS validate time: {} ms.", TimeUtil.getElapsedMs(startTime));
        return addressResults;
    }

    public List<Address> validateOrDefault(List<Address> addresses) {
        List<Address> finalAddresses = new ArrayList<>();
        List<AddressResult> results = validate(addresses, null);
        for (int i = 0; i < addresses.size(); i++) {
            finalAddresses.add(getOrDefault(results.get(i), addresses.get(i)));
        }
        return finalAddresses;
    }

    @Nonnull
    public CityStateResult lookupCityState(Zip5 zip5, AddressSource source) {
        if (zip5 == null) {
            return new CityStateResult(null, ResultStatus.INVALID_ADDRESS);
        }
        CityStateResult result = providerMap.get(source).lookupCityState(zip5);
        if (result == null) {
            return new CityStateResult(source, ResultStatus.NO_ADDRESS_VALIDATE_RESULT);
        }
        return result;
    }

    public List<CityStateResult> lookupCityState(List<Zip5> zips, AddressSource source) {
        List<CityStateResult> results = providerMap.get(source).lookupCityStates(zips);
        if (results == null) {
            return List.of(new CityStateResult(source, ResultStatus.NO_ADDRESS_VALIDATE_RESULT));
        }
        return results;
    }

    private static Address getOrDefault(AddressResult result, Address defaultAddress) {
        return result != null && result.isSuccess() ? result.getAddress() : defaultAddress;
    }
}
