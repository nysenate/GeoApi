package gov.nysenate.sage.dao.provider;

import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.district.SingleDistrict;

public interface SingleDistrictService {
    SingleDistrict getSingleDistrict(DistrictType type, String code);
}
