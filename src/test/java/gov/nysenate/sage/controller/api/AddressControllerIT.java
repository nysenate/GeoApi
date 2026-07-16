package gov.nysenate.sage.controller.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.nysenate.sage.WebAppBaseTests;
import gov.nysenate.sage.annotation.SillyTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * SillyTest for now, since it relies on the underlying AMS service being up and running.
 */
@Category(SillyTest.class)
public class AddressControllerIT extends WebAppBaseTests {
    private static final String BATCH_PATH = "/api/v2/address/validate/batch";
    private static final ObjectMapper mapper = new ObjectMapper();

    private JsonNode postBatch(String body, String... providerParam) throws Exception {
        var request = post(BATCH_PATH).contentType(APPLICATION_JSON).accept(APPLICATION_JSON).content(body);
        for (String provider : providerParam) {
            request = request.param("provider", provider);
        }
        String response = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(response);
    }

    @Test
    public void batchValidateParsesPayloadAndReturnsAResultPerAddress() throws Exception {
        JsonNode root = postBatch("""
                [{"addr1":"200 State St","postalCity":"Albany","state":"NY","zip5":"12210"},
                 {"addr1":"100 N Drive","postalCity":"Troy","state":"NY","zip5":"12180"}]""");

        assertEquals("SUCCESS", root.get("status").asText());
        assertEquals(2, root.get("total").asInt());

        JsonNode results = root.get("results");
        assertTrue(results.get(0).get("address").get("addr1").asText().contains("State St"));
        assertEquals("Albany", results.get(0).get("address").get("city").asText());
        assertEquals("Troy", results.get(1).get("address").get("city").asText());
    }

    @Test
    public void batchValidateHonorsTheProviderParam() throws Exception {
        JsonNode root = postBatch("""
                [{"addr1":"200 State St","postalCity":"Albany","state":"NY","zip5":"12210"}]""", "AIS");

        assertEquals(1, root.get("total").asInt());
        assertEquals("AIS", root.get("results").get(0).get("sources").asText());
    }

    @Test
    public void batchValidateRejectsAnUnknownProvider() throws Exception {
        mockMvc.perform(post(BATCH_PATH).param("provider", "NOT_A_PROVIDER").contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON).content("[]"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void batchValidateRejectsAnUnparseablePayload() throws Exception {
        JsonNode root = postBatch("""
                {"addresses":[{"addr1":"200 State St","postalCity":"Albany","state":"NY","zip5":"12210"}]}""");

        assertEquals("INVALID_BATCH_ADDRESSES", root.get("status").asText());
        assertEquals(55, root.get("statusCode").asInt());
    }

    /** An empty array is indistinguishable from a parse failure, matching the sibling batch endpoints. */
    @Test
    public void batchValidateRejectsAnEmptyArray() throws Exception {
        JsonNode root = postBatch("[]");

        assertEquals("INVALID_BATCH_ADDRESSES", root.get("status").asText());
    }
}
