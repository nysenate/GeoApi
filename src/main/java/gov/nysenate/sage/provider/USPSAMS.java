package gov.nysenate.sage.provider;

import gov.nysenate.sage.dao.provider.USPSAMSDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.model.result.ResultStatus;
import gov.nysenate.sage.service.address.AddressService;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.List;

public class USPSAMS implements AddressService
{
    private final Logger logger = Logger.getLogger(this.getClass());
    private USPSAMSDao uspsAmsDao = new USPSAMSDao();

    /**
     * Performs USPS validation for a single address using the AMS Web Service.
     * @param address Address to validate
     * @return AddressResult
     */
    @Override
    public AddressResult validate(Address address) 
    {
        if (address != null && !address.isEmpty()) {
            AddressResult result = uspsAmsDao.getValidatedAddressResult(address);
            if (result != null) {
                result.setSource(this.getClass());
                return result;
            }
        }
        return new AddressResult(this.getClass(), ResultStatus.NO_ADDRESS_VALIDATE_RESULT);
    }

    /**
     * Performs batch USPS validation using the AMS Web Service.
     * @param addresses Addresses to validate
     * @return List<AddressResult>
     */
    @Override
    public List<AddressResult> validate(List<Address> addresses)
    {
        if (addresses != null && !addresses.isEmpty()) {
            List<AddressResult> results = uspsAmsDao.getValidatedAddressResults(addresses);
            if (results != null && results.size() == addresses.size()) {
                return results;
            }
            return Arrays.asList(new AddressResult(this.getClass(), ResultStatus.NO_ADDRESS_VALIDATE_RESULT));
        }
        return Arrays.asList(new AddressResult(this.getClass(), ResultStatus.MISSING_ADDRESS));
    }

    @Override
    public AddressResult lookupCityState(Address address) 
    {
        if(address != null && !address.isEmpty() && address.getZip5() != null)
        {
             AddressResult result = uspsAmsDao.getCityStateResult(address);
            if (result != null) {
                result.setSource(this.getClass());
                return result;
            }
        }
        return new AddressResult(this.getClass(), ResultStatus.NO_ADDRESS_VALIDATE_RESULT);
    }

    @Override
    public List<AddressResult> lookupCityState(List<Address> addresses)
    {
        if(addresses != null && !addresses.isEmpty())
        {
            List<AddressResult> results = uspsAmsDao.getCityStateResults(addresses);
            if (results != null && results.size() == addresses.size()) {
                return results;
            }
            return Arrays.asList(new AddressResult(this.getClass(), ResultStatus.NO_ADDRESS_VALIDATE_RESULT));
        }
        return Arrays.asList(new AddressResult(this.getClass(), ResultStatus.MISSING_ADDRESS));
    }

    /**
     * Proxies to validate since there is no specific API call available.
     * @param address Address to lookup
     * @return AddressResult
     */
    @Override
    public AddressResult lookupZipCode(Address address) 
    {
        return validate(address);
    }

    /**
     * Proxies to validate since there is no specific API call available.
     * @param addresses Addresses to lookup
     * @return List<AddressResult>
     */
    @Override
    public List<AddressResult> lookupZipCode(List<Address> addresses)
    {
        return validate(addresses);
    }
}