package gov.nysenate.sage.scripts.streetfinder.scripts.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Range;

import java.io.Serializable;

/**
 * A simple class to access data from a call to the validate API.
 */
public class AddressValidationResult implements Serializable {
    private static final int DEFAULT_INT = Short.MIN_VALUE;
    private final Addr1WithZip validatedAddr;
    // The validated address is within this range.
    private final Range<Integer> withinRange;

    public AddressValidationResult(JsonNode response) {
        if (!response.get("validated").asBoolean()) {
            this.validatedAddr = null;
            this.withinRange = null;
            return;
        }
        JsonNode address = response.get("address");
        var addr = new Addr1WithZip(address.get("addr1").asText(), address.get("zip5").asInt());
        JsonNode records = response.get("records");
        if (records.isEmpty()) {
            this.validatedAddr = addr;
            this.withinRange = null;
            return;
        }
        var bldgRange = Range.closed(
                records.get(0).get("primaryLow").asInt(DEFAULT_INT),
                records.get(0).get("primaryHigh").asInt(DEFAULT_INT));
        // This wouldn't be an actual Range.
        if (bldgRange.contains(DEFAULT_INT)) {
            bldgRange = null;
        }
        this.validatedAddr = addr;
        this.withinRange = bldgRange;
    }

    public boolean isValid() {
        return validatedAddr != null;
    }

    public Addr1WithZip addr() {
        return validatedAddr;
    }

    public Range<Integer> withinRange() {
        return withinRange;
    }
}
