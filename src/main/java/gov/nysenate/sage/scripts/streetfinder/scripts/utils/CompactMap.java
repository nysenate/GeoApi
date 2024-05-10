package gov.nysenate.sage.scripts.streetfinder.scripts.utils;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.BiConsumer;

public class CompactMap<K, V> implements Map<K, V> {
    private static final Object[] emptyArray = new Object[]{};
    private K[] keys = (K[]) emptyArray;
    private V[] values = (V[]) emptyArray;

    @Override
    public int size() {
        return keys.length;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V get(Object key) {
        try {
            int index = indexOf((K) key);
            return index == -1 ? null : values[index];
        } catch (ClassCastException ex) {
            return null;
        }
    }

    @Override
    public V put(K key, V value) {
        int index = indexOf(key);
        if (index == -1) {
            keys = Arrays.copyOf(keys, keys.length + 1);
            keys[keys.length - 1] = key;
            values = Arrays.copyOf(values, values.length + 1);
            values[values.length - 1] = value;
            return null;
        }
        V oldValue = values[index];
        values[index] = value;
        return oldValue;
    }

    @Override
    public V remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (var entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        this.keys = (K[]) emptyArray;
        this.values = (V[]) emptyArray;
    }

    @Nonnull
    @Override
    public Set<K> keySet() {
        return Set.of(keys);
    }

    @Nonnull
    @Override
    public Collection<V> values() {
        return List.of(values);
    }

    @Nonnull
    @Override
    public Set<Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        for (int i = 0; i < keys.length; i++) {
            action.accept(keys[i], values[i]);
        }
    }

    private int indexOf(K key) {
        for (int i = 0; i < keys.length; i++) {
            if (Objects.equals(keys[i], key)) {
                return i;
            }
        }
        return -1;
    }
}
