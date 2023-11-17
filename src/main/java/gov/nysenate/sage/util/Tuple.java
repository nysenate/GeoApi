package gov.nysenate.sage.util;

import java.util.Objects;

public class Tuple<T, R> {
    private final T first;
    private final R second;

    public Tuple(T first, R second) {
        this.first = first;
        this.second = second;
    }

    public T first() {
        return first;
    }

    public R second() {
        return second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tuple<?, ?> tuple = (Tuple<?, ?>) o;
        return Objects.equals(first, tuple.first) && Objects.equals(second, tuple.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }

    @Override
    public String toString() {
        return "(%s, %s)".formatted(Objects.toString(first), Objects.toString(second));
    }
}
