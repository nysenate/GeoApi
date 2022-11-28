package gov.nysenate.sage.util;

public class Pair<T> {
    private final T one, two;

    public Pair(T one, T two) {
        this.one = one;
        this.two = two;
    }

    public T getOne() {
        return one;
    }

    public T getTwo() {
        return two;
    }
}
