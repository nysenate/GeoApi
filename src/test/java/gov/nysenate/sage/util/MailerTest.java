package gov.nysenate.sage.util;

import gov.nysenate.sage.TestBase;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MailerTest extends TestBase
{
    Mailer mailer;
    private static Logger logger = LogManager.getLogger(MailerTest.class);

    @Before
    public void setUp()
    {
        mailer = new Mailer();
    }

    @Test
    public void testSendMail()  throws Exception
    {
        logger.info(new Timestamp(new Date().getTime()).toString());
        mailer.sendMail("calabres@nysenate.gov", "IGNORE THIS (email testing)!", "...");
    }

}
