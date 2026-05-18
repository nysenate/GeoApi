package gov.nysenate.sage.dao.model;

import gov.nysenate.sage.BaseTests;
import gov.nysenate.sage.annotation.IntegrationTest;
import gov.nysenate.sage.dao.model.member.MemberDao;
import gov.nysenate.sage.model.district.DistrictMember;
import gov.nysenate.sage.model.district.DistrictType;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class CongressionalDaoIT extends BaseTests {

    @Autowired
    private MemberDao memberDao;

    @Test
    public void getCongressionalsTest() {
        List<DistrictMember> congressionalList = memberDao.getMembers(DistrictType.CONGRESSIONAL);
        assertNotNull(congressionalList);

        DistrictMember c = congressionalList.getFirst();
        assertEquals(DistrictType.CONGRESSIONAL, c.districtType());
        assertNotNull(c.memberName());
        assertNotNull(c.memberUrl());
        assertFalse(c.memberName().isEmpty());
        assertFalse(c.memberUrl().isEmpty());
        assertTrue(c.district() > 0);
    }
}

