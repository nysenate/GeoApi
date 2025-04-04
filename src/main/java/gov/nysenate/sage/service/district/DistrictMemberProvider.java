package gov.nysenate.sage.service.district;

import com.google.common.collect.ImmutableMap;
import gov.nysenate.sage.dao.model.member.MemberDao;
import gov.nysenate.sage.dao.model.senate.SqlSenateDao;
import gov.nysenate.sage.model.district.DistrictMap;
import gov.nysenate.sage.model.district.DistrictMember;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.result.MapResult;
import gov.nysenate.services.model.Senator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

/**
 * Typically when the district service providers return a DistrictInfo, only the district codes
 * and maps are provided. This class provides methods to populate the remaining data which includes
 * the district members and the senator information. Since this information is not always required, this
 * functionality should be invoked through a controller as opposed to the provider implementations.
 */
@Component
public class DistrictMemberProvider {
    // TODO: cache data, similar method to add to DistrictResponse
    private final SqlSenateDao sqlSenateDao;
    private final MemberDao memberDao;
    private ImmutableMap<Integer, Senator> senatorCache;
    private ImmutableMap<Integer, DistrictMember> assemblyCache, congressionalCache;

    @Autowired
    public DistrictMemberProvider(SqlSenateDao sqlSenateDao, MemberDao memberDao) {
        this.sqlSenateDao = sqlSenateDao;
        this.memberDao = memberDao;
        recreateCaches();
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
                case SENATE -> map.setSenator(senatorCache.get(code));
                case ASSEMBLY -> map.setMember(assemblyCache.get(code));
                case CONGRESSIONAL ->  map.setMember(congressionalCache.get(code));
            }
        }
    }

    public void recreateCaches() {
        this.senatorCache = getCache(sqlSenateDao.getSenators(), sen -> sen.getDistrict().getNumber());
        this.assemblyCache = getCache(memberDao.getMembers(DistrictType.ASSEMBLY), DistrictMember::district);
        this.congressionalCache = getCache(memberDao.getMembers(DistrictType.CONGRESSIONAL), DistrictMember::district);
    }

    private static <T> ImmutableMap<Integer, T> getCache(List<T> members, Function<T, Integer> getDistrict) {
        var tempMap = new HashMap<Integer, T>();
        for (T member : members) {
            tempMap.put(getDistrict.apply(member), member);
        }
        return  ImmutableMap.copyOf(tempMap);
    }

}
