package gov.nysenate.sage.util;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.http.HttpRequest;
import oauth.signpost.signature.AuthorizationHeaderSigningStrategy;
import oauth.signpost.signature.HmacSha1MessageSigner;
import oauth.signpost.signature.SigningStrategy;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public abstract class UrlRequest
{
    public static Logger logger = Logger.getLogger(UrlRequest.class);

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
        if (inputStream != null) {
            String response = IOUtils.toString(inputStream);
            logger.debug("Retrieved string of length " + response.length());
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
        HttpURLConnection uc = (HttpURLConnection)u.openConnection();
        int responseCode = uc.getResponseCode();
        logger.debug("Connection replied with response code: " + responseCode);

        if (responseCode >= 400) {
            logger.error("Failed to get a successful response. Returning null input stream.");
            return null;
        }

        InputStream inputStream = uc.getInputStream();
        logger.debug("Retrieved input stream");
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
        InputStream is = getInputStreamFromUrlUsingOauth(url, consumerKey, consumerSecret);
        if (is != null) {
            return IOUtils.toString(is);
        }
        return null;
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
            HttpURLConnection uc = (HttpURLConnection) u.openConnection();
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
        catch (OAuthExpectationFailedException ex) {
            logger.error(ex);
        }
        catch (OAuthCommunicationException ex) {
            logger.error(ex);
        }
        catch (OAuthMessageSignerException ex) {
            logger.error(ex);
        }
        return null;
    }
}