package gov.nysenate.sage.dao.model;

import gov.nysenate.sage.BaseTests;
import gov.nysenate.sage.annotation.IntegrationTest;
import gov.nysenate.sage.dao.model.member.MemberDao;
import gov.nysenate.sage.model.district.DistrictMember;
import gov.nysenate.sage.model.district.DistrictType;
import gov.nysenate.sage.model.district.MemberInfo;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static org.junit.Assert.assertFalse;

@Category(IntegrationTest.class)
public class MemberDaoIT extends BaseTests {
    @Autowired
    private MemberDao memberDao;

    @Test
    public void getHouseMembersTest() {
        for (DistrictType type : DistrictType.values()) {
            Map<Long, DistrictMember> memberMap = memberDao.getMembers(type);
            if (memberMap == null) {
                continue;
            }
            assertFalse(memberMap.isEmpty());

            MemberInfo memberInfo = memberMap.values().stream().findFirst().get().info();
            assertFalse(StringUtils.isBlank(memberInfo.invertedName()));
            assertFalse(StringUtils.isBlank(memberInfo.url()));
        }
    }
}
