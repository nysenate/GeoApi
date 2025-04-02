package gov.nysenate.sage.dao.model;

import gov.nysenate.sage.BaseTests;
import gov.nysenate.sage.annotation.IntegrationTest;
import gov.nysenate.sage.config.DatabaseConfig;
import gov.nysenate.sage.dao.model.member.MemberDao;
import gov.nysenate.sage.model.district.DistrictMember;
import gov.nysenate.sage.model.district.DistrictType;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class CongressionalDaoIT extends BaseTests {
    private static final int TOTAL_CONGRESSIONALS = 26;

    @Autowired
    private MemberDao memberDao;

    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void getCongressionalsTest() {
        List<DistrictMember> congressionalList = memberDao.getMembers(DistrictType.CONGRESSIONAL);
        assertNotNull(congressionalList);
        assertEquals(TOTAL_CONGRESSIONALS, congressionalList.size());

        DistrictMember c = congressionalList.get(0);
        assertEquals(DistrictType.CONGRESSIONAL, c.districtType());
        assertNotNull(c.memberName());
        assertNotNull(c.memberUrl());
        assertFalse(c.memberName().isEmpty());
        assertFalse(c.memberUrl().isEmpty());
        assertTrue(c.district() > 0);
    }

    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void insertAndDeleteTest() {
        var congressional = new DistrictMember(DistrictType.CONGRESSIONAL, 200,"TEST TEST","NY.GOV.CONGRESS.TEST");
        memberDao.insertDistrictMember(congressional);
        memberDao.deleteDistrictMember(DistrictType.CONGRESSIONAL, 200);
    }


    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void getMemberByInvalidDistrictTest() {
        assertNull(memberDao.getMemberByDistrict(DistrictType.CONGRESSIONAL, 0));
    }
}

