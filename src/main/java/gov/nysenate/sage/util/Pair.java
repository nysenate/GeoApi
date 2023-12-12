package gov.nysenate.sage.util;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.List;

/**
 * A pair of two Objects of the same type. Order isn't relevant for comparisons.
 * @param <T>
 */
public class Pair<T> extends Tuple<T, T> implements Iterable<T> {
    public Pair(T one, T two) {
        super(one, two);
    }


    @Override
    @Nonnull
    public Iterator<T> iterator() {
        return List.of(first(), second()).iterator();
    }
}
