package gov.nysenate.sage.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

/**
 * A class used to get batches of a set size.
 */
public class BatchSupplier<V> {
    private final Supplier<Boolean> hasNext;
    // Note that if this throws an error, it's automatically skipped.
    private final Supplier<V> valueSupplier;
    private final int batchSize;
    private final boolean excludeNulls;

    public BatchSupplier(Iterator<V> batchIter, int batchSize, boolean excludeNulls) {
        this(batchIter::hasNext, batchIter::next, batchSize, excludeNulls);
    }

    public BatchSupplier(Supplier<Boolean> hasNext, Supplier<V> valueSupplier, int batchSize, boolean excludeNulls) {
        this.hasNext = hasNext;
        this.valueSupplier = valueSupplier;
        this.batchSize = batchSize;
        this.excludeNulls = excludeNulls;
    }

    public boolean hasNextBatch() {
        return hasNext.get();
    }

    public List<V> getNextBatch() {
        var batch = new ArrayList<V>(batchSize);
        for (int i = 0; i < batchSize && hasNext.get();) {
            V next = null;
            try {
                next = valueSupplier.get();
            } catch (Exception ignored) {}
            if (excludeNulls && next == null) {
                continue;
            }
            batch.add(next);
            i++;
        }
        return batch;
    }
}
