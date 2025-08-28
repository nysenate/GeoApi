package gov.nysenate.sage.dao.provider;

import gov.nysenate.sage.model.district.DistrictType;

public interface DistrictNameDao {
    String getDistrictName(DistrictType type, String code);
}
