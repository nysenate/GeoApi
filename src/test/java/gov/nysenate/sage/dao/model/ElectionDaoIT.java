package gov.nysenate.sage.dao.model;

import gov.nysenate.sage.BaseTests;
import gov.nysenate.sage.annotation.IntegrationTest;
import gov.nysenate.sage.dao.model.election.ElectionDao;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

@Category(IntegrationTest.class)
public class ElectionDaoIT extends BaseTests {
    @Autowired
    private ElectionDao dao;

    @Test
    public void electionTest() {
        dao.rebuildMaps();
    }
}
