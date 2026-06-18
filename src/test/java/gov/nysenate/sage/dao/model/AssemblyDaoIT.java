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
public class AssemblyDaoIT extends BaseTests {
    @Autowired
    private MemberDao memberDao;

    @Test
    public void getAssembliesTest() {
        Map<Long, DistrictMember> assemblyMap = memberDao.getMembers(DistrictType.ASSEMBLY);
        assertNotNull(assemblyMap);
        assertEquals(150, assemblyMap.size());

        DistrictMember a = assemblyMap.get(1L);
        assertNotNull(a.info().invertedName());
        assertNotNull(a.info().url());
        assertFalse(a.info().invertedName().isEmpty());
        assertFalse(a.info().url().isEmpty());
    }
}
