package gov.nysenate.sage.scripts.streetfinder.scripts.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Range;
import gov.nysenate.sage.util.Tuple;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains some common code for accessing the APIs outside SAGE.
 */
public class ApiHelper {
    private static final int DEFAULT_INT = Short.MIN_VALUE;
    private final static String datagen = "/admin/datagen/",
            validate = "/api/validate";
    public final static Tuple<Addr1WithZip, Range<Integer>> invalid = new Tuple<>(null, null);
    private final String sageUrl, adminKey, amsUrl;

    public ApiHelper(String sageUrl, String adminKey, String amsUrl) {
        this.sageUrl = sageUrl;
        this.adminKey = adminKey;
        this.amsUrl = amsUrl;
    }

    /**
     * Gets the codes from the given SAGE instance.
     * @param isTown true if we're getting town codes, otherwise we're getting county codes.
     */
    public Map<String, String> getCodes(boolean isTown) throws Exception {
        final String endPoint = isTown ? "towncodes" : "countycodes";
        final String printType = isTown ? "town" : "county";
        String urlString = sageUrl + datagen + "%s?key=%s".formatted(endPoint, adminKey);
        JsonNode jsonResponse = getResponse(urlString);
        if (!jsonResponse.get("success").asBoolean()) {
            throw new Exception("SAGE failed to create " + printType + " code service");
        }
        return mapFromResponse(jsonResponse.get("message").asText());
    }

    /**
     * @return a validated and standardized address, or null if there was a problem accessing the API.
     */
    public AddressValidationResult getValidatedAddress(Addr1WithZip addr1WithZip) {
        String urlString = amsUrl + validate + "?addr1=%s&state=New York&zip5=%s&initCaps=true&detail=true"
                        .formatted(addr1WithZip.addr1(), addr1WithZip.zip());
        try {
            return new AddressValidationResult(getResponse(urlString));
        } catch (IOException ex) {
            System.err.println("Problem parsing " + addr1WithZip.addr1());
            return null;
        }
    }

    private static JsonNode getResponse(String urlString) throws IOException {
        urlString = urlString.replaceAll(" ", "%20");
        try (InputStream is = new URL(urlString).openStream()) {
            String response = IOUtils.toString(is, StandardCharsets.UTF_8);
            return new ObjectMapper().readTree(response);
        }
    }

    /**
     * The codes are stored in a specific, standard format.
     * See getCodes in {@link gov.nysenate.sage.controller.admin.DataGenController}.
     */
    // TODO: cleanup
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
