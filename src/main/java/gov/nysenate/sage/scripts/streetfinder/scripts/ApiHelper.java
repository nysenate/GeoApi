package gov.nysenate.sage.scripts.streetfinder.scripts;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains some common code for accessing the SAGE API from outside SAGE.
 */
public class ApiHelper {
    private final static String apiStart = "/api/v2/", datagen = "/admin/datagen/", validate = apiStart + "address/validate";
    private final String sageUrl, adminKey;

    public ApiHelper(String sageUrl, String adminKey) {
        this.sageUrl = sageUrl;
        this.adminKey = adminKey;
    }

    /**
     * Gets the codes from the given SAGE instance.
     * @param isTown true if we're getting town codes, otherwise false.
     */
    public Map<String, String> getCodes(boolean isTown) throws Exception {
        final String endPoint = isTown ? "towncodes" : "countycodes";
        final String printType = isTown ? "town" : "county";
        String urlString = sageUrl + datagen + "%s?key=%s".formatted(endPoint, adminKey);
        JsonNode jsonResponse = getResponse(urlString);
        if (!Boolean.parseBoolean(jsonResponse.get("success").toString())) {
            throw new Exception("SAGE failed to create " + printType + " code service");
        }
        return mapFromResponse(jsonResponse.get("message").toString());
    }

    /**
     * The fields are all needed to get accurate matches.
     * @return the street, in standard USPS format.
     */
    public String getCorrectedStreet(String num, String street, String zip5) throws Exception {
        String urlString = sageUrl + validate +
                "?addr1=%s&state=New York&zip5=%s".formatted(num + " " + street, zip5);
        JsonNode jsonResponse = getResponse(urlString);
        if (!Boolean.parseBoolean(jsonResponse.get("validated").toString())) {
            return null;
        }
        String correctedAddr = jsonResponse.get("address").get("addr1").toString();
        if (!num.isBlank()) {
            correctedAddr = correctedAddr.replaceFirst(num, "");
        }
        return correctedAddr;
    }

    private static JsonNode getResponse(String urlString) throws Exception {
        urlString = urlString.replaceAll(" ", "%20");
        try (InputStream is = new URL(urlString).openStream()) {
            String sageResponse = IOUtils.toString(is, StandardCharsets.UTF_8);
            return new ObjectMapper().readTree(sageResponse);
        }
    }

    /**
     * The codes are stored in a specific, standard format.
     * See getCodes in {@link gov.nysenate.sage.controller.admin.DataGenController}.
     */
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
