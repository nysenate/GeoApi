package gov.nysenate.sage.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * A simple List implementation that ensures nulls are never in the list.
 */
public class NonnullList<E> {
    private final List<E> internalList = new ArrayList<>();

    /**
     * Note that nulls in the input are allowed, but ignored.
     */
    public static <E> NonnullList<E> of(List<E> elements) {
        var list = new NonnullList<E>();
        if (elements == null) {
            return list;
        }
        for (E element : elements) {
            list.addIfNotNull(element);
        }
        return list;
    }

    public void addIfNotNull(E element) {
        if (element != null) {
            internalList.add(element);
        }
    }

    public Stream<E> stream() {
        return internalList.stream();
    }

    public boolean isEmpty() {
        return internalList.isEmpty();
    }
}
