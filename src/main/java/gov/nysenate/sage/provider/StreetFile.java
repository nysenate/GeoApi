package gov.nysenate.sage.provider;

import gov.nysenate.sage.dao.provider.StreetFileDao;
import gov.nysenate.sage.model.address.DistrictedStreetRange;
import gov.nysenate.sage.model.address.DistrictedAddress;
import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.address.StreetAddress;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.service.street.StreetLookupService;
import gov.nysenate.sage.service.district.DistrictService;
import gov.nysenate.sage.service.district.ParallelDistrictService;
import gov.nysenate.sage.util.StreetAddressParser;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static gov.nysenate.sage.model.result.ResultStatus.*;
import static gov.nysenate.sage.service.district.DistrictServiceValidator.validateDistrictInfo;
import static gov.nysenate.sage.service.district.DistrictServiceValidator.validateInput;

/**
 * A street file provider implementation to resolve district codes.
   Street files are distributed by the Board of Elections on a county basis.
   These files contain address ranges with corresponding district code information.
   District information can be obtained quickly by matching a given address to an
   address range stored in the street file database.
 */
public class StreetFile implements DistrictService, StreetLookupService
{
    private Logger logger = Logger.getLogger(StreetFile.class);
    private StreetFileDao streetFileDao;

    public StreetFile() {
        this.streetFileDao = new StreetFileDao();
    }

    @Override
    public boolean requiresGeocode() { return false; }

    @Override
    public boolean providesMaps() { return false; }

    /** No map functionality */
    @Override
    public void fetchMaps(boolean fetch) {}

    @Override
    public List<DistrictedStreetRange> streetLookup(String zip5)
    {
        try {
            int intZip5 = Integer.parseInt(zip5);
            return streetFileDao.getDistrictStreetRangesByZip(intZip5);
        }
        catch (NumberFormatException ex) {
            logger.error("Zip5 must be parseable as an int!", ex);
        }
        return null;
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
        if (!validateInput(geocodedAddress, districtResult, false)) {
            return districtResult;
        }
        /** Parse the address */
        StreetAddress streetAddr = StreetAddressParser.parseAddress(geocodedAddress.getAddress());
        logger.trace("Streetfile lookup on " + streetAddr.toStringParsed());

        try {
            DistrictedAddress match = null;
            if (!streetAddr.isStreetEmpty()) {
                /** Try a House level match */
                match = streetFileDao.getDistAddressByHouse(streetAddr);
                /** Try a Street level match */
                if (match == null) {
                    match = streetFileDao.getDistAddressByStreet(streetAddr);
                }
            }
            /** Try a Zip5 level match */
            if (match == null) {
                match = streetFileDao.getDistAddressByZip(streetAddr);
            }

            /** Validate result and return error status */
            if (match == null) {
                districtResult.setStatusCode(NO_DISTRICT_RESULT);
            }
            else {
                validateDistrictInfo(match.getDistrictInfo(), reqTypes, districtResult);
                districtResult.setDistrictedAddress(match);
            }
        }
        catch (SQLException ex) {
            districtResult.setStatusCode(DATABASE_ERROR);
            logger.error(ex);
        }
        catch (Exception ex) {
            districtResult.setStatusCode(INTERNAL_ERROR);
            logger.error(ex);
        }
        logger.debug("Done with street lookup");
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

    @Override
    public Map<String, DistrictMap> nearbyDistricts(GeocodedAddress geocodedAddress, DistrictType districtType)
    {
        logger.warn("Nearby district search is not implemented using streetfiles!");
        return null;
    }

    @Override
    public Map<String, DistrictMap> nearbyDistricts(GeocodedAddress geocodedAddress, DistrictType districtType, int count)
    {
        logger.warn("Nearby district search is not implemented using streetfiles!");
        return null;
    }
}