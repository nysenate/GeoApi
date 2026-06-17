package gov.nysenate.sage.dao.model;

import gov.nysenate.sage.BaseTests;
import gov.nysenate.sage.annotation.IntegrationTest;
import gov.nysenate.sage.dao.model.member.MemberDao;
import gov.nysenate.sage.model.district.DistrictMember;
import gov.nysenate.sage.model.district.DistrictType;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class CongressionalDaoIT extends BaseTests {

    @Autowired
    private MemberDao memberDao;

    @Test
    public void getCongressionalsTest() {
        Map<Long, DistrictMember> congressionalMap = memberDao.getMembers(DistrictType.CONGRESSIONAL);
        assertNotNull(congressionalMap);

        DistrictMember c = congressionalMap.get(14L);
        assertNotNull(c.info().name());
        assertNotNull(c.info().url());
        assertFalse(c.info().name().isEmpty());
        assertFalse(c.info().url().isEmpty());
    }
}

