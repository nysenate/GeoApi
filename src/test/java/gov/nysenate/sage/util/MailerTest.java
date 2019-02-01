package gov.nysenate.sage.util;

import gov.nysenate.sage.BaseTests;
import gov.nysenate.sage.annotation.IntegrationTest;
import gov.nysenate.sage.config.DatabaseConfig;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Date;

@Category(IntegrationTest.class)
public class MailerTest extends BaseTests {

    @Autowired
    Mailer mailer;

    @Test
    @Transactional(value = DatabaseConfig.geoApiTxManager)
    public void testSendMail()  throws Exception
    {
        System.out.print(new Timestamp(new Date().getTime()).toString());
        mailer.sendMail("calabres@nysenate.gov", "IGNORE THIS (email testing)!", "...");
    }

}
