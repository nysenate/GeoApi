package gov.nysenate.sage.util;

import gov.nysenate.sage.BaseTests;
import gov.nysenate.sage.annotation.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

@Category(IntegrationTest.class)
public class MailerIT extends BaseTests {
    @Autowired
    private Mailer mailer;

    @Test
    public void testSendMail()  throws Exception {
        mailer.sendMail("sage@nysenate.gov", "SAGE Deployment: Mailer Integration Test",
                "This is a test message being generated during SAGE deployment. It utilizes the SMTP parameters in app.properties to configure the SMTP server.");
    }
}
