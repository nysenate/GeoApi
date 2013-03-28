package gov.nysenate.sage.util;

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

}