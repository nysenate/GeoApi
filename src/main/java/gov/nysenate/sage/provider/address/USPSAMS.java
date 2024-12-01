package gov.nysenate.sage.provider.address;

import gov.nysenate.sage.dao.provider.usps.HttpUSPSAMSDao;
import gov.nysenate.sage.model.address.Address;
import gov.nysenate.sage.model.address.Zip5;
import gov.nysenate.sage.model.result.AddressResult;
import gov.nysenate.sage.model.result.CityStateResult;
import gov.nysenate.sage.model.result.ResultStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Provider implementation for the USPS AMS WebService.
 */
@Service
public class USPSAMS implements AddressService {
    private final HttpUSPSAMSDao httpUSPSAMSDao;

    @Autowired
    public USPSAMS(HttpUSPSAMSDao httpUSPSAMSDao) {
        this.httpUSPSAMSDao = httpUSPSAMSDao;
    }

    @Override
    public AddressSource source() {
        return AddressSource.AMS;
    }

    /** {@inheritDoc} */
    @Nonnull
    @Override
    public AddressResult validate(Address address) {
        if (address != null && address.isValid()) {
            AddressResult result = httpUSPSAMSDao.getValidatedAddressResult(address);
            if (result != null) {
                return result;
            }
        }
        return new AddressResult(AddressSource.AMS, ResultStatus.NO_ADDRESS_VALIDATE_RESULT);
    }

    /** {@inheritDoc} */
    @Override
    public List<AddressResult> validate(List<Address> addresses) {
        if (addresses == null) return null;

        if (!addresses.isEmpty()) {
            List<AddressResult> results = httpUSPSAMSDao.getValidatedAddressResults(addresses);
            if (results != null && results.size() == addresses.size()) {
                for (AddressResult result : results) {
                    if (result.getAddress() != null) {
                        result.getAddress().setUspsValidated( result.isValidated() );
                    }
                }
                return results;
            }
            // TODO Determine if this else block should stay. It could be unreachable with current code flow changes
            else {
                List<AddressResult> errorResults = new ArrayList<>();
                for (int i = 0; i < addresses.size(); i++) {
                    errorResults.add(new AddressResult(AddressSource.AMS, ResultStatus.NO_ADDRESS_VALIDATE_RESULT));
                }
                return errorResults;
            }
        }
        return List.of(new AddressResult(AddressSource.AMS, ResultStatus.MISSING_ADDRESS));
    }

    /** {@inheritDoc} */
    @Nonnull
    @Override
    public CityStateResult lookupCityState(Zip5 zip5) {
        if (zip5 != null && !zip5.isMissing()) {
            CityStateResult result = httpUSPSAMSDao.getCityStateResult(zip5);
            if (result != null) {
                return result;
            }
        }
        return new CityStateResult(AddressSource.AMS, ResultStatus.NO_ADDRESS_VALIDATE_RESULT);
    }

    /** {@inheritDoc} */
    @Override
    public List<CityStateResult> lookupCityState(List<Zip5> zips) {
        if (zips != null && !zips.isEmpty()) {
            List<CityStateResult> results = httpUSPSAMSDao.getCityStateResults(zips);
            if (results != null && results.size() == zips.size()) {
                return results;
            }
            return List.of(new CityStateResult(AddressSource.AMS, ResultStatus.NO_ADDRESS_VALIDATE_RESULT));
        }
        return List.of(new CityStateResult(AddressSource.AMS, ResultStatus.MISSING_ADDRESS));
    }

}