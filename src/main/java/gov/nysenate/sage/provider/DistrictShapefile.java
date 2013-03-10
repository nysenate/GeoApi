package gov.nysenate.sage.provider;

import gov.nysenate.sage.model.address.GeocodedAddress;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.DistrictResult;
import gov.nysenate.sage.service.district.DistrictService;

import java.util.List;

public class DistrictShapefile implements DistrictService
{

    @Override
    public DistrictResult assignDistricts(GeocodedAddress geocodedAddress) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public DistrictResult assignDistricts(GeocodedAddress geocodedAddress, List<DistrictType> types)
    {
        return null;
    }

    @Override
    public List<DistrictResult> assignDistricts(List<GeocodedAddress> geocodedAddresses) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<DistrictResult> assignDistricts(List<GeocodedAddress> geocodedAddresses, List<DistrictType> types) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
