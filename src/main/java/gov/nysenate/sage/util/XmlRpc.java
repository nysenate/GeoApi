package gov.nysenate.sage.util;


import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.log4j.Logger;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcClientRequestImpl;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;


@SuppressWarnings("unchecked")
public class XmlRpc {
	
	private Logger logger = Logger.getLogger(XmlRpc.class);

	String SERVICES_URL = "http://www.nysenate.gov/services/xmlrpc";
	String API_KEY = Resource.get("nysenate.key");
	String BASE_URL = "nysenate.gov";

	String NODE_GET = "node.get";
	String VIEWS_GET = "views.get";

	public HashMap<String, Object> getMap(Object o) {

		if (o.getClass().isArray()) {
			return (HashMap<String, Object>) ((Object[]) o)[0];
		}
		return (HashMap<String, Object>) o;
	}

	/*
	 * depending on response object can be a list or a single object
	 */
	public void printView(Object obj) {
		Class<?> clazz = obj.getClass();

		if (clazz.isArray()) {
			for (Object o : (Object[]) obj) {
				System.out.println(o);
			}
		} else {
			System.out.println(obj);
		}
	}

	/**
	 * @param nid
	 *            node id
	 * @return object from XmlRpc request
	 */
	public Object getNode(Integer nid) {
		List<Object> params = new ArrayList<Object>();
		params.addAll(getSecurityParameters(NODE_GET));
		params.add(nid);

//		List<Object> oParams = new ArrayList<Object>();
//		oParams.add("nid");
//		oParams.add("title");
//		oParams.add("field_location");

		return getXmlRpcResponse(NODE_GET, params);
	}

	/**
	 * 
	 * @param viewName
	 *            required
	 * @param displayId
	 * @param offset
	 * @param limit
	 *            max results to return
	 * @param formatOutput
	 *            false should be default, true will return html formatted
	 *            response
	 * @param arguments
	 *            list of optional arguments
	 * @return object from XmlRpc request
	 */
	public Object getView(String viewName, String displayId, Integer offset,
			Integer limit, boolean formatOutput, List<Object> arguments) {

		List<Object> params = new ArrayList<Object>();
		params.addAll(getSecurityParameters(VIEWS_GET));
		params.add(viewName);
		params.add((displayId == null) ? "default" : displayId);

		if (arguments == null) {
			arguments = new ArrayList<Object>();
		}
		params.add(arguments);

		params.add((offset == null) ? 0 : offset);
		params.add((limit == null) ? 0 : limit);
		params.add(formatOutput);

		return getXmlRpcResponse(VIEWS_GET, params);
	}

	/**
	 * @return returns list of parameters that must be tied to ever services
	 *         XmlRpc request
	 */
	public List<Object> getSecurityParameters(String methodName) {
		long time = (new Date()).getTime();
		String nonce = generateServiceNonce(time);
		String hash = generateServicesHash(time, nonce, methodName);
		
		List<Object> params = new ArrayList<Object>();
		params.add(hash);
		params.add(BASE_URL);
		params.add(time + "");
		params.add(nonce);

		return params;
	}

	public String generateServiceNonce(long time) {
		String nonce = null;

		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			
			byte[] md5Digest = md.digest(Long.toString(time).getBytes());

			nonce = "";
			for (byte b : md5Digest) {
				nonce += String.format("%02x", b);
			}
			
			return nonce.substring(0, 20);
			
		} catch (NoSuchAlgorithmException e) {
			logger.warn(e);
		}

		return nonce;
	}

	public String generateServicesHash(long time, String nonce,
			String methodName) {
		String hash = null;

		Mac mac;
		try {
			mac = Mac.getInstance("HmacSHA256");
			
			SecretKeySpec secret = new SecretKeySpec(
					API_KEY.getBytes(),"HmacSHA256");
			
			mac.init(secret);
		
			byte[] shaDigest = mac.doFinal(
						(time + ";" +
						BASE_URL + ";" +
						nonce + ";" +
						methodName).getBytes());
			
			hash = "";
			
			for (byte b : shaDigest) {
				hash += String.format("%02x", b);
			}
			
		} catch (NoSuchAlgorithmException e) {
			logger.warn(e);
		} catch (InvalidKeyException e) {
			logger.warn(e);
		}

		return hash;
	}

	/* the following access the XmlRpc Apache library */

	public XmlRpcClient getXmlRpcClient(XmlRpcClientConfigImpl config) {
		XmlRpcClient client = new XmlRpcClient();
		client.setTransportFactory(new XmlRpcCommonsTransportFactory(client));
		client.setConfig(config);

		return client;
	}

	public Object getXmlRpcResponse(String methodName, List<Object> parameters) {
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		
		try {
			config.setServerURL(new URL(SERVICES_URL));
			
			XmlRpcClientRequestImpl req = new XmlRpcClientRequestImpl(config,
					methodName, parameters);
			
			
			
			return getXmlRpcClient(config).execute(req);
			
		} catch (XmlRpcException e) {
			logger.warn(e);
		} catch (MalformedURLException e) {
			logger.warn(e);
		}
		
		return null;
	}

}