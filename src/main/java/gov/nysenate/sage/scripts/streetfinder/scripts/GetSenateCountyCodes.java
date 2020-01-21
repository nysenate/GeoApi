package gov.nysenate.sage.scripts.streetfinder.scripts;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;


import java.io.*;
import java.net.URL;

public class GetSenateCountyCodes {

    public static void main(String[] args) {
        String urlString = args[0] + "/admin/datagen/countycodes?key=" + args[1];
        urlString = urlString.replaceAll(" ", "%20");
        System.out.println(urlString);
        try {
            URL url = new URL(urlString);
            InputStream is = url.openStream();
            String sageReponse = IOUtils.toString(is, "UTF-8");
            JsonNode jsonResonse = new ObjectMapper().readTree(sageReponse);
            is.close();
            String value = jsonResonse.get("success").toString();
            if (!Boolean.parseBoolean(value)) {
                throw new Exception("SAGE failed to create town code service");
            }
        }
        catch (Exception e) {
            System.err.println("Failed to create town code file" + e);
        }
    }

}
