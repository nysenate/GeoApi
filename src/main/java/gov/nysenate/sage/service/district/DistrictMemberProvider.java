package gov.nysenate.sage.service.district;

import gov.nysenate.sage.dao.model.member.MemberDao;
import gov.nysenate.sage.dao.model.senate.SqlSenateDao;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.MapResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Typically when the district service providers return a DistrictInfo, only the district codes
 * and maps are provided. This class provides methods to populate the remaining data which includes
 * the district members and the senator information. Since this information is not always required, this
 * functionality should be invoked through a controller as opposed to the provider implementations.
 */
@Component
public class DistrictMemberProvider implements SageDistrictMemberProvider {
    private final SqlSenateDao sqlSenateDao;
    private final MemberDao memberDao;

    @Autowired
    public DistrictMemberProvider(SqlSenateDao sqlSenateDao, MemberDao memberDao) {
        this.sqlSenateDao = sqlSenateDao;
        this.memberDao = memberDao;
    }

    /**
     * Adds the senator, congressional, and assembly member data to the map result.
     */
    public void assignDistrictMembers(MapResult mapResult) {
        if (mapResult == null || !mapResult.isSuccess()) {
            return;
        }
        for (DistrictMap map : mapResult.getDistrictMaps()) {
            if (map.getDistrictType() == DistrictType.TOWN_CITY) {
                continue;
            }
            int code = Integer.parseInt(map.getDistrictCode());
            switch (map.getDistrictType()) {
                case SENATE -> map.setSenator(sqlSenateDao.getSenatorByDistrict(code));
                case ASSEMBLY, CONGRESSIONAL -> map.setMember(memberDao.getMemberByDistrict(map.getDistrictType(), code));
            }
        }
    }
}
