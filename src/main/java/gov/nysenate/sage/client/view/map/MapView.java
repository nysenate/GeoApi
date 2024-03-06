package gov.nysenate.sage.client.view.map;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class MapView<K, V> {
    private final ImmutableMap<K, V> items;

    public MapView(Map<K, V> map) {
        if (map != null) {
            this.items = ImmutableMap.copyOf(map);
        }
        else {
            this.items = ImmutableMap.of();
        }
    }

    public Map<K, V> getItems() {
        return items;
    }
}
