package gov.nysenate.sage.provider;

import gov.nysenate.sage.dao.provider.StreetFileDao;
import gov.nysenate.sage.model.address.DistrictedAddress;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.service.district.DistrictService;
import gov.nysenate.sage.service.district.ParallelDistrictService;
import gov.nysenate.sage.service.district.DistrictServiceValidator;
import gov.nysenate.sage.util.AddressParser;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.List;

import static gov.nysenate.sage.model.result.ResultStatus.*;

/**
 * A street file provider implementation to resolve district codes.
   Street files are distributed by the Board of Elections on a county basis.
   These files contain address ranges with corresponding district code information.
   District information can be obtained quickly by matching a given address to an
   address range stored in the street file database.
 */
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
        if (!DistrictServiceValidator.validateInput(geocodedAddress, districtResult, false)) {
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
                match = streetFileDao.getDistAddressByZip(streetAddr);
            }

            if (match != null) {
                if (DistrictServiceValidator.validateDistrictInfo(match.getDistrictInfo(), reqTypes, districtResult)) {
                    districtResult.setDistrictedAddress(match);
                }
                else {
                    districtResult.setStatusCode(NO_DISTRICT_RESULT);
                }
            }
            else {
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
