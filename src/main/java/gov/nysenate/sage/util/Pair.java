package gov.nysenate.sage.util;

import java.util.Objects;

/**
 * A pair of two Objects of the same type. Order isn't relevant for comparisons.
 * @param <T>
 */
public class Pair<T> {
    private final T one, two;

    public Pair(T one, T two) {
        this.one = one;
        this.two = two;
    }

    public T first() {
        return one;
    }

    public T second() {
        return two;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?> pair = (Pair<?>) o;
        return (Objects.equals(one, pair.one) && Objects.equals(two, pair.two)) ||
                (Objects.equals(one, pair.two) && Objects.equals(two, pair.one));
    }

    @Override
    public int hashCode() {
        return one.hashCode() + two.hashCode();
    }
}
