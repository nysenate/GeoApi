package gov.nysenate.sage.BulkProcessing;

import gov.nysenate.sage.model.BulkProcessing.JobProcess;
import gov.nysenate.sage.util.Config;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Mailer {
	private static final String SMTP_HOST_NAME = Config.read("smtp.host");
	private static final String SMTP_DEBUG = Config.read("smtp.debug");
	private static final String SMTP_ACTIVE = Config.read("smtp.active");
	private static final String SMTP_PORT = Config.read("smtp.port");
	private static final String SMTP_ACCOUNT_USER = Config.read("smtp.user");
	private static final String SMTP_ACCOUNT_PASS = Config.read("smtp.pass");
	private static final String STMP_USER = Config.read("smtp.admin");

	public static void sendMail(String to, String subject, String message, String from, String fromDisplay) throws Exception {
	    if (!SMTP_ACTIVE.equals("true")) return;

		Properties props = new Properties();
		props.put("mail.smtp.host", SMTP_HOST_NAME);
		props.put("mail.smtp.auth", "true");
		props.put("mail.debug", SMTP_DEBUG);
		props.put("mail.smtp.port", SMTP_PORT);
		props.put("mail.smtp.starttls.enable","false");
		props.put("mail.smtp.socketFactory.port", SMTP_PORT);
		props.put("mail.smtp.socketFactory.fallback", "false");
		props.put("mail.smtp.ssl.enable","false");

		Session session = Session.getDefaultInstance(props,	new javax.mail.Authenticator() {
										@Override
										protected PasswordAuthentication getPasswordAuthentication() {
											return new PasswordAuthentication(SMTP_ACCOUNT_USER, SMTP_ACCOUNT_PASS);}});

		Message msg = new MimeMessage(session);
		InternetAddress addressFrom = new InternetAddress(from);
		addressFrom.setPersonal(fromDisplay);
		msg.setFrom(addressFrom);


		StringTokenizer st = new StringTokenizer (to,",");

		InternetAddress[] rcps = new InternetAddress[st.countTokens()];
		int idx = 0;

		while (st.hasMoreTokens())
		{
			InternetAddress addressTo = new InternetAddress(st.nextToken());
			rcps[idx++] = addressTo;

		}

		msg.setRecipients(Message.RecipientType.TO,rcps);

		msg.setSubject(subject);
		msg.setContent(message, "text/html");

		Transport.send(msg);
	}

	public static void mailError(Exception e) {
		try {
		    StringWriter msg = new StringWriter();
		    PrintWriter out = new PrintWriter(msg);
		    out.println("<pre>");
		    e.printStackTrace(out);
		    out.println("</pre>");
			sendMail(STMP_USER,
					"bulk upload error",
					msg.getBuffer().toString(),
					STMP_USER,
					"SAGE Bulk error");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public static void mailError(Exception e, JobProcess jp) {
		try {
		    StringWriter msg = new StringWriter();
            PrintWriter out = new PrintWriter(msg);
            out.println(e.getMessage());
            e.printStackTrace(out);
			sendMail(STMP_USER,
					"bulk processing error",
					jp.getContact() + " - " + jp.getClassName() + " - " + jp.getFileName() + "<br/><br/>" + e.getMessage(),
					STMP_USER,
					"SAGE Bulk front-end error");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public static void mailAdminComplete(JobProcess jp) {
		try {
			sendMail(STMP_USER,
					"bulk processing complete",
					jp.getContact() + " - " + jp.getClassName() + " - " + jp.getFileName() + "<br/><br/>",
					STMP_USER,
					"SAGE Bulk processing complete");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public static void mailUserComplete(JobProcess jp) {
		try {
			sendMail(jp.getContact(),
					"SAGE Districting Completed",
					"Your request from " + new Date(jp.getRequestTime()) + " has been completed and can be downloaded at "+Config.read("smtp.context")+"/downloads/" + jp.getFileName() +
					"<br/><br/>This is an automated message.",
					STMP_USER,
					"SAGE");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
}
