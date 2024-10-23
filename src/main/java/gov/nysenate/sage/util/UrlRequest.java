package gov.nysenate.sage.util;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public abstract class UrlRequest {
    private static Logger logger = LoggerFactory.getLogger(UrlRequest.class);
    private static final int CONNECTION_TIMEOUT = 10000;
    private static final int RESPONSE_TIMEOUT = 30000;

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
    */
    public static String getResponseFromUrl(String url) throws IOException {
        InputStream inputStream = getInputStreamFromUrl(url);
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
        URL u = new URL(url);
        logger.debug("Requesting connection to " + url);
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

    public static String getResponseFromUrlUsingPOST(String url, String postBody) throws IOException {
        InputStream inputStream = getInputStreamFromUrlUsingPOST(url, postBody);
        return getResponseFromInputStream(inputStream);
    }

    public static InputStream getInputStreamFromUrlUsingPOST(String url, String postBody) throws IOException {
        URL u = new URL(url);
        logger.debug("Requesting connection to " + url);
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
     * Returns a HttpURLConnection object using the URL supplied. Timeout options are set as well.
     * @param u URL
     * @return HttpURLConnection
     */
    private static HttpURLConnection getHttpURLConnection(URL u) throws IOException {
        HttpURLConnection uc = (HttpURLConnection) u.openConnection();
        uc.setConnectTimeout(CONNECTION_TIMEOUT);
        uc.setReadTimeout(RESPONSE_TIMEOUT);
        return uc;
    }

    public static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
        return sb.toString();
    }
}
