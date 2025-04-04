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
public class AssemblyDaoIT extends BaseTests {
    @Autowired
    private MemberDao memberDao;

    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void getAssembliesTest() {
        List<DistrictMember> assemblyList = memberDao.getMembers(DistrictType.ASSEMBLY);
        assertNotNull(assemblyList);
        assertEquals(150, assemblyList.size());

        DistrictMember a = assemblyList.get(0);
        assertEquals(DistrictType.ASSEMBLY, a.districtType());
        assertNotNull(a.memberName());
        assertNotNull(a.memberUrl());
        assertFalse(a.memberName().isEmpty());
        assertFalse(a.memberUrl().isEmpty());
        assertTrue(a.district() > 0);
    }
}
