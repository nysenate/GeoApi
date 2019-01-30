package gov.nysenate.sage.scripts.streetfinder.scripts;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


import java.io.*;
import java.net.URL;

public class GetSenateCountyCodes {

    private static Logger logger = LoggerFactory.getLogger(GetSenateCountyCodes.class);

    public static void main(String[] args) {
        String urlString = args[0] + "/admin/datagen/countycodes";
        urlString = urlString.replaceAll(" ", "%20");
        logger.info(urlString);
        try {
            URL url = new URL(urlString);
            InputStream is = url.openStream();
            String sageReponse = IOUtils.toString(is, "UTF-8");
            JsonNode jsonResonse = new ObjectMapper().readTree(sageReponse);
            is.close();
            String value = jsonResonse.get("status").textValue();
            if (!value.equals("SUCCESS")) {
                throw new Exception("SAGE failed to create town code service");
            }
        }
        catch (Exception e) {
            logger.error("Failed to create town code file" + e);
        }
    }

}
