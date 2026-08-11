package gov.nysenate.sage.model.district;

import com.fasterxml.jackson.annotation.JsonValue;

public record DistrictId(String id) {
    public DistrictId {
        if (id == null || !id.matches("(\\d+-)*\\d+")) {
            throw new IllegalArgumentException(id + " is not a valid DistrictId");
        }
    }

    // Serialized as a bare string, so clients can pass it straight back as a request param.
    @JsonValue
    @Override
    public String id() {
        return id;
    }

    @Override
    public String toString() {
        return id;
    }
}
