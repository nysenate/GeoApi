package gov.nysenate.sage.service.district;

import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.DistrictResult;
import org.apache.log4j.Logger;
import java.util.List;

/**
 * DistrictService is used to assign district information to addresses and may or may not require
 * geo-coordinate information.
 */
public interface DistrictService
{
    public DistrictResult assignDistricts(GeocodedAddress geocodedAddress);
    public DistrictResult assignDistricts(GeocodedAddress geocodedAddress, List<DistrictType> types);
    public List<DistrictResult> assignDistricts(List<GeocodedAddress> geocodedAddresses);
    public List<DistrictResult> assignDistricts(List<GeocodedAddress> geocodedAddresses, List<DistrictType> types);
}
