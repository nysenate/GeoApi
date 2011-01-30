package gov.nysenate.sage.connectors;


import generated.dmv.AmsValidateResponse;
import gov.nysenate.sage.model.ErrorResponse;
import gov.nysenate.sage.model.ValidateResponse;
import gov.nysenate.sage.util.Resource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.soap.*;
import javax.xml.transform.stream.StreamSource;

import com.google.gson.Gson;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;



public class DMVConnect {
	private static final String DMV_URL = "dmv.url";
	private static final String DMV_USER = "dmv.user";
	private static final String DMV_PASS = "dmv.pass";
	private static final String DMV_TOKEN = "dmv.token";

	public static String validateAddress(String addr, String city, String state, String zip, String punctuation, String format) throws Exception {
		AmsValidateResponse avr = connect(constructSoap(addr,city,state,zip));
		
		Object response = null;
		
		if(avr.getAmsValidateResult().getSYSTEMMESSAGES().getMESSAGETEXT1() != null) {
			response = new ErrorResponse(avr.getAmsValidateResult().getSYSTEMMESSAGES().getMESSAGETEXT1());
		}
		else  {
			ValidateResponse vr = new ValidateResponse();
			vr.setAddress2(avr.getAmsValidateResult().getOUTPUT().getSTDDELIVADDR(), punctuation);
			vr.setCity(avr.getAmsValidateResult().getOUTPUT().getSTDCITY());
			vr.setState(avr.getAmsValidateResult().getOUTPUT().getSTDSTATE());
			vr.setZip4(Integer.toString(avr.getAmsValidateResult().getOUTPUT().getZIPADDON()));
			vr.setZip5(Integer.toString(avr.getAmsValidateResult().getOUTPUT().getZIP()));
			
			response = vr;
		}
		
		if(format.equals("xml")) {
			XStream xstream = new XStream(new DomDriver());
			xstream.processAnnotations(new Class[]{ValidateResponse.class, ErrorResponse.class});
			return xstream.toXML(response);
		}
		Gson gson = new Gson();
		return gson.toJson(response);
	}
	
	private static AmsValidateResponse connect(String command) throws Exception {
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[]{
		    new X509TrustManager() {
		        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
		            return null;
		        }
		        public void checkClientTrusted(
		            java.security.cert.X509Certificate[] certs, String authType) {
		        }
		        public void checkServerTrusted(
		            java.security.cert.X509Certificate[] certs, String authType) {
		        }
		    }
		};
		
		try {
		    SSLContext sc = SSLContext.getInstance("SSL");
		    sc.init(null, trustAllCerts, new java.security.SecureRandom());
		    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
		}

		// Create the connection
		SOAPConnectionFactory scf = SOAPConnectionFactory.newInstance();
		SOAPConnection conn = scf.createConnection();
				
		// Create message
		MessageFactory mf = MessageFactory.newInstance();
		SOAPMessage msg = mf.createMessage();
							
		// Object for message parts
		SOAPPart sp = msg.getSOAPPart();
		StreamSource prepMsg = new StreamSource(
		  new StringReader(command));
		sp.setContent(prepMsg);
		
		MimeHeaders hd = msg.getMimeHeaders();
        hd.addHeader("SOAPAction", "http://www.nysdmv.com/DMVAMSWS/AmsValidate");
        hd.addHeader("Host", "wsc.dmv.state.ny.us");
		
		// Save message
		msg.saveChanges();
				
		// Send
		SOAPMessage rp = conn.call(msg, Resource.get(DMV_URL));
				
		// Get reply content
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		rp.writeTo(baos);
		
		String response = baos.toString();		
		conn.close();
		
		Pattern p = Pattern.compile("<AmsValidateResponse.*?</AmsValidateResponse>");
		Matcher m = p.matcher(response);
		
		if(m.find()) {
			response = response.substring(m.start(),m.end());
		}
				
		return (AmsValidateResponse)parseStream(response);
		
	}
	
	private static String constructSoap(String addr, String city, String state, String zip) {
		String soap = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:dmv=\"http://www.nysdmv.com/DMVAMSWS/\">"
			+ "<soapenv:Header>"
			+ "<wsse:Security xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">"
			+ "<wsse:UsernameToken wsu:Id=\"" + Resource.get(DMV_TOKEN) +"\" xmlns:wsu=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd\">"
			+ "<wsse:Username>" + Resource.get(DMV_USER) + "</wsse:Username>"
			+ "<wsse:Password Type=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText\">" + Resource.get(DMV_PASS) + "</wsse:Password>"
			+ "</wsse:UsernameToken>"
			+ "</wsse:Security>"
			+ "</soapenv:Header>"
			+ "<soapenv:Body>"
			+ "<dmv:AmsValidate>"
			+ "<dmv:structInput>"
			+ "<dmv:ADDRESS>" + (addr == null ? "?" : addr) + "</dmv:ADDRESS>"
			+ "<dmv:CITY>" + (city == null ? "?" : city) + "</dmv:CITY> "
			+ "<dmv:STATE>" + (state == null ? "?" : state) + "</dmv:STATE>"
			+ "<dmv:ZIP>" + (zip == null ? "?" : zip) + "</dmv:ZIP>"
			+ "</dmv:structInput>"
			+ "</dmv:AmsValidate>"
			+ "</soapenv:Body>"
			+ "</soapenv:Envelope>";
		
		return soap;
	}
	
	private static Object parseStream(String s) throws Exception{
		
		String packageName = "generated.dmv";
		JAXBContext jc = JAXBContext.newInstance(packageName);
		Unmarshaller u = jc.createUnmarshaller();
		Object o = u.unmarshal(new ByteArrayInputStream(s.getBytes()));

		return o;
	}
}
