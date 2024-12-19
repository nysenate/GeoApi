package gov.nysenate.sage.util;

import gov.nysenate.sage.config.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.StringTokenizer;

@Component
public class Mailer
{
    private Logger logger = LoggerFactory.getLogger(Mailer.class);
    private String SMTP_HOST_NAME;
    private String SMTP_DEBUG;
    private String SMTP_ACTIVE;
    private String SMTP_PORT;
    private String SMTP_AUTH;
    private String SMTP_ACCOUNT_USER;
    private String SMTP_ACCOUNT_PASS;
    private String SMTP_ADMIN;
    private String SMTP_TLS_ENABLE;
    private String SMTP_SSL_ENABLE;
    private String SMTP_CONTEXT;
    private Environment env;

    @Autowired
    public Mailer(Environment env)
    {
        this.env = env;
        SMTP_HOST_NAME = env.getSmtpHost();
        SMTP_DEBUG = env.getSmtpDebug().toString();
        SMTP_ACTIVE = env.getSmtpActive().toString();
        SMTP_PORT = Integer.toString(env.getSmtpPort());
        SMTP_AUTH = env.getSmtpAuth().toString();
        SMTP_ACCOUNT_USER = env.getSmtpUser();
        SMTP_ACCOUNT_PASS = env.getSmtpPass();
        SMTP_ADMIN = env.getSmtpAdmin();
        SMTP_TLS_ENABLE = String.valueOf(env.getSmtpTlsEnable());
        SMTP_SSL_ENABLE = String.valueOf(env.getSmtpSslEnable());
        SMTP_CONTEXT = env.getSmtpContext();
    }

    public String getContext() {
        return SMTP_CONTEXT;
    }

    public String getAdminEmail() {
        return SMTP_ADMIN;
    }

    public void sendMail(String to, String subject, String message) throws Exception
    {
        sendMail(to, subject, message, SMTP_ADMIN, "SAGE");
    }

    public void sendMail(String to, String subject, String message, String from, String fromDisplay) throws Exception
    {
	    if (!SMTP_ACTIVE.equals("true")) return;

        /** Set FROM to admin if it is empty */
        if (from == null || from.isEmpty()) {
            from = SMTP_ADMIN;
        }

		Properties props = new Properties();
		props.put("mail.smtp.host", SMTP_HOST_NAME);
		props.put("mail.smtp.auth", SMTP_AUTH);
		props.put("mail.debug", SMTP_DEBUG);
		props.put("mail.smtp.port", SMTP_PORT);
		props.put("mail.smtp.starttls.enable",SMTP_TLS_ENABLE);
		props.put("mail.smtp.socketFactory.port", SMTP_PORT);
		props.put("mail.smtp.socketFactory.fallback", "false");
		props.put("mail.smtp.ssl.enable",SMTP_SSL_ENABLE);

        Session session;
        if (env.getSmtpAuth()) {
            session = Session.getDefaultInstance(props,	new javax.mail.Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SMTP_ACCOUNT_USER, SMTP_ACCOUNT_PASS);
                }
            });
        }
		else {
            session = Session.getDefaultInstance(props);
        }

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