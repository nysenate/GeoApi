package gov.nysenate.sage.util;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public abstract class UrlRequest {
    private static final Logger logger = LoggerFactory.getLogger(UrlRequest.class);
    private static final int CONNECTION_TIMEOUT = 10000;
    private static final int DEFAULT_RESPONSE_TIMEOUT = 5000;

    public static String getResponseFromUrl(String url) throws IOException {
        return getResponseFromUrl(url, DEFAULT_RESPONSE_TIMEOUT);
    }

    /**
    * Connects to a url and retrieves the body response.
    *
    * @param url          Url request string
    * @param readTimeout  read timeout in milliseconds
    * @return             String containing response
    */
    public static String getResponseFromUrl(String url, int readTimeout) throws IOException {
        InputStream inputStream = getInputStreamFromUrl(url, readTimeout);
        return getResponseFromInputStream(inputStream);
    }

    private static String getResponseFromInputStream(InputStream inputStream) throws IOException {
        if (inputStream != null) {
            String response = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            logger.trace("Retrieved string of length {}", response.length());
            logger.trace("Response: {}", response);
            return response;
        }
        else {
            return null;
        }
    }

    /**
    * Retrieves an input stream from a url resource
    */
    public static InputStream getInputStreamFromUrl(String url) throws IOException {
        return getInputStreamFromUrl(url, DEFAULT_RESPONSE_TIMEOUT);
    }

    public static InputStream getInputStreamFromUrl(String url, int readTimeout) throws IOException {
        URL u = new URL(url);
        logger.debug("Requesting connection to " + url);
        HttpURLConnection uc = getHttpURLConnection(u, readTimeout);
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

    public static String getResponseFromUrlUsingPOST(String url, String postBody) throws IOException {
        URL u = new URL(url);
        logger.debug("Requesting connection to " + url);
        HttpURLConnection uc = getHttpURLConnection(u, DEFAULT_RESPONSE_TIMEOUT);
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
        return getResponseFromInputStream(inputStream);
    }

    /**
     * Returns a HttpURLConnection object using the URL supplied. Timeout is set as well.
     * @param u URL
     * @return HttpURLConnection
     */
    private static HttpURLConnection getHttpURLConnection(URL u, int readTimeout) throws IOException {
        HttpURLConnection uc = (HttpURLConnection) u.openConnection();
        uc.setConnectTimeout(CONNECTION_TIMEOUT);
        uc.setReadTimeout(readTimeout);
        return uc;
    }
}
