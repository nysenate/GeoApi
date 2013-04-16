package gov.nysenate.sage.util;

import gov.nysenate.sage.TestBase;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.Date;

public class MailerTest extends TestBase
{
    Mailer mailer;

    @Before
    public void setUp()
    {
        mailer = new Mailer();
    }

    @Test
    public void testSendMail()  throws Exception
    {
        System.out.print(new Timestamp(new Date().getTime()).toString());
        //mailer.sendMail("ashislam858@gmail.com", "IGNORE THIS (email testing)!", "Meow");
    }

}
