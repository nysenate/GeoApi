package gov.nysenate.sage.util;

/**
 * A pair of two Objects of the same type. Order isn't relevant for comparisons.
 * @param <T>
 */
public class Pair<T> extends Tuple<T, T>{
    public Pair(T one, T two) {
        super(one, two);
    }
}
