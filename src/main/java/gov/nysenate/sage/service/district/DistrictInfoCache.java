package gov.nysenate.sage.service.district;

import gov.nysenate.sage.dao.provider.DistrictInfoDao;
import gov.nysenate.sage.model.district.DistrictId;
import gov.nysenate.sage.model.district.DistrictInfo;
import gov.nysenate.sage.model.district.DistrictType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Caches the display names of every district, keyed by type and code.
 */
@Service
public class DistrictInfoCache extends DistrictIdCache<DistrictInfo> {
    @Autowired
    public DistrictInfoCache(DistrictInfoDao nameDao) {
        super(nameDao::getInfoMap);
    }

    public String getCode(DistrictType type, DistrictId id) {
        DistrictInfo info = getData(type, id);
        String code = info == null ? null : info.get("code");
        return StringUtils.isBlank(code) ? id.id() : code;
    }
}
