package gov.nysenate.sage.provider.address;

import gov.nysenate.sage.dao.provider.usps.HttpUSPSAMSDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.model.result.ResultStatus;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Provider implementation for the USPS AMS WebService.
 */
@Service
public class USPSAMS implements AddressService
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private HttpUSPSAMSDao httpUSPSAMSDao;

    @Autowired
    public USPSAMS(HttpUSPSAMSDao httpUSPSAMSDao) {
        this.httpUSPSAMSDao = httpUSPSAMSDao;
    }

    /** {@inheritDoc} */
    @Override
    public AddressResult validate(Address address) 
    {
        if (address != null && !address.isEmpty()) {
            AddressResult result = httpUSPSAMSDao.getValidatedAddressResult(address);
            if (result != null) {
                result.setSource(this.getClass());
                return result;
            }
        }
        return new AddressResult(this.getClass(), ResultStatus.NO_ADDRESS_VALIDATE_RESULT);
    }

    /** {@inheritDoc} */
    @Override
    public List<AddressResult> validate(List<Address> addresses)
    {
        if (addresses == null) return null;

        if (!addresses.isEmpty()) {
            List<AddressResult> results = httpUSPSAMSDao.getValidatedAddressResults(addresses);
            if (results != null && results.size() == addresses.size()) {
                for (AddressResult result : results) {
                    result.setSource(this.getClass());
                }
                return results;
            }
            else {
                List<AddressResult> errorResults = new ArrayList<>();
                for (int i = 0; i < addresses.size(); i++) {
                    errorResults.add(new AddressResult(this.getClass(), ResultStatus.NO_ADDRESS_VALIDATE_RESULT));
                }
                return errorResults;
            }
        }

        return Arrays.asList(new AddressResult(this.getClass(), ResultStatus.MISSING_ADDRESS));
    }

    /** {@inheritDoc} */
    @Override
    public AddressResult lookupCityState(Address address) 
    {
        if(address != null && !address.isEmpty() && address.getZip5() != null)
        {
            AddressResult result = httpUSPSAMSDao.getCityStateResult(address);
            if (result != null) {
                result.setSource(this.getClass());
                return result;
            }
        }
        return new AddressResult(this.getClass(), ResultStatus.NO_ADDRESS_VALIDATE_RESULT);
    }

    /** {@inheritDoc} */
    @Override
    public List<AddressResult> lookupCityState(List<Address> addresses)
    {
        if(addresses != null && !addresses.isEmpty())
        {
            List<AddressResult> results = httpUSPSAMSDao.getCityStateResults(addresses);
            if (results != null && results.size() == addresses.size()) {
                return results;
            }
            return Arrays.asList(new AddressResult(this.getClass(), ResultStatus.NO_ADDRESS_VALIDATE_RESULT));
        }
        return Arrays.asList(new AddressResult(this.getClass(), ResultStatus.MISSING_ADDRESS));
    }

    /** {@inheritDoc} */
    @Override
    public AddressResult lookupZipCode(Address address) 
    {
        return validate(address);
    }

    /** {@inheritDoc} */
    @Override
    public List<AddressResult> lookupZipCode(List<Address> addresses)
    {
        return validate(addresses);
    }
}