package gov.nysenate.sage.provider;

import gov.nysenate.sage.dao.provider.StreetFileDao;
import gov.nysenate.sage.model.address.DistrictedAddress;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.DistrictResult;
import static gov.nysenate.sage.model.result.ResultStatus.*;
import gov.nysenate.sage.service.district.DistrictService;
import gov.nysenate.sage.service.district.ParallelDistrictService;
import gov.nysenate.sage.util.AddressParser;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.List;

public class StreetFile implements DistrictService
{
    private Logger logger = Logger.getLogger(StreetFile.class);
    private StreetFileDao streetFileDao;

    public StreetFile() {
        this.streetFileDao = new StreetFileDao();
    }

    @Override
    public DistrictResult assignDistricts(GeocodedAddress geocodedAddress)
    {
        return assignDistricts(geocodedAddress, DistrictType.getStateBasedTypes());
    }

    @Override
    public DistrictResult assignDistricts(GeocodedAddress geocodedAddress, List<DistrictType> reqTypes)
    {
        DistrictResult districtResult = new DistrictResult(this.getClass());

        /** Validate input */
        if (geocodedAddress == null || geocodedAddress.getAddress() == null) {
            districtResult.setStatusCode(MISSING_INPUT_PARAMS);
            return districtResult;
        }
        else if (geocodedAddress.getAddress().isEmpty()) {
            districtResult.setStatusCode(INSUFFICIENT_ADDRESS);
            return districtResult;
        }

        /** Parse the address */
        StreetAddress streetAddr = AddressParser.parseAddress(geocodedAddress.getAddress().toString());

        try {
            /** Try a House level match */
            DistrictedAddress match = streetFileDao.getDistAddressByHouse(streetAddr);

            /** Try a Street level match */
            if (match == null) {
                match = streetFileDao.getDistAddressByStreet(streetAddr);
            }

            /** Try a Zip5 level match */
            if (match == null) {
                match = streetFileDao.getDistAddressByStreet(streetAddr);
            }

            if (match != null) {
                districtResult.setDistrictedAddress(match);
                /** Set partial district assignment if not all were found */
                if (reqTypes != null && !districtResult.getAssignedDistricts().containsAll(reqTypes)) {
                    districtResult.setStatusCode(PARTIAL_DISTRICT_RESULT);
                }
            }
            else {
                /** No Match */
                districtResult.setStatusCode(NO_DISTRICT_RESULT);
            }
        }
        catch (SQLException ex) {
            districtResult.setStatusCode(DATABASE_ERROR);
        }
        catch (Exception ex) {
            districtResult.setStatusCode(INTERNAL_ERROR);
        }

        return districtResult;
    }

    @Override
    public List<DistrictResult> assignDistricts(List<GeocodedAddress> geocodedAddresses)
    {
        return ParallelDistrictService.assignDistricts(this, geocodedAddresses, DistrictType.getStateBasedTypes());
    }

    @Override
    public List<DistrictResult> assignDistricts(List<GeocodedAddress> geocodedAddresses, List<DistrictType> types)
    {
        return ParallelDistrictService.assignDistricts(this, geocodedAddresses, types);
    }
}
