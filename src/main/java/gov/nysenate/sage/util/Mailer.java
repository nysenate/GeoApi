package gov.nysenate.sage.util;

import gov.nysenate.sage.factory.ApplicationFactory;
import org.apache.log4j.Logger;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Mailer
{
    private Logger logger = Logger.getLogger(Mailer.class);
    private Config config = ApplicationFactory.getConfig();
    private String SMTP_HOST_NAME;
    private String SMTP_DEBUG;
    private String SMTP_ACTIVE;
    private String SMTP_PORT;
    private String SMTP_ACCOUNT_USER;
    private String SMTP_ACCOUNT_PASS;
    private String SMTP_ADMIN;
    private String SMTP_TLS_ENABLE;
    private String SMTP_SSL_ENABLE;
    private String SMTP_CONTEXT;

    public Mailer()
    {
        SMTP_HOST_NAME = config.getValue("smtp.host");
        SMTP_DEBUG = config.getValue("smtp.debug");
        SMTP_ACTIVE = config.getValue("smtp.active");
        SMTP_PORT = config.getValue("smtp.port");
        SMTP_ACCOUNT_USER = config.getValue("smtp.user");
        SMTP_ACCOUNT_PASS = config.getValue("smtp.pass");
        SMTP_ADMIN = config.getValue("smtp.admin");
        SMTP_TLS_ENABLE = config.getValue("smtp.tls.enable");
        SMTP_SSL_ENABLE = config.getValue("smtp.ssl.enable");
        SMTP_CONTEXT = config.getValue("smtp.context");
    }

    public String getContext() {
        return SMTP_CONTEXT;
    }

    public String getAdminEmail() {
        return SMTP_ADMIN;
    }

    public void sendMail(String to, String subject, String message) throws Exception
    {
        sendMail(to, subject, message, SMTP_ACCOUNT_USER, "SAGE");
    }

    public void sendMail(String to, String subject, String message, String from, String fromDisplay) throws Exception
    {
	    if (!SMTP_ACTIVE.equals("true")) return;

		Properties props = new Properties();
		props.put("mail.smtp.host", SMTP_HOST_NAME);
		props.put("mail.smtp.auth", "true");
		props.put("mail.debug", SMTP_DEBUG);
		props.put("mail.smtp.port", SMTP_PORT);
		props.put("mail.smtp.starttls.enable",SMTP_TLS_ENABLE);
		props.put("mail.smtp.socketFactory.port", SMTP_PORT);
		props.put("mail.smtp.socketFactory.fallback", "false");
		props.put("mail.smtp.ssl.enable",SMTP_SSL_ENABLE);

		Session session = Session.getDefaultInstance(props,	new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_ACCOUNT_USER, SMTP_ACCOUNT_PASS);
            }
        });

		Message msg = new MimeMessage(session);
		InternetAddress addressFrom = new InternetAddress(from);
		addressFrom.setPersonal(fromDisplay);
		msg.setFrom(addressFrom);

		StringTokenizer st = new StringTokenizer (to,",");
        InternetAddress[] rcps = new InternetAddress[st.countTokens()];
		int idx = 0;

		while (st.hasMoreTokens()) {
			InternetAddress addressTo = new InternetAddress(st.nextToken());
			rcps[idx++] = addressTo;
        }

        logger.debug("Recipients list: " + FormatUtil.toJsonString(rcps));
		msg.setRecipients(Message.RecipientType.TO,rcps);
		msg.setSubject(subject);
		msg.setContent(message, "text/html");
		Transport.send(msg);
        logger.debug("Message delivered!");
	}
}