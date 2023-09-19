package gov.nysenate.sage.scripts.streetfinder.scripts.utils;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

import java.util.Map;
import java.util.Set;

/**
 * Maps a generic row and column to a compact list of longs.
 */
public class LongMultiTable<R, C> {
    private final Table<R, C, LongList> internalTable;

    public LongMultiTable(int expectedRows, int expectedCellsPerRow) {
        this.internalTable = HashBasedTable.create(expectedRows, expectedCellsPerRow);
    }

    public void put(R row, C column, long value) {
        var currCell = internalTable.get(row, column);
        if (currCell == null) {
            currCell = new LongArrayList();
            internalTable.put(row, column, currCell);
        }
        currCell.add(value);
    }

    public Set<R> rows() {
        return internalTable.rowKeySet();
    }

    public Map<C, LongList> getRow(R row) {
        return internalTable.row(row);
    }
}
