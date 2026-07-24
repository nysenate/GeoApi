package gov.nysenate.sage.client.response.base;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;

import java.util.Map;

@Getter
public class MapResponse<KeyType, ViewType> {
    private final ImmutableMap<KeyType, ViewType> items;

    public MapResponse(Map<KeyType, ViewType> map) {
        if (map != null) {
            this.items = ImmutableMap.copyOf(map);
        }
        else {
            this.items = ImmutableMap.of();
        }
    }

    public Map<KeyType, ViewType> getItems() {
        return items;
    }

    public int getSize() {
        return items.size();
    }
}
