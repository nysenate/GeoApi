package gov.nysenate.sage.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.StringTokenizer;

@Component
public class Mailer {
    private static final Logger logger = LoggerFactory.getLogger(Mailer.class);
    @Value("${smtp.host}")
    private String smtpHostName;
    @Value("${smtp.debug}")
    private String smtpDebug;
    @Value("${smtp.active}")
    private boolean smtpActive;
    @Value("${smtp.port}")
    private Integer smtpPort;
    @Value("${smtp.user}")
    private String smtpAccountUser;
    @Value("${smtp.pass}")
    private String smtpAccountPass;
    @Value("${smtp.admin}")
    private String smtpAdmin;
    @Value("${smtp.tls.enable}")
    private boolean smtpTlsEnable;
    @Value("${smtp.ssl.enable}")
    private boolean smtpSslEnable;

    public String getAdminEmail() {
        return smtpAdmin;
    }

    public void sendMail(String to, String subject, String message) throws Exception {
        if (smtpActive) {
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHostName);
        props.put("mail.smtp.auth", true);
        props.put("mail.debug", smtpDebug);
        props.put("mail.smtp.port", smtpPort);
        props.put("mail.smtp.starttls.enable", smtpTlsEnable);
        props.put("mail.smtp.socketFactory.port", smtpPort);
        props.put("mail.smtp.socketFactory.fallback", false);
        props.put("mail.smtp.ssl.enable", smtpSslEnable);

        Session session = Session.getDefaultInstance(props,	new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpAccountUser, smtpAccountPass);
            }
        });

        Message msg = new MimeMessage(session);
        InternetAddress addressFrom = new InternetAddress(smtpAdmin);
        addressFrom.setPersonal("SAGE");
        msg.setFrom(addressFrom);

        StringTokenizer st = new StringTokenizer (to,",");
        InternetAddress[] rcps = new InternetAddress[st.countTokens()];
        int idx = 0;

        while (st.hasMoreTokens()) {
            InternetAddress addressTo = new InternetAddress(st.nextToken());
            rcps[idx++] = addressTo;
        }

        logger.debug("Recipients list: {}", FormatUtil.toJsonString(rcps));
        msg.setRecipients(Message.RecipientType.TO,rcps);
        msg.setSubject(subject);
        msg.setContent(message, "text/html");
        Transport.send(msg);
        logger.debug("Message delivered!");
    }
}
