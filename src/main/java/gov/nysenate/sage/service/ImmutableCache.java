package gov.nysenate.sage.service;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

/**
 * A simple cache that is refreshed all at once.
 * The {@code volatile} field safely publishes the current map to concurrent readers.
 */
public class ImmutableCache<K, V> {
    // Ensures thread-safety
    private static final CopyOnWriteArrayList<Runnable> allRefreshes = new CopyOnWriteArrayList<>();
    private final Supplier<Map<K, V>> mapSupplier;
    private volatile ImmutableMap<K, V> dataMap;

    public ImmutableCache(Supplier<Map<K, V>> mapSupplier) {
        this.mapSupplier = mapSupplier;
        allRefreshes.add(this::refresh);
    }

    public static void refreshAll() {
        allRefreshes.forEach(Runnable::run);
    }

    public final void refresh() {
        dataMap = ImmutableMap.copyOf(mapSupplier.get());
    }

    public ImmutableSet<K> keySet() {
        ensureMapExists();
        return dataMap.keySet();
    }

    public V get(K key) {
        ensureMapExists();
        return dataMap.get(key);
    }

    private void ensureMapExists() {
        // Theoretically two threads could run this at the same time, but it's not a big deal.
        if (dataMap == null) {
            refresh();
        }
    }
}
