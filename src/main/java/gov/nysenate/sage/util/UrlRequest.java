package gov.nysenate.sage.util;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public abstract class UrlRequest
{
    public static Logger logger = LoggerFactory.getLogger(UrlRequest.class);
    private static int CONNECTION_TIMEOUT = 10000;
    private static int RESPONSE_TIMEOUT = 30000;

    /**
    * Connects to a url and retrieves the body response in String representation.
    * This function can alternatively be implemented using the code snippet below
    * but this produces unnecessarily detailed logs.
    * <code>
    *     Content content = Request.Get(url).execute().returnContent();
    *     String response = content.asString();
    * </code>
    *
    * @param url   Url request string
    * @return      String containing response
    * @throws MalformedURLException
    * @throws IOException
    */
    public static String getResponseFromUrl(String url) throws IOException
    {
        InputStream inputStream = getInputStreamFromUrl(url);
        return getResponseFromInputStream(inputStream);
    }

    private static String getResponseFromInputStream(InputStream inputStream) throws IOException
    {
        if (inputStream != null) {
            String response = IOUtils.toString(inputStream);
            logger.trace("Retrieved string of length " + response.length());
            logger.trace("Response: " + response);
            return response;
        }
        else {
            return null;
        }
    }

    /**
    * Retrieves an input stream from a url resource
    * @param url
    * @return
    * @throws IOException
    */
    public static InputStream getInputStreamFromUrl(String url) throws IOException
    {
        URL u = new URL(url);
        logger.debug("Requesting connection to " + url.toString());
        HttpURLConnection uc = getHttpURLConnection(u);
        int responseCode = uc.getResponseCode();
        logger.debug("Connection replied with response code: " + responseCode);

        if (responseCode >= 400) {
            logger.error("Failed to get a successful response. Returning null input stream.");
            return null;
        }

        InputStream inputStream = uc.getInputStream();
        logger.trace("Retrieved input stream");
        return inputStream;
    }

    public static String getResponseFromUrlUsingPOST(String url, String postBody) throws IOException
    {
        InputStream inputStream = getInputStreamFromUrlUsingPOST(url, postBody);
        return getResponseFromInputStream(inputStream);
    }

    public static InputStream getInputStreamFromUrlUsingPOST(String url, String postBody) throws IOException
    {
        URL u = new URL(url);
        logger.debug("Requesting connection to " + url.toString());
        HttpURLConnection uc = getHttpURLConnection(u);
        uc.setRequestMethod("POST");
        uc.setDoOutput(true);
        uc.setRequestProperty("Content-Length", String.valueOf(postBody.length()));
        uc.setRequestProperty("Content-Type", "text/javascript");
        OutputStream os = uc.getOutputStream();
        os.write(postBody.getBytes());
        os.flush();
        os.close();

        int responseCode = uc.getResponseCode();
        logger.debug("Connection replied with response code: " + responseCode);

        if (responseCode >= 400) {
            logger.error("Failed to get a successful response. Returning null input stream.");
            return null;
        }

        InputStream inputStream = uc.getInputStream();
        logger.trace("Retrieved input stream");
        return inputStream;
    }

    /**
    * Retrieves String response from an OAuth signed url request.
    * @param url            Request Url
    * @param consumerKey    Consumer Key for OAuth request
    * @param consumerSecret Consumer Secret for OAuth request
    * @return               InputStream on success, null otherwise
    */
    public static String getResponseFromUrlUsingOauth(String url, String consumerKey, String consumerSecret) throws IOException
    {
        InputStream inputStream = getInputStreamFromUrlUsingOauth(url, consumerKey, consumerSecret);
        return getResponseFromInputStream(inputStream);
    }

    /**
    * Retrieves input stream from an OAuth signed url request.
    * @param url            Request Url
    * @param consumerKey    Consumer Key for OAuth request
    * @param consumerSecret Consumer Secret for OAuth request
    * @return               InputStream on success, null otherwise
    */
    public static InputStream getInputStreamFromUrlUsingOauth(String url, String consumerKey, String consumerSecret) throws IOException
    {
        try {
            logger.debug("Requesting connection to: " + url);
            URL u = new URL(url);
            HttpURLConnection uc = getHttpURLConnection(u);
            OAuthConsumer consumer = new DefaultOAuthConsumer(consumerKey, consumerSecret);
            consumer.sign(uc);

            int responseCode = uc.getResponseCode();
            if (responseCode >= 400) {
                logger.error("Service responded with error code (" + responseCode + "): " + uc.getResponseMessage() + ". " + IOUtils.toString(uc.getErrorStream()));
                return null;
            }

            return uc.getInputStream();
        }
        catch(MalformedURLException ex) {
            logger.error("Malformed Url in Oauth Request: " + ex.getMessage(), ex);
        }
        catch (OAuthExpectationFailedException |
               OAuthCommunicationException |
               OAuthMessageSignerException ex) {
            logger.error("" + ex);
        }
        return null;
    }

    /**
     * Returns a HttpURLConnection object using the URL supplied. Timeout options are set as well.
     * @param u URL
     * @return HttpURLConnection
     * @throws IOException
     */
    private static HttpURLConnection getHttpURLConnection(URL u) throws IOException
    {
        HttpURLConnection uc = (HttpURLConnection) u.openConnection();
        uc.setConnectTimeout(CONNECTION_TIMEOUT);
        uc.setReadTimeout(RESPONSE_TIMEOUT);
        return uc;
    }
}