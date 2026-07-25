package gov.nysenate.sage.client.response.base;

import com.google.common.collect.ImmutableMap;

public record MapResponse<KeyType, ViewType>(ImmutableMap<KeyType, ViewType> items) {
    public int getSize() {
        return items.size();
    }
}
