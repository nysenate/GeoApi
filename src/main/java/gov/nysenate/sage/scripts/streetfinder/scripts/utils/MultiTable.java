package gov.nysenate.sage.scripts.streetfinder.scripts.utils;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.*;

/**
 * Maps a generic row and column to a list of values.
 */
public class MultiTable<R, C, V> {
    private final Table<R, C, List<V>> internalTable = HashBasedTable.create();

    public boolean containsRow(R row) {
        return internalTable.containsRow(row);
    }

    public void put(R row, C column, Collection<V> values) {
        for (V value : values) {
            put(row, column, value);
        }
    }

    public void put(R row, C column, V value) {
        var currCell = internalTable.get(row, column);
        if (currCell == null) {
            currCell = new ArrayList<>();
            internalTable.put(row, column, currCell);
        }
        currCell.add(value);
    }

    public Set<R> rows() {
        return internalTable.rowKeySet();
    }

    public Map<C, List<V>> getRow(R row) {
        return internalTable.row(row);
    }
}
