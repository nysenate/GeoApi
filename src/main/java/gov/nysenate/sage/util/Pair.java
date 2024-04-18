package gov.nysenate.sage.util;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.List;

/**
 * A pair of two Objects of the same type. Order isn't relevant for comparisons.
 * @param <T>
 */
public record Pair<T>(T first, T second) implements Iterable<T> {
    @Override
    @Nonnull
    public Iterator<T> iterator() {
        return List.of(first(), second()).iterator();
    }
}
