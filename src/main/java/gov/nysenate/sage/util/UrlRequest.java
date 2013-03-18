package gov.nysenate.sage.util;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
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
        URL u = new URL(url);
        logger.debug("Requesting connection to " + url.toString());
        HttpURLConnection uc = (HttpURLConnection)u.openConnection();
        String response = IOUtils.toString(uc.getInputStream());
        logger.debug("Retrieved response stream of length " + response.length());
        return response;
    }
}