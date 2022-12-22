package gov.nysenate.sage.scripts.streetfinder.scripts;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class GetCodes {
    public static Map<String, String> getCodesHelper(String sageUrl, String adminKey, boolean isTown)
            throws Exception {
        final String endPoint = isTown ? "towncodes" : "countycodes";
        final String printType = isTown ? "town" : "county";
        String urlString = "%s/admin/datagen/%s?key=%s".formatted(sageUrl, endPoint, adminKey);
        urlString = urlString.replaceAll(" ", "%20");
        try (InputStream is = new URL(urlString).openStream()) {
            String sageResponse = IOUtils.toString(is, StandardCharsets.UTF_8);
            JsonNode jsonResponse = new ObjectMapper().readTree(sageResponse);
            if (!Boolean.parseBoolean(jsonResponse.get("success").toString())) {
                throw new Exception("SAGE failed to create " + printType + " code service");
            }
            return mapFromResponse(jsonResponse.get("message").toString());
        }
    }

    private static Map<String, String> mapFromResponse(String response) {
        var map = new HashMap<String, String>();
        String[] pairs = response.split("\\|");
        for (String pair : pairs) {
            if (pair.isBlank()) {
                continue;
            }
            String[] values = pair.replaceAll("\"", "").split(",");
            map.put(values[0], values[1]);
        }
        return map;
    }
}
